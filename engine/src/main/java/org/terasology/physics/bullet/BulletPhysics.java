/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.physics.bullet;

import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btBroadphasePair;
import com.badlogic.gdx.physics.bullet.collision.btBroadphasePairArray;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.collision.btManifoldPoint;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifold;
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifoldArray;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.collision.btVector3i;
import com.badlogic.gdx.physics.bullet.collision.btVoxelShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.AABB;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.physics.components.TriggerComponent;
import org.terasology.physics.engine.CharacterCollider;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.physics.engine.PhysicsEngineManager;
import org.terasology.physics.engine.PhysicsSystem;
import org.terasology.physics.engine.RigidBody;
import org.terasology.physics.engine.SweepCallback;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Physics engine implementation using TeraBullet (a customised version of JBullet).
 */
public class BulletPhysics implements PhysicsEngine {
    public static final int AABB_SIZE = Integer.MAX_VALUE;

    public static final float SIMD_EPSILON = 1.1920929E-7F;

    private static final Logger logger = LoggerFactory.getLogger(BulletPhysics.class);

    private final Deque<RigidBodyRequest> insertionQueue = Lists.newLinkedList();
    private final Deque<BulletRigidBody> removalQueue = Lists.newLinkedList();

    private final btCollisionDispatcher dispatcher;
    private final btBroadphaseInterface broadphase;
    private final btDiscreteDynamicsWorld discreteDynamicsWorld;
    private final BlockEntityRegistry blockEntityRegistry;
    private Map<EntityRef, BulletRigidBody> entityRigidBodies = Maps.newHashMap();
    private Map<EntityRef, BulletCharacterMoverCollider> entityColliders = Maps.newHashMap();
    private Map<EntityRef, btPairCachingGhostObject> entityTriggers = Maps.newHashMap();
    private List<PhysicsSystem.CollisionPair> collisions = new ArrayList<>();
    private btPersistentManifoldArray manifolds = new btPersistentManifoldArray();

    private final btCollisionConfiguration defaultCollisionConfiguration;
    private final btSequentialImpulseConstraintSolver sequentialImpulseConstraintSolver;
    private final btRigidBody.btRigidBodyConstructionInfo blockConsInf;
    private final btRigidBody.btRigidBodyConstructionInfo liquidConsInfo;

    private final BulletRigidBody rigidBody;
    private final BulletRigidBody liquidBody;
//    private final btConstraintSolverPoolMt solverPoolMt;

    private final btGhostPairCallback callback;

    private final btVoxelShape worldShape;
    private final btVoxelShape liquidShape;

    private final PhysicsWorldWrapper wrapper;
    private final PhysicsLiquidWrapper liquidWrapper;
//    private final btITaskScheduler scheduler;
    public BulletPhysics(WorldProvider world) {

        callback = new btGhostPairCallback();

        broadphase = new btDbvtBroadphase();
        defaultCollisionConfiguration = new btDefaultCollisionConfiguration();

//        scheduler = LinearMath.btCreateDefaultTaskScheduler();
//        LinearMath.btSetTaskScheduler(scheduler);

        dispatcher = new btCollisionDispatcher(defaultCollisionConfiguration);
        sequentialImpulseConstraintSolver = new btSequentialImpulseConstraintSolver();
//        solverPoolMt = new btConstraintSolverPoolMt(100);
        discreteDynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, sequentialImpulseConstraintSolver, defaultCollisionConfiguration);
        discreteDynamicsWorld.setGravity(new Vector3f(0f, -15f, 0f));
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);

        discreteDynamicsWorld.getBroadphase().getOverlappingPairCache().setInternalGhostPairCallback(callback);

        //TODO: reimplement wrapper

        wrapper = new PhysicsWorldWrapper(world);
        worldShape = new btVoxelShape(wrapper,new Vector3f(-AABB_SIZE, -AABB_SIZE, -AABB_SIZE),new Vector3f(AABB_SIZE, AABB_SIZE, AABB_SIZE));

        liquidWrapper = new PhysicsLiquidWrapper(world);
        liquidShape = new btVoxelShape(liquidWrapper,new Vector3f(-AABB_SIZE, -AABB_SIZE, -AABB_SIZE),new Vector3f(AABB_SIZE, AABB_SIZE, AABB_SIZE));//liquidWrapper);*/

        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        btDefaultMotionState blockMotionState = new btDefaultMotionState(matrix4f);

        blockConsInf = new btRigidBody.btRigidBodyConstructionInfo(0, blockMotionState, worldShape, new Vector3f());
        rigidBody = new BulletRigidBody(blockConsInf);
        rigidBody.rb.setCollisionFlags(btCollisionObject.CollisionFlags.CF_STATIC_OBJECT | rigidBody.rb.getCollisionFlags());
        short mask = (short) (~(StandardCollisionGroup.STATIC.getFlag() | StandardCollisionGroup.LIQUID.getFlag()));
        discreteDynamicsWorld.addRigidBody(rigidBody.rb, combineGroups(StandardCollisionGroup.WORLD), mask);


        liquidConsInfo = new btRigidBody.btRigidBodyConstructionInfo(0, blockMotionState, liquidShape, new Vector3f());
        liquidBody = new BulletRigidBody(liquidConsInfo);
        liquidBody.rb.setCollisionFlags(btCollisionObject.CollisionFlags.CF_STATIC_OBJECT | rigidBody.rb.getCollisionFlags());
        discreteDynamicsWorld.addRigidBody(liquidBody.rb, combineGroups(StandardCollisionGroup.LIQUID), StandardCollisionGroup.SENSOR.getFlag());

    }

    //*****************Physics Interface methods******************\\

    @Override
    public List<PhysicsSystem.CollisionPair> getCollisionPairs() {
        List<PhysicsSystem.CollisionPair> temp = collisions;
        collisions = new ArrayList<>();
        return temp;
    }

    @Override
    public void dispose() {
        this.discreteDynamicsWorld.dispose();
        this.dispatcher.dispose();
//        this.solverPoolMt.dispose();
    }

    @Override
    public short combineGroups(CollisionGroup... groups) {
        return combineGroups(Arrays.asList(groups));
    }

    @Override
    public short combineGroups(Iterable<CollisionGroup> groups) {
        short flags = 0;
        for (CollisionGroup group : groups) {
            flags |= group.getFlag();
        }
        return flags;
    }

    @Override
    public List<EntityRef> scanArea(AABB area, CollisionGroup... collisionFilter) {
        return scanArea(area, Arrays.asList(collisionFilter));
    }

    @Override
    public List<EntityRef> scanArea(AABB area, Iterable<CollisionGroup> collisionFilter) {
        // TODO: Add the aabbTest method from newer versions of bullet to TeraBullet, use that instead

        btBoxShape shape = new btBoxShape(area.getExtents());
        btGhostObject scanObject = createCollider(area.getCenter(), shape,StandardCollisionGroup.SENSOR.getFlag(),
                combineGroups(collisionFilter), btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);

        // This in particular is overkill
        broadphase.calculateOverlappingPairs(dispatcher);
        List<EntityRef> result = Lists.newArrayList();
        for (int i = 0; i < scanObject.getNumOverlappingObjects(); ++i) {
            btCollisionObject other = scanObject.getOverlappingObject(i);
            Object userObj = other.getUserPointer();
            if (userObj instanceof EntityRef) {
                result.add((EntityRef) userObj);
            }
        }
        removeCollider(scanObject);
        return result;
    }

    @Override
    public HitResult rayTrace(Vector3f from1, Vector3f direction, float distance, CollisionGroup... collisionGroups) {
        return rayTrace(from1, direction, distance, Sets.newHashSet(), collisionGroups);
    }

    @Override
    public HitResult rayTrace(Vector3f from1, Vector3f direction, float distance, Set<EntityRef> excludedEntities,
                              CollisionGroup... collisionGroups) {
        if (excludedEntities == null) {
            return rayTrace(from1, direction, distance, collisionGroups);
        }
        Vector3f to = new Vector3f(direction);
        Vector3f from = from1;
        to.normalize();
        to.scale(distance);
        to.add(from);

        short filter = combineGroups(collisionGroups);

        // lookup all the collision item ids for these entities
        Set<Integer> excludedCollisionIds = Sets.newHashSet();
        for (EntityRef excludedEntity : excludedEntities) {
            if (entityRigidBodies.containsKey(excludedEntity)) {
                excludedCollisionIds.add(entityRigidBodies.get(excludedEntity).rb.getBroadphaseHandle().getUid());
            }
            if (entityColliders.containsKey(excludedEntity)) {
                excludedCollisionIds.add(entityColliders.get(excludedEntity).collider.getBroadphaseHandle().getUid());
            }
            if (entityTriggers.containsKey(excludedEntity)) {
                excludedCollisionIds.add(entityTriggers.get(excludedEntity).getBroadphaseHandle().getUid());
            }
        }
        ClosestRayResultCallback callback = new ClosestRayResultCallback(from,to);
        callback.setCollisionFilterGroup(StandardCollisionGroup.ALL.getFlag());
        callback.setCollisionFilterMask(filter);

        discreteDynamicsWorld.rayTest(from, to, callback);
        if (callback.hasHit()) {
            btCollisionObject collisionObject = callback.getCollisionObject();
            Vector3f hitPointWorld = new Vector3f();
            callback.getHitPointWorld(hitPointWorld);

            Vector3f hitNormalWorld = new Vector3f();
            callback.getHitNormalWorld(hitNormalWorld);

            if(callback.hasHit()) {
                callback.dispose();
                if (collisionObject.userData instanceof EntityRef) { //we hit an other entity
                    return new HitResult((EntityRef) collisionObject.userData,
                            hitPointWorld,
                            hitNormalWorld);
                }
                else if ((collisionObject.getCollisionFlags() & btCollisionObject.CollisionFlags.CF_VOXEL_OBJECT) > 0) {
                    btVector3i pos = new btVector3i();
                    collisionObject.getVoxelPosition(pos);

                    Vector3i voxelPosition = new Vector3i(pos.getX(), pos.getY(), pos.getZ());
                    final EntityRef entityAt = blockEntityRegistry.getEntityAt(voxelPosition);
                    return new HitResult(entityAt,
                            hitPointWorld,
                            hitNormalWorld,
                            voxelPosition);
                } else { //we hit something we don't understand, assume its nothing and log a warning
                    logger.warn("Unidentified object was hit in the physics engine: {}", collisionObject.userData);
                }
            }

        }
        else {
            callback.dispose();
        }
        return new HitResult();
    }

    @Override
    public void update(float delta) {
        processQueuedBodies();
        applyPendingImpulsesAndForces();
        try {
            PerformanceMonitor.startActivity("Step Simulation");
            if (discreteDynamicsWorld.stepSimulation(delta,8) != 0) {
                for (BulletCharacterMoverCollider collider : entityColliders.values()) {
                    collider.pending = false;
                }
            }
            PerformanceMonitor.endActivity();
        } catch (Exception e) {
            logger.error("Error running simulation step.", e);
        }
        collisions.addAll(getNewCollisionPairs());
    }

    public btDiscreteDynamicsWorld getDiscreteDynamicsWorld()
    {
        return  this.discreteDynamicsWorld;
    }

    @Override
    public boolean removeRigidBody(EntityRef entity) {
        BulletRigidBody rigidBody = entityRigidBodies.remove(entity);
        if (rigidBody != null) {
            removeRigidBody(rigidBody);
            // wake up this entities neighbors

            Matrix4f m = new Matrix4f();
            Vector3f aabbMin = new Vector3f();
            Vector3f aabbMax = new Vector3f();

            rigidBody.rb.getCollisionShape().getAabb(m,aabbMin,aabbMax);
            awakenArea(rigidBody.getLocation(new Vector3f()), (aabbMax.sub(aabbMin)).length() * .5f);
            return true;
        } else {
            logger.warn("Deleting non existing rigidBody from physics engine?! Entity: {}", entity);
            return false;
        }
    }

    @Override
    public boolean updateRigidBody(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        RigidBodyComponent rb = entity.getComponent(RigidBodyComponent.class);
        BulletRigidBody rigidBody = entityRigidBodies.get(entity);

        if (location == null) {
            logger.warn("Updating rigid body of entity that has no "
                    + "LocationComponent?! Nothing is done, except log this"
                    + " warning instead. Entity: {}", entity);
            return false;
        } else if (rigidBody != null) {
            float scale = location.getWorldScale();
            if (Math.abs(rigidBody.rb.getCollisionShape().getLocalScaling().x - scale) > this.getEpsilon()
                    || rigidBody.collidesWith != combineGroups(rb.collidesWith)) {
                removeRigidBody(rigidBody);
                newRigidBody(entity);
            } else {
                rigidBody.rb.setAngularFactor(rb.angularFactor);
                rigidBody.rb.setLinearFactor(rb.linearFactor);
                rigidBody.rb.setFriction(rb.friction);
            }

            return true;
        } else {
            /*
             * During the destruction of the entity it can happen that the rigged body is already destroyed while
             * the location component changes.
             * e.g. because another component that was attached via the LocationComponent gets removed.
             *
             * In such a situation it would be wrong to recreate the rigid body as it can't be updated properly after
             * the destruction of the entity.
             *
             */
            return false;
        }

        // TODO: update if mass or collision groups change
    }

    @Override
    public boolean hasRigidBody(EntityRef entity) {
        return entityRigidBodies.containsKey(entity);
    }

    @Override
    public RigidBody getRigidBody(EntityRef entity) {
        RigidBody rb = entityRigidBodies.get(entity);
        if (rb == null) {
            rb = newRigidBody(entity);
        }
        return rb;
    }

    @Override
    public boolean removeTrigger(EntityRef entity) {
        btGhostObject ghost = entityTriggers.remove(entity);
        if (ghost != null) {
            removeCollider(ghost);
            return true;
        } else {
            return false;
        }
    }

    @Override
    //TODO: update if detectGroups changed
    public boolean updateTrigger(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        btPairCachingGhostObject triggerObj = entityTriggers.get(entity);

        if (location == null) {
            logger.warn("Trying to update or create trigger of entity that has no LocationComponent?! Entity: {}", entity);
            return false;
        }
        if (triggerObj != null) {
            float scale = location.getWorldScale();
            if (Math.abs(triggerObj.getCollisionShape().getLocalScaling().x - scale) > SIMD_EPSILON) {
                discreteDynamicsWorld.removeCollisionObject(triggerObj);
                newTrigger(entity);
            } else {
                Quat4f worldRotation = location.getWorldRotation();
                Vector3f worldPosition = location.getWorldPosition();
                triggerObj.setWorldTransform(new Matrix4f(worldRotation,worldPosition,1.0f));//new Transform(new Matrix4f(worldRotation, worldPosition, 1.0f)));
            }
            return true;
        } else {
            newTrigger(entity);
            return false;
        }
    }

    @Override
    public boolean hasTrigger(EntityRef entity) {
        return entityTriggers.containsKey(entity);
    }

    @Override
    public boolean removeCharacterCollider(EntityRef entity) {
        BulletCharacterMoverCollider toRemove = entityColliders.remove(entity);
        if (toRemove == null) {
            logger.warn("Trying to remove CharacterCollider of entity that has "
                    + "no CharacterCollider in the physics engine. Entity: {}", entity);
            return false;
        } else {
            removeCollider(toRemove.collider);
            return true;
        }
    }

    @Override
    public CharacterCollider getCharacterCollider(EntityRef entity) {
        CharacterCollider cc = entityColliders.get(entity);
        if (cc == null) {
            cc = createCharacterCollider(entity);
        }
        return cc;
    }

    @Override
    public boolean hasCharacterCollider(EntityRef entity) {
        return entityColliders.containsKey(entity);
    }

    @Override
    public Set<EntityRef> getPhysicsEntities() {
        return ImmutableSet.copyOf(entityRigidBodies.keySet());
    }

    @Override
    public Iterator<EntityRef> physicsEntitiesIterator() {
        return entityRigidBodies.keySet().iterator();
    }

    @Override
    public void awakenArea(Vector3f pos, float radius) {
        btPairCachingGhostObject ghost = new btPairCachingGhostObject();
        btSphereShape shape = new btSphereShape(radius);
        ghost.setCollisionShape(shape);
        ghost.setWorldTransform(new Matrix4f(Quat4f.IDENTITY,pos,1.0f));

        discreteDynamicsWorld.addCollisionObject(ghost,(short)-1,(short)-1);
        for (int i = 0; i < ghost.getNumOverlappingObjects(); ++i) {
            btCollisionObject other = ghost.getOverlappingObject(i);
            other.activate(true);
        }
        discreteDynamicsWorld.removeCollisionObject(ghost);
    }

    @Override
    public float getEpsilon() {
        //TODO: figure out how access this from libgdx
        return 1.19209290e-07f;
    }

    //*******************Private helper methods**************************\\


    /**
     * Creates a new trigger.
     *
     * @param entity the entity to create a trigger for.
     */
    private boolean newTrigger(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        TriggerComponent trigger = entity.getComponent(TriggerComponent.class);
        btCollisionShape shape =  PhysicsEngineManager.COLLISION_SHAPE_FACTORY.getShapeFor(entity).underlyingShape;
        if (location != null && trigger != null) {
            float scale = location.getWorldScale();
            shape.setLocalScaling(new Vector3f(scale,scale,scale));
            List<CollisionGroup> detectGroups = Lists.newArrayList(trigger.detectGroups);
            CollisionGroup collisionGroup = trigger.collisionGroup;
            btPairCachingGhostObject triggerObj = createCollider(
                    location.getWorldPosition(),
                    shape,
                    collisionGroup.getFlag(),
                    combineGroups(detectGroups),
                    btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);

            triggerObj.userData = entity;
            btPairCachingGhostObject oldTrigger = entityTriggers.put(entity, triggerObj);
            if (oldTrigger != null) {
                logger.warn("Creating a trigger for an entity that already has a trigger. " +
                        "Multiple trigger pre entity are not supported. Removing old one. Entity: {}", entity);
                removeCollider(oldTrigger);
                return false;
            } else {
                return true;
            }
        } else {
            logger.warn("Trying to create trigger for entity without ShapeComponent or without LocationComponent or without TriggerComponent. Entity: {}", entity);
            return false;
        }
    }

    /**
     * Creates a Collider for the given entity based on the LocationComponent
     * and CharacterMovementComponent.
     * All collision flags are set right for a character movement component.
     *
     * @param owner the entity to create the collider for.
     * @return
     */
    private ArrayList<btConvexShape> shapes = Lists.newArrayList();
    private CharacterCollider createCharacterCollider(EntityRef owner) {
        LocationComponent locComp = owner.getComponent(LocationComponent.class);
        CharacterMovementComponent movementComp = owner.getComponent(CharacterMovementComponent.class);
        if (locComp == null || movementComp == null) {
            throw new IllegalArgumentException("Expected an entity with a Location component and CharacterMovementComponent.");
        }
        Vector3f pos = new Vector3f(locComp.getWorldPosition());
        final float worldScale = locComp.getWorldScale();
        final float height = (movementComp.height - 2 * movementComp.radius) * worldScale;
        final float width = movementComp.radius * worldScale;
        btConvexShape shape = new btCapsuleShape(width , height);
        shapes.add(shape);
        shape.setMargin(0.1f);
        return createCustomCollider(pos, shape, movementComp.collisionGroup.getFlag(), combineGroups(movementComp.collidesWith),
                btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT, owner);
    }
    private RigidBody newRigidBody(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        RigidBodyComponent rigidBody = entity.getComponent(RigidBodyComponent.class);
        btCollisionShape shape =  PhysicsEngineManager.COLLISION_SHAPE_FACTORY.getShapeFor(entity).underlyingShape;
        if (location != null && rigidBody != null) {
            float scale = location.getWorldScale();
            shape.setLocalScaling(new Vector3f(scale, scale, scale));

            if (rigidBody.mass < 1) {
                logger.warn("RigidBodyComponent.mass is set to less than 1.0, this can lead to strange behaviour, such as the objects moving through walls. " +
                        "Entity: {}", entity);
            }
            Vector3f inertia = new Vector3f();
            shape.calculateLocalInertia(rigidBody.mass,inertia);

            btRigidBody.btRigidBodyConstructionInfo info = new btRigidBody.btRigidBodyConstructionInfo(rigidBody.mass, new EntityMotionState(entity), shape, inertia);
            BulletRigidBody collider = new BulletRigidBody(info);
            collider.rb.userData = entity;
            collider.rb.setAngularFactor(rigidBody.angularFactor);
            collider.rb.setLinearFactor(rigidBody.linearFactor);
            collider.rb.setFriction(rigidBody.friction);
            collider.collidesWith = combineGroups(rigidBody.collidesWith);
            collider.setVelocity(rigidBody.velocity, rigidBody.angularVelocity);
            collider.setTransform(location.getWorldPosition(), location.getWorldRotation());
            updateKinematicSettings(rigidBody, collider);
            BulletRigidBody oldBody = entityRigidBodies.put(entity, collider);
            addRigidBody(collider, Lists.newArrayList(rigidBody.collisionGroup), rigidBody.collidesWith);
            if (oldBody != null) {
                removeRigidBody(oldBody);
            }
            return collider;
        } else {
            throw new IllegalArgumentException("Can only create a new rigid body for entities with a LocationComponent," +
                    " RigidBodyComponent and ShapeComponent, this entity misses at least one: " + entity);
        }
    }

    private void removeCollider(btCollisionObject collider) {
        discreteDynamicsWorld.removeCollisionObject(collider);
//        collider.getCollisionShape().dispose();
//        collider.dispose();
    }

    /**
     * Creates a new Collider. Colliders are similar to rigid bodies, except
     * that they do not respond to forces from the physics engine. They collide
     * with other objects and other objects may move if colliding with a
     * collider, but the collider itself will only respond to movement orders
     * from outside the physics engine. Colliders also detect any objects
     * colliding with them. Allowing them to be used as sensors.
     *
     * @param pos            The initial position of the collider.
     * @param shape          The shape of this collider.
     * @param groups
     * @param filters
     * @param collisionFlags
     * @param entity         The entity to associate this collider with. Can be null.
     * @return The newly created and added to the physics engine, Collider object.
     */
    private CharacterCollider createCustomCollider(Vector3f pos, btConvexShape shape, short groups, short filters, int collisionFlags, EntityRef entity) {
        if (entityColliders.containsKey(entity)) {
            entityColliders.remove(entity);
        }
        final BulletCharacterMoverCollider bulletCollider = new BulletCharacterMoverCollider(pos, shape, groups, filters, collisionFlags, entity);
        entityColliders.put(entity, bulletCollider);
        return bulletCollider;
    }

    /**
     * To make sure the state of the physics engine is constant, all changes are
     * stored and executed at the same time. This method executes the stored
     * additions and removals of bodies to and from the physics engine. It also
     * ensures that impulses requested before the body is added to the engine
     * are applied after the body is added to the engine.
     */
    // TODO: None of the above is true.
    // TODO: This isn't necessary, create and remove bodies immediately
    private synchronized void processQueuedBodies() {
        while (!insertionQueue.isEmpty()) {
            RigidBodyRequest request = insertionQueue.poll();
            discreteDynamicsWorld.addRigidBody(request.body.rb, request.groups, request.filter);
        }
        while (!removalQueue.isEmpty()) {
            BulletRigidBody body = removalQueue.poll();
            if(body.isDisposed)
                continue;
            discreteDynamicsWorld.removeRigidBody(body.rb);
            body.dispose();
        }
    }

    /**
     * Applies all pending impulses to the corresponding rigidBodies and clears
     * the pending impulses.
     */
    private void applyPendingImpulsesAndForces() {

        for (Map.Entry<EntityRef, BulletRigidBody> entree : entityRigidBodies.entrySet()) {
            BulletRigidBody body = entree.getValue();
            if(body.pendingImpulse.lengthSquared() > .01f || body.pendingForce.lengthSquared() > .01f ) {
                body.rb.applyCentralImpulse(body.pendingImpulse);
                body.rb.applyCentralForce(body.pendingForce);
            }
            body.pendingImpulse.x = 0;
            body.pendingImpulse.y = 0;
            body.pendingImpulse.z = 0;

            body.pendingForce.x = 0;
            body.pendingForce.y = 0;
            body.pendingForce.z = 0;
        }
    }

    private void addRigidBody(BulletRigidBody body) {

        short filter = (short) (btBroadphaseProxy.CollisionFilterGroups.DefaultFilter | btBroadphaseProxy.CollisionFilterGroups.StaticFilter| btBroadphaseProxy.CollisionFilterGroups.SensorTrigger);
        insertionQueue.add(new RigidBodyRequest(body, (short) btBroadphaseProxy.CollisionFilterGroups.DefaultFilter, filter));
    }

    private void addRigidBody(BulletRigidBody body, List<CollisionGroup> groups, List<CollisionGroup> filter) {
        insertionQueue.add(new RigidBodyRequest(body, combineGroups(groups), combineGroups(filter)));
    }

    private void addRigidBody(BulletRigidBody body, short groups, short filter) {
        insertionQueue.add(new RigidBodyRequest(body, groups, (short) (filter | btBroadphaseProxy.CollisionFilterGroups.SensorTrigger)));
    }

    private void removeRigidBody(BulletRigidBody body) {
        removalQueue.add(body);
    }


    private void updateKinematicSettings(RigidBodyComponent rigidBody, BulletRigidBody collider) {
        if (rigidBody.kinematic) {
            collider.rb.setCollisionFlags(collider.rb.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
            collider.rb.setActivationState(Collision.DISABLE_DEACTIVATION);
        } else {
            collider.rb.setCollisionFlags(collider.rb.getCollisionFlags() & ~btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
            collider.rb.setActivationState(Collision.ACTIVE_TAG);
        }
    }

    private btPairCachingGhostObject createCollider(Vector3f pos, btCollisionShape shape, short groups, short filters, int collisionFlags) {

        Matrix4f startTransform =  new Matrix4f(Quat4f.IDENTITY,pos,1.0f);;
        btPairCachingGhostObject result = new btPairCachingGhostObject();

        result.setWorldTransform(startTransform);
        result.setCollisionShape(shape);
        result.setCollisionFlags(collisionFlags);
        discreteDynamicsWorld.addCollisionObject(result, groups, filters);
        return result;
    }

    private Collection<? extends PhysicsSystem.CollisionPair> getNewCollisionPairs() {
        discreteDynamicsWorld.getCollisionWorld().performDiscreteCollisionDetection();

        List<PhysicsSystem.CollisionPair> collisionPairs = Lists.newArrayList();

        for (btPairCachingGhostObject trigger : entityTriggers.values()) {
            EntityRef entity = (EntityRef) trigger.userData;
            btBroadphasePairArray pairs = trigger.getOverlappingPairCache().getOverlappingPairArray();
            for(int x = 0; x < pairs.size(); x++)
            {
                btBroadphasePair initialPair = pairs.at(x);
                EntityRef otherEntity = null;
                btBroadphaseProxy p0 = btBroadphaseProxy.obtain(initialPair.getPProxy0().getCPointer(),false);
                btBroadphaseProxy p1 = btBroadphaseProxy.obtain(initialPair.getPProxy1().getCPointer(),false);

                if (p0.getClientObject() == trigger.getCPointer()) {

                    btCollisionObject other = btCollisionObject.getInstance(p1.getClientObject());
                    if (other.userData instanceof EntityRef) {
                        otherEntity = (EntityRef) other.userData;
                    }
                } else {
                    btCollisionObject other = btCollisionObject.getInstance(p0.getClientObject());
                    if (other.userData instanceof EntityRef) {
                        otherEntity = (EntityRef) other.userData;
                    }
                }
                if (otherEntity == null || otherEntity == EntityRef.NULL) {
                    continue;
                }
                btBroadphasePair pair = discreteDynamicsWorld.getPairCache().findPair(p0,p1);
                if (pair == null) {
                    continue;
                }

                manifolds.clear();
                if (pair.getAlgorithm() != null) {
                    pair.getAlgorithm().getAllContactManifolds(manifolds);
                }
                for(int y = 0; y < manifolds.size(); y++)
                {

                    btPersistentManifold manifold = manifolds.atConst(y);
                    for (int point = 0; point < manifold.getNumContacts(); ++point) {
                        btManifoldPoint manifoldPoint = manifold.getContactPoint(point);
                        if (manifoldPoint.getDistance() < 0.f) {
                            Vector3f a1 = Vector3f.zero();
                            manifoldPoint.getPositionWorldOnA(a1);

                            Vector3f a2 =Vector3f.zero();
                            manifoldPoint.getPositionWorldOnB(a2);
                            int l = manifoldPoint.getLifeTime();

                            Vector3f a3 = Vector3f.zero();
                            manifoldPoint.getNormalWorldOnB(a3);

                            collisionPairs.add(new PhysicsSystem.CollisionPair(entity, otherEntity,
                                    a1,
                                    a2,
                                    manifoldPoint.getDistance(),
                                    a3));
                            break;
                        }
                    }
                }
                btBroadphaseProxy.free(p0);
                btBroadphaseProxy.free(p1);

            }
        }
        return collisionPairs;
    }

    //********************Private helper classes*********************\\

    private static class RigidBodyRequest {
        final BulletRigidBody body;
        final short groups;
        final short filter;

        RigidBodyRequest(BulletRigidBody body, short groups, short filter) {
            this.body = body;
            this.groups = groups;
            this.filter = filter;
        }
    }

    private static class BulletRigidBody implements RigidBody {

        public final btRigidBody rb;
        public final btRigidBody.btRigidBodyConstructionInfo info;
        public short collidesWith;
        public boolean isDisposed;
        //  private final Transform pooledTransform = new Transform();
        private final Vector3f pendingImpulse = new Vector3f();
        private final Vector3f pendingForce = new Vector3f();

        BulletRigidBody(btRigidBody.btRigidBodyConstructionInfo info) {
            this.info = info;
            rb = new btRigidBody(info);
            isDisposed = false;
        }

        @Override
        public void applyImpulse(Vector3f impulse) {
            pendingImpulse.add(impulse);
        }

        @Override
        public void applyForce(Vector3f force) {
            pendingForce.add(force);
        }

        @Override
        public void translate(Vector3f translation) {
            rb.translate(translation);
        }

        @Override
        public Quat4f getOrientation(Quat4f out) {
            Quat4f rotation = new Quat4f();
            rotation.set(rb.getWorldTransform());
            return rotation;
        }

        @Override
        public Vector3f getLocation(Vector3f out) {
            Vector3f result = rb.getWorldTransform().getTranslation();
            out.x = result.x;
            out.y = result.y;
            out.z = result.z;
            return out;
        }

        @Override
        public Matrix4f getWorldTransform()
        {
            return  rb.getWorldTransform();
        }

        @Override
        public  Matrix4f setWorldTransform(Matrix4f trans)
        {
            rb.setWorldTransform(trans);
            return trans;
        }

        @Override
        public Vector3f getLinearVelocity(Vector3f out) {
            return rb.getLinearVelocity();
        }

        @Override
        public Vector3f getAngularVelocity(Vector3f out) {
            return rb.getAngularVelocity();// out;
        }

        @Override
        public void setLinearVelocity(Vector3f value) {
            rb.setLinearVelocity(value);
        }

        @Override
        public void setAngularVelocity(Vector3f value) {
            rb.setAngularVelocity(value);
        }

        @Override
        public void setOrientation(Quat4f orientation) {
            Matrix4f transform =  rb.getWorldTransform();
            rb.setWorldTransform(new Matrix4f(orientation,transform.getTranslation(),1.0f));
        }

        @Override
        public void setLocation(Vector3f location) {
            Matrix4f translation = rb.getWorldTransform();
            Quat4f quaternion = new Quat4f();
            quaternion.set(translation);

            rb.setWorldTransform(new Matrix4f(quaternion,location,1.0f));

        }

        @Override
        public void setVelocity(Vector3f linear, Vector3f angular) {
            rb.setLinearVelocity(linear);
            rb.setAngularVelocity(angular);
        }

        @Override
        public void setTransform(Vector3f location, Quat4f orientation) {
            Matrix4f transform =  new Matrix4f(rb.getWorldTransform());
            transform.set(new Matrix4f(orientation,location,1.0f));
            rb.setWorldTransform(transform);

        }

        @Override
        public boolean isActive() {
            return rb.isActive();
        }

        public void dispose() {
            if(isDisposed)
                return;
            this.info.dispose();
            this.rb.dispose();
            isDisposed = true;

        }
    }

    private final class BulletCharacterMoverCollider implements CharacterCollider  {
        boolean pending = true;

        //private final Transform temp = new Transform();
        private final Vector3f tempPos = new Vector3f();


        //If a class can figure out that its Collider is a BulletCollider, it
        //is allowed to gain direct access to the bullet body:
        private final btPairCachingGhostObject collider;

        private BulletCharacterMoverCollider(Vector3f pos, btConvexShape shape, List<CollisionGroup> groups, List<CollisionGroup> filters, EntityRef owner) {

            this(pos, shape, groups, filters, 0, owner);
        }

        private BulletCharacterMoverCollider(Vector3f pos, btConvexShape shape, List<CollisionGroup> groups, List<CollisionGroup> filters, int collisionFlags, EntityRef owner) {
            this(pos, shape, combineGroups(groups), combineGroups(filters), collisionFlags, owner);
        }

        private BulletCharacterMoverCollider(Vector3f pos, btConvexShape shape, short groups, short filters, int collisionFlags, EntityRef owner) {
            collider = createCollider(pos, shape, groups, filters, collisionFlags);
            collider.userData = owner;
        }

        @Override
        public boolean isPending() {
            return pending;
        }

        @Override
        public Vector3f getLocation() {
            //collider.getWorldTransform(temp);
            Vector3f pos = collider.getWorldTransform().getTranslation();
            return new Vector3f(pos.x, pos.y, pos.z);
        }

        @Override
        public void setLocation(Vector3f loc) {
            Matrix4f matrix =  collider.getWorldTransform();
            matrix.setTranslation(loc);
            collider.setWorldTransform(matrix);
        }

        @Override
        public SweepCallback sweep(Vector3f startPos, Vector3f endPos, float allowedPenetration, float slopeFactor) {
            Matrix4f startTransform = new Matrix4f(Quat4f.IDENTITY,startPos, 1.0f);
            Matrix4f endTransform = new Matrix4f(Quat4f.IDENTITY,endPos, 1.0f);
            BulletSweepCallback callback = new BulletSweepCallback(collider,startPos,slopeFactor);
            callback.setCollisionFilterGroup(collider.getBroadphaseHandle().getCollisionFilterGroup());
            callback.setCollisionFilterMask(collider.getBroadphaseHandle().getCollisionFilterMask());
            callback.setCollisionFilterGroup((short)(callback.getCollisionFilterGroup() & (~StandardCollisionGroup.SENSOR.getFlag())));
            collider.convexSweepTest((btConvexShape)(collider.getCollisionShape()),startTransform,endTransform,callback,allowedPenetration);
            return callback;
        }
    }
}

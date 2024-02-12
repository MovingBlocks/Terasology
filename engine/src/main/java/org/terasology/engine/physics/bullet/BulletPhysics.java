// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet;

import com.badlogic.gdx.physics.bullet.collision.AllHitsRayResultCallback;
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
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectConstArray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
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
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.HitResult;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.physics.components.RigidBodyComponent;
import org.terasology.engine.physics.components.TriggerComponent;
import org.terasology.engine.physics.components.shapes.BoxShapeComponent;
import org.terasology.engine.physics.components.shapes.CapsuleShapeComponent;
import org.terasology.engine.physics.components.shapes.CylinderShapeComponent;
import org.terasology.engine.physics.components.shapes.HullShapeComponent;
import org.terasology.engine.physics.components.shapes.SphereShapeComponent;
import org.terasology.engine.physics.engine.CharacterCollider;
import org.terasology.engine.physics.engine.PhysicsEngine;
import org.terasology.engine.physics.engine.PhysicsSystem;
import org.terasology.engine.physics.engine.RigidBody;
import org.terasology.engine.physics.engine.SweepCallback;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.joml.geom.AABBf;

import java.nio.FloatBuffer;
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
@RegisterSystem
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

    private final btGhostPairCallback ghostPairCallback;

    /**
     * Creates a Collider for the given entity based on the LocationComponent and CharacterMovementComponent. All
     * collision flags are set right for a character movement component.
     *
     * @param owner the entity to create the collider for.
     * @return
     */
    private ArrayList<btConvexShape> shapes = Lists.newArrayList();

    public BulletPhysics() {

        ghostPairCallback = new btGhostPairCallback();

        broadphase = new btDbvtBroadphase();
        defaultCollisionConfiguration = new btDefaultCollisionConfiguration();

        dispatcher = new btCollisionDispatcher(defaultCollisionConfiguration);
        sequentialImpulseConstraintSolver = new btSequentialImpulseConstraintSolver();
        discreteDynamicsWorld =
                new btDiscreteDynamicsWorld(dispatcher, broadphase, sequentialImpulseConstraintSolver, defaultCollisionConfiguration);
        discreteDynamicsWorld.setGravity(new Vector3f(0f, -PhysicsEngine.GRAVITY, 0f));
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);

        discreteDynamicsWorld.getBroadphase().getOverlappingPairCache().setInternalGhostPairCallback(ghostPairCallback);

    }

    public btDiscreteDynamicsWorld getWorld() {
        return discreteDynamicsWorld;
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
        this.defaultCollisionConfiguration.dispose();
        this.entityTriggers.forEach((k, v) -> v.dispose());
        this.entityRigidBodies.forEach((k, v) -> v.dispose());
        this.ghostPairCallback.dispose();
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
    public List<EntityRef> scanArea(AABBf area, CollisionGroup... collisionFilter) {
        return scanArea(area, Arrays.asList(collisionFilter));
    }

    @Override
    public List<EntityRef> scanArea(AABBf area, Iterable<CollisionGroup> collisionFilter) {
        // TODO: Add the aabbTest method from newer versions of bullet to TeraBullet, use that instead


        Vector3f extent = new Vector3f((area.maxX - area.minX) / 2.0f, (area.maxY - area.minY) / 2.0f,
                (area.maxZ - area.minZ) / 2.0f);
        btBoxShape shape = new btBoxShape(extent);
        btGhostObject scanObject = createCollider(new Vector3f(area.minX, area.minY, area.minZ).add(extent), shape,
                StandardCollisionGroup.SENSOR.getFlag(),
                combineGroups(collisionFilter), btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);

        // This in particular is overkill
        broadphase.calculateOverlappingPairs(dispatcher);
        List<EntityRef> result = Lists.newArrayList();
        for (int i = 0; i < scanObject.getNumOverlappingObjects(); ++i) {
            btCollisionObject other = scanObject.getOverlappingObject(i);
            Object userObj = other.userData;
            if (userObj instanceof EntityRef) {
                result.add((EntityRef) userObj);
            }
        }
        removeCollider(scanObject);
        return result;
    }

    @Override
    public HitResult rayTrace(Vector3f from, Vector3f direction, float distance, CollisionGroup... collisionGroups) {
        Vector3f to = new Vector3f(direction);
        to.normalize();
        to.mul(distance);
        to.add(from);

        short filter = combineGroups(collisionGroups);

        ClosestRayResultCallback callback = new ClosestRayResultCallback(from, to);
        callback.setCollisionFilterGroup(StandardCollisionGroup.ALL.getFlag());
        callback.setCollisionFilterMask(filter);

        discreteDynamicsWorld.rayTest(from, to, callback);
        if (callback.hasHit()) {
            btCollisionObject collisionObject = callback.getCollisionObject();
            Vector3f hitPointWorld = new Vector3f();
            callback.getHitPointWorld(hitPointWorld);

            Vector3f hitNormalWorld = new Vector3f();
            callback.getHitNormalWorld(hitNormalWorld);

            if (callback.hasHit()) {
                callback.dispose();
                if (collisionObject.userData instanceof EntityRef) { //we hit an other entity
                    return new HitResult((EntityRef) collisionObject.userData,
                            hitPointWorld,
                            hitNormalWorld);
                } else if ((collisionObject.getCollisionFlags() & btCollisionObject.CollisionFlags.CF_VOXEL_OBJECT) > 0) {
                    btVector3i pos = new btVector3i();
                    collisionObject.getVoxelPosition(pos);

                    Vector3i voxelPosition = new Vector3i(pos.getX(), pos.getY(), pos.getZ());
                    final EntityRef entityAt = blockEntityRegistry.getEntityAt(voxelPosition);
                    return new HitResult(entityAt,
                            hitPointWorld,
                            hitNormalWorld,
                            voxelPosition);
                } else { //we hit something we don't understand, assume its nothing and log a warning
                    logger.warn("Unidentified object was hit in the physics engine: {}", collisionObject.userData); //NOPMD
                }
            }

        } else {
            callback.dispose();
        }
        return new HitResult();
    }

    @Override
    public HitResult rayTrace(Vector3f from, Vector3f direction, float distance, Set<EntityRef> excludedEntities,
                              CollisionGroup... collisionGroups) {
        if (excludedEntities == null || excludedEntities.size() == 0) {
            return rayTrace(from, direction, distance, collisionGroups);
        }
        Vector3f to = new Vector3f(direction);
        to.normalize();
        to.mul(distance);
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
        AllHitsRayResultCallback callback = new AllHitsRayResultCallback(from, to);
        callback.setCollisionFilterGroup(StandardCollisionGroup.ALL.getFlag());
        callback.setCollisionFilterMask(filter);

        discreteDynamicsWorld.rayTest(from, to, callback);
        if (callback.hasHit()) {
            btCollisionObjectConstArray collisionObjects = callback.getCollisionObjects();
            for (int x = 0; x < collisionObjects.size(); x++) {
                btCollisionObject collisionObject = collisionObjects.atConst(x);
                if (!excludedCollisionIds.contains(collisionObject.getBroadphaseHandle().getUid())) {
                    Vector3f hitPointWorld = callback.getHitPointWorld().at(x);
                    Vector3f hitNormalWorld = callback.getHitNormalWorld().at(x);
                    callback.dispose();
                    if (collisionObject.userData instanceof EntityRef) { //we hit an other entity
                        return new HitResult((EntityRef) collisionObject.userData,
                                hitPointWorld,
                                hitNormalWorld);
                    } else if ((collisionObject.getCollisionFlags() & btCollisionObject.CollisionFlags.CF_VOXEL_OBJECT) > 0) {
                        btVector3i pos = new btVector3i();
                        collisionObject.getVoxelPosition(pos);

                        Vector3i voxelPosition = new Vector3i(pos.getX(), pos.getY(), pos.getZ());
                        final EntityRef entityAt = blockEntityRegistry.getEntityAt(voxelPosition);
                        return new HitResult(entityAt,
                                hitPointWorld,
                                hitNormalWorld,
                                voxelPosition);
                    } else { //we hit something we don't understand, assume its nothing and log a warning
                        logger.warn("Unidentified object was hit in the physics engine: {}", collisionObject.userData); //NOPMD
                    }
                }
            }
        } else {
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
            if (discreteDynamicsWorld.stepSimulation(delta, 10) != 0) {
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

    public btDiscreteDynamicsWorld getDiscreteDynamicsWorld() {
        return this.discreteDynamicsWorld;
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

            rigidBody.rb.getCollisionShape().getAabb(m, aabbMin, aabbMax);
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
        if (location == null) {
            logger.warn("Trying to update or create trigger of entity that has no LocationComponent?! Entity: {}",
                    entity);
            return false;
        }
        btPairCachingGhostObject triggerObj = entityTriggers.get(entity);

        if (triggerObj != null) {
            float scale = location.getWorldScale();
            if (Math.abs(triggerObj.getCollisionShape().getLocalScaling().x - scale) > SIMD_EPSILON) {
                discreteDynamicsWorld.removeCollisionObject(triggerObj);
                newTrigger(entity);
            } else {
                Quaternionf worldRotation = location.getWorldRotation(new Quaternionf());
                Vector3f position = location.getWorldPosition(new Vector3f());
                if (!position.isFinite() || !worldRotation.isFinite()) {
                    logger.warn("Can't update Trigger entity with a non-finite position/rotation?! Entity: {}", entity);
                    return false;
                }
                triggerObj.setWorldTransform(new Matrix4f().translationRotateScale(position, worldRotation, 1.0f));
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
    public void awakenArea(Vector3fc pos, float radius) {
        btPairCachingGhostObject ghost = new btPairCachingGhostObject();
        btSphereShape shape = new btSphereShape(radius);
        ghost.setCollisionShape(shape);
        ghost.setWorldTransform(new Matrix4f().translationRotateScale(pos, new Quaternionf(), 1.0f));

        discreteDynamicsWorld.addCollisionObject(ghost, (short) -1, (short) -1);
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
        btCollisionShape shape = getShapeFor(entity);
        if (shape != null && location != null && trigger != null) {
            float scale = location.getWorldScale();
            shape.setLocalScaling(new Vector3f(scale, scale, scale));
            List<CollisionGroup> detectGroups = Lists.newArrayList(trigger.detectGroups);
            CollisionGroup collisionGroup = trigger.collisionGroup;
            btPairCachingGhostObject triggerObj = createCollider(
                    location.getWorldPosition(new Vector3f()),
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
            logger.warn("Trying to create trigger for entity without ShapeComponent or without LocationComponent " +
                    "or without TriggerComponent. Entity: {}", entity);
            return false;
        }
    }

    private CharacterCollider createCharacterCollider(EntityRef owner) {
        LocationComponent locComp = owner.getComponent(LocationComponent.class);
        CharacterMovementComponent movementComp = owner.getComponent(CharacterMovementComponent.class);
        if (locComp == null || movementComp == null) {
            throw new IllegalArgumentException("Expected an entity with a Location component and " +
                    "CharacterMovementComponent.");
        }
        Vector3f pos = locComp.getWorldPosition(new Vector3f());
        final float worldScale = locComp.getWorldScale();
        final float height = (movementComp.height - 2 * movementComp.radius) * worldScale;
        final float width = movementComp.radius * worldScale;
        btConvexShape shape = new btCapsuleShape(width, height);
        shapes.add(shape);
        shape.setMargin(0.1f);
        return createCustomCollider(pos, shape, movementComp.collisionGroup.getFlag(),
                combineGroups(movementComp.collidesWith),
                btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT, owner);
    }

    private RigidBody newRigidBody(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        RigidBodyComponent rigidBody = entity.getComponent(RigidBodyComponent.class);
        btCollisionShape shape = getShapeFor(entity);
        if (location != null && rigidBody != null && shape != null) {
            float scale = location.getWorldScale();
            shape.setLocalScaling(new Vector3f(scale, scale, scale));

            if (rigidBody.mass < 1) {
                logger.warn("RigidBodyComponent.mass is set to less than 1.0, this can lead to strange behaviour, " +
                        "such as the objects moving through walls. " +
                        "Entity: {}", entity);
            }
            Vector3f inertia = new Vector3f();
            shape.calculateLocalInertia(rigidBody.mass, inertia);

            btRigidBody.btRigidBodyConstructionInfo info =
                    new btRigidBody.btRigidBodyConstructionInfo(rigidBody.mass, new EntityMotionState(entity), shape, inertia);
            BulletRigidBody collider = new BulletRigidBody(info);
            collider.rb.userData = entity;
            collider.rb.setAngularFactor(rigidBody.angularFactor);
            collider.rb.setLinearFactor(rigidBody.linearFactor);
            collider.rb.setFriction(rigidBody.friction);
            collider.collidesWith = combineGroups(rigidBody.collidesWith);
            collider.setVelocity(rigidBody.velocity, rigidBody.angularVelocity);
            collider.setTransform(location.getWorldPosition(new Vector3f()),
                    location.getWorldRotation(new Quaternionf()));
            updateKinematicSettings(rigidBody, collider);
            BulletRigidBody oldBody = entityRigidBodies.put(entity, collider);
            addRigidBody(collider, Lists.newArrayList(rigidBody.collisionGroup), rigidBody.collidesWith);
            if (oldBody != null) {
                removeRigidBody(oldBody);
            }
            return collider;
        } else {
            throw new IllegalArgumentException("Can only create a new rigid body for entities with a " +
                    "LocationComponent," +
                    " RigidBodyComponent and ShapeComponent, this entity misses at least one: " + entity);
        }
    }

    private void removeCollider(btCollisionObject collider) {
        discreteDynamicsWorld.removeCollisionObject(collider);
        collider.dispose();
    }

    /**
     * Creates a new Collider. Colliders are similar to rigid bodies, except that they do not respond to forces from the
     * physics engine. They collide with other objects and other objects may move if colliding with a collider, but the
     * collider itself will only respond to movement orders from outside the physics engine. Colliders also detect any
     * objects colliding with them. Allowing them to be used as sensors.
     *
     * @param pos The initial position of the collider.
     * @param shape The shape of this collider.
     * @param groups
     * @param filters
     * @param collisionFlags
     * @param entity The entity to associate this collider with. Can be null.
     * @return The newly created and added to the physics engine, Collider object.
     */
    private CharacterCollider createCustomCollider(Vector3f pos, btConvexShape shape, short groups, short filters,
                                                   int collisionFlags, EntityRef entity) {
        if (entityColliders.containsKey(entity)) {
            entityColliders.remove(entity);
        }
        final BulletCharacterMoverCollider bulletCollider = new BulletCharacterMoverCollider(pos, shape, groups,
                filters, collisionFlags, entity);
        entityColliders.put(entity, bulletCollider);
        return bulletCollider;
    }

    /**
     * To make sure the state of the physics engine is constant, all changes are stored and executed at the same time.
     * This method executes the stored additions and removals of bodies to and from the physics engine. It also ensures
     * that impulses requested before the body is added to the engine are applied after the body is added to the
     * engine.
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
            if (body.isDisposed) {
                continue;
            }
            discreteDynamicsWorld.removeRigidBody(body.rb);
            body.dispose();
        }
    }

    /**
     * Applies all pending impulses to the corresponding rigidBodies and clears the pending impulses.
     */
    private void applyPendingImpulsesAndForces() {

        for (Map.Entry<EntityRef, BulletRigidBody> entree : entityRigidBodies.entrySet()) {
            BulletRigidBody body = entree.getValue();
            if (body.pendingImpulse.lengthSquared() > .01f || body.pendingForce.lengthSquared() > .01f) {
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

    private void addRigidBody(BulletRigidBody body, List<CollisionGroup> groups, List<CollisionGroup> filter) {
        insertionQueue.add(new RigidBodyRequest(body, combineGroups(groups), combineGroups(filter)));
    }

    private void removeRigidBody(BulletRigidBody body) {
        removalQueue.add(body);
    }

    /**
     * Returns the shape belonging to the given entity. It currently knows 4 different shapes: Sphere, Capsule, Cylinder
     * or arbitrary. The shape is determined based on the shape component of the given entity. If the entity has somehow
     * got multiple shapes, only one is picked. The order of priority is: Sphere, Capsule, Cylinder, arbitrary.
     * <br><br>
     * TODO: Flyweight this (take scale as parameter)
     *
     * @param entityRef the entity to get the shape of.
     * @return the shape of the entity, ready to be used by Bullet.
     */
    private btCollisionShape getShapeFor(EntityRef entityRef) {
        BoxShapeComponent box = entityRef.getComponent(BoxShapeComponent.class);
        if (box != null) {
            Vector3f halfExtents = new Vector3f(box.extents);
            return new btBoxShape(halfExtents.mul(.5f));
        }
        SphereShapeComponent sphere = entityRef.getComponent(SphereShapeComponent.class);
        if (sphere != null) {
            return new btSphereShape(sphere.radius);
        }
        CapsuleShapeComponent capsule = entityRef.getComponent(CapsuleShapeComponent.class);
        if (capsule != null) {
            return new btCapsuleShape(capsule.radius, capsule.height);
        }
        CylinderShapeComponent cylinder = entityRef.getComponent(CylinderShapeComponent.class);
        if (cylinder != null) {
            return new btCylinderShape(new Vector3f(cylinder.radius, 0.5f * cylinder.height, cylinder.radius));
        }
        HullShapeComponent hull = entityRef.getComponent(HullShapeComponent.class);
        if (hull != null) {
            VertexAttributeBinding<Vector3fc, Vector3f> positions = hull.sourceMesh.vertices();
            final int numVertices = hull.sourceMesh.elementCount();
            FloatBuffer buffer = BufferUtils.createFloatBuffer(numVertices * 3);
            Vector3f pos = new Vector3f();
            for (int i = 0; i < numVertices; i++) {
                positions.get(i, pos);
                buffer.put(pos.x);
                buffer.put(pos.y);
                buffer.put(pos.z);
            }
            return new btConvexHullShape(buffer, numVertices, 3 * Float.BYTES);
        }
        CharacterMovementComponent characterMovementComponent =
                entityRef.getComponent(CharacterMovementComponent.class);
        if (characterMovementComponent != null) {
            return new btCapsuleShape(characterMovementComponent.pickupRadius,
                    characterMovementComponent.height - 2 * characterMovementComponent.radius);
        }
        logger.error("Creating physics object that requires a ShapeComponent or CharacterMovementComponent, but has " +
                "neither. Entity: {}", entityRef);
        throw new IllegalArgumentException("Creating physics object that requires a ShapeComponent or " +
                "CharacterMovementComponent, but has neither. Entity: " + entityRef);
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

        Matrix4f startTransform = new Matrix4f().translation(pos);
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
            for (int x = 0; x < pairs.size(); x++) {
                btBroadphasePair initialPair = pairs.at(x);
                EntityRef otherEntity = null;
                btBroadphaseProxy p0 = btBroadphaseProxy.obtain(initialPair.getPProxy0().getCPointer(), false);
                btBroadphaseProxy p1 = btBroadphaseProxy.obtain(initialPair.getPProxy1().getCPointer(), false);

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
                btBroadphasePair pair = discreteDynamicsWorld.getPairCache().findPair(p0, p1);
                if (pair == null) {
                    continue;
                }

                manifolds.clear();
                if (pair.getAlgorithm() != null) {
                    pair.getAlgorithm().getAllContactManifolds(manifolds);
                }
                for (int y = 0; y < manifolds.size(); y++) {

                    btPersistentManifold manifold = manifolds.atConst(y);
                    for (int point = 0; point < manifold.getNumContacts(); ++point) {
                        btManifoldPoint manifoldPoint = manifold.getContactPoint(point);
                        if (manifoldPoint.getDistance() < 0.f) {
                            Vector3f a1 = new Vector3f();
                            manifoldPoint.getPositionWorldOnA(a1);

                            Vector3f a2 = new Vector3f();
                            manifoldPoint.getPositionWorldOnB(a2);

                            Vector3f a3 = new Vector3f();
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
        public Quaternionf getOrientation(Quaternionf out) {
            return out.setFromUnnormalized(rb.getWorldTransform());
        }

        @Override
        public Vector3f getLocation(Vector3f out) {
            Vector3f result = rb.getWorldTransform().getTranslation(new Vector3f());
            out.x = result.x;
            out.y = result.y;
            out.z = result.z;
            return out;
        }

        @Override
        public Matrix4f getWorldTransform() {
            return rb.getWorldTransform();
        }

        @Override
        public Matrix4f setWorldTransform(Matrix4f trans) {
            rb.setWorldTransform(trans);
            return trans;
        }

        @Override
        public Vector3f getLinearVelocity(Vector3f out) {
            //TODO: set value on `out` vector
            return rb.getLinearVelocity();
        }

        @Override
        public Vector3f getAngularVelocity(Vector3f out) {
            //TODO: set value on `out` vector
            return rb.getAngularVelocity();
        }

        @Override
        public void setLinearVelocity(Vector3f value) {
            rb.activate();
            rb.setLinearVelocity(value);
        }

        @Override
        public void setAngularVelocity(Vector3f value) {
            rb.activate();
            rb.setAngularVelocity(value);
        }

        @Override
        public void setOrientation(Quaternionf orientation) {
            Matrix4f transform = rb.getWorldTransform();
            rb.setWorldTransform(new Matrix4f().translationRotateScale(transform.getTranslation(new Vector3f()),
                    orientation, 1.0f));
        }

        @Override
        public void setLocation(Vector3f location) {
            Matrix4f translation = rb.getWorldTransform();
            Quaternionf quaternion = new Quaternionf().setFromUnnormalized(translation);

            rb.setWorldTransform(new Matrix4f().translationRotateScale(location, quaternion, 1.0f));

        }

        @Override
        public void setVelocity(Vector3f linear, Vector3f angular) {
            rb.setLinearVelocity(linear);
            rb.setAngularVelocity(angular);
        }

        @Override
        public void setTransform(Vector3f location, Quaternionf orientation) {
            Matrix4f transform = new Matrix4f(rb.getWorldTransform());
            transform.set(new Matrix4f().translationRotateScale(location, orientation, 1.0f));
            rb.setWorldTransform(transform);

        }

        @Override
        public boolean isActive() {
            return rb.isActive();
        }

        public void dispose() {
            if (isDisposed) {
                return;
            }
            this.info.dispose();
            this.rb.dispose();
            isDisposed = true;

        }
    }

    private final class BulletCharacterMoverCollider implements CharacterCollider {
        boolean pending = true;

        //private final Transform temp = new Transform();
        private final Vector3f tempPos = new Vector3f();


        //If a class can figure out that its Collider is a BulletCollider, it
        //is allowed to gain direct access to the bullet body:
        private final btPairCachingGhostObject collider;

        private BulletCharacterMoverCollider(Vector3f pos, btConvexShape shape, List<CollisionGroup> groups,
                                             List<CollisionGroup> filters, EntityRef owner) {

            this(pos, shape, groups, filters, 0, owner);
        }

        private BulletCharacterMoverCollider(Vector3f pos, btConvexShape shape, List<CollisionGroup> groups,
                                             List<CollisionGroup> filters, int collisionFlags, EntityRef owner) {
            this(pos, shape, combineGroups(groups), combineGroups(filters), collisionFlags, owner);
        }

        private BulletCharacterMoverCollider(Vector3f pos, btConvexShape shape, short groups, short filters,
                                             int collisionFlags, EntityRef owner) {
            collider = createCollider(pos, shape, groups, filters, collisionFlags);
            collider.userData = owner;
        }

        @Override
        public boolean isPending() {
            return pending;
        }

        @Override
        public Vector3f getLocation() {
            Vector3f pos = collider.getWorldTransform().getTranslation(new Vector3f());
            return new Vector3f(pos.x, pos.y, pos.z);
        }

        @Override
        public void setLocation(Vector3f loc) {
            Matrix4f matrix = collider.getWorldTransform();
            matrix.setTranslation(loc);
            collider.setWorldTransform(matrix);
        }

        @Override
        public SweepCallback sweep(Vector3f startPos, Vector3f endPos, float allowedPenetration, float slopeFactor) {
            Matrix4f startTransform = new Matrix4f().translationRotateScale(startPos, new Quaternionf(), 1.0f);
            Matrix4f endTransform = new Matrix4f().translationRotateScale(endPos, new Quaternionf(), 1.0f);
            BulletSweepCallback callback = new BulletSweepCallback(collider, startPos, slopeFactor);
            callback.setCollisionFilterGroup(collider.getBroadphaseHandle().getCollisionFilterGroup());
            callback.setCollisionFilterMask(collider.getBroadphaseHandle().getCollisionFilterMask());
            callback.setCollisionFilterGroup((short) (callback.getCollisionFilterGroup() & (~StandardCollisionGroup.SENSOR.getFlag())));
            collider.convexSweepTest((btConvexShape) (collider.getCollisionShape()), startTransform, endTransform,
                    callback, allowedPenetration);
            return callback;
        }
    }
}

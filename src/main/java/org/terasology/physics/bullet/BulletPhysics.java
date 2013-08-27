/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.physics.PhysicsEngine;
import org.terasology.physics.bullet.BulletSweepCallback;
import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.voxel.VoxelWorldShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.iterator.TFloatIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.EventReceiver;
import org.terasology.entitySystem.event.EventSystem;
import org.terasology.math.AABB;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.physics.CharacterCollider;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.physics.HitResult;
import org.terasology.physics.events.PhysicsSystem;
import org.terasology.physics.PhysicsWorldWrapper;
import org.terasology.physics.RigidBody;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.components.TriggerComponent;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.physics.shapes.CapsuleShapeComponent;
import org.terasology.physics.shapes.CylinderShapeComponent;
import org.terasology.physics.shapes.HullShapeComponent;
import org.terasology.physics.shapes.SphereShapeComponent;

/**
 * Renders blocks using the Bullet physics library.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BulletPhysics implements PhysicsEngine {

    private static final Logger logger = LoggerFactory.getLogger(BulletPhysics.class);

    private final Deque<RigidBodyRequest> insertionQueue = Lists.newLinkedList();
    private final Deque<BulletRigidBody> removalQueue = Lists.newLinkedList();

    private final CollisionDispatcher dispatcher;
    private final BroadphaseInterface broadphase;
    private final CollisionConfiguration defaultCollisionConfiguration;
    private final SequentialImpulseConstraintSolver sequentialImpulseConstraintSolver;
    private final DiscreteDynamicsWorld discreteDynamicsWorld;
    private final BlockEntityRegistry blockEntityRegistry;
    private final PhysicsWorldWrapper wrapper;
    private Map<EntityRef, BulletRigidBody> entityRigidBodies = Maps.newHashMap();
    private Map<EntityRef, BulletCharacterMoverCollider> entityColliders = Maps.newHashMap();
    private Map<EntityRef, PairCachingGhostObject> entityTriggers = Maps.newHashMap();

    public BulletPhysics(WorldProvider world) {
        broadphase = new DbvtBroadphase();
        broadphase.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
        defaultCollisionConfiguration = new DefaultCollisionConfiguration();
        dispatcher = new CollisionDispatcher(defaultCollisionConfiguration);
        sequentialImpulseConstraintSolver = new SequentialImpulseConstraintSolver();
        discreteDynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, sequentialImpulseConstraintSolver, defaultCollisionConfiguration);
        discreteDynamicsWorld.setGravity(new Vector3f(0f, -15f, 0f));
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);

        wrapper = new PhysicsWorldWrapper(world);
        VoxelWorldShape worldShape = new VoxelWorldShape(wrapper);

        Matrix3f rot = new Matrix3f();
        rot.setIdentity();
        DefaultMotionState blockMotionState = new DefaultMotionState(new Transform(new Matrix4f(rot, new Vector3f(0, 0, 0), 1.0f)));
        RigidBodyConstructionInfo blockConsInf = new RigidBodyConstructionInfo(0, blockMotionState, worldShape, new Vector3f());
        BulletRigidBody rigidBody = new BulletRigidBody(blockConsInf);
        rigidBody.rb.setCollisionFlags(CollisionFlags.STATIC_OBJECT | rigidBody.rb.getCollisionFlags());
        short mask = (short) (CollisionFilterGroups.ALL_FILTER ^ CollisionFilterGroups.STATIC_FILTER);
        discreteDynamicsWorld.addRigidBody(rigidBody.rb, combineGroups(StandardCollisionGroup.WORLD), mask);
    }
    
    //*****************Physics Interface methods******************\\
    /**
     * Return a list with all CollisionPairs that occurred in the previous
     * physics simulation step.
     * TODO: alter this method to return all collision pairs since the last call to this method.
     *
     * @return A newly allocated list with all pairs of entities that collided.
     */
    @Override
    public List<PhysicsSystem.CollisionPair> getCollisionPairs() {
        List<PhysicsSystem.CollisionPair> collisionPairs = Lists.newArrayList();

        DynamicsWorld world = discreteDynamicsWorld;
        ObjectArrayList<PersistentManifold> manifolds = new ObjectArrayList<PersistentManifold>();
        for (PairCachingGhostObject trigger : entityTriggers.values()) {
            EntityRef entity = (EntityRef) trigger.getUserPointer();
            for (BroadphasePair initialPair : trigger.getOverlappingPairCache().getOverlappingPairArray()) {
                EntityRef otherEntity = null;
                if (initialPair.pProxy0.clientObject == trigger) {
                    if (((CollisionObject) initialPair.pProxy1.clientObject).getUserPointer() instanceof EntityRef) {
                        otherEntity = (EntityRef) ((CollisionObject) initialPair.pProxy1.clientObject).getUserPointer();
                    }
                } else {
                    if (((CollisionObject) initialPair.pProxy0.clientObject).getUserPointer() instanceof EntityRef) {
                        otherEntity = (EntityRef) ((CollisionObject) initialPair.pProxy0.clientObject).getUserPointer();
                    }
                }
                if (otherEntity == null) {
                    continue;
                }
                BroadphasePair pair = world.getPairCache().findPair(initialPair.pProxy0, initialPair.pProxy1);
                if (pair == null) {
                    continue;
                }
                manifolds.clear();
                if (pair.algorithm != null) {
                    pair.algorithm.getAllContactManifolds(manifolds);
                }
                for (PersistentManifold manifold : manifolds) {
                    for (int point = 0; point < manifold.getNumContacts(); ++point) {
                        ManifoldPoint manifoldPoint = manifold.getContactPoint(point);
                        if (manifoldPoint.getDistance() < 0) {
                            collisionPairs.add(new PhysicsSystem.CollisionPair(entity, otherEntity));
                            break;
                        }
                    }
                }
            }
        }
        return collisionPairs;
    }
     
    @Override
    public void dispose() {
        discreteDynamicsWorld.destroy();
        wrapper.dispose();
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
        BoxShape shape = new BoxShape(area.getExtents());
        GhostObject scanObject = createCollider(area.getCenter(), shape, CollisionFilterGroups.SENSOR_TRIGGER,
                combineGroups(collisionFilter), CollisionFlags.NO_CONTACT_RESPONSE);
        // This in particular is overkill
        broadphase.calculateOverlappingPairs(dispatcher);
        List<EntityRef> result = Lists.newArrayList();
        for (int i = 0; i < scanObject.getNumOverlappingObjects(); ++i) {
            CollisionObject other = scanObject.getOverlappingObject(i);
            Object userObj = other.getUserPointer();
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
        to.scale(distance);
        to.add(from);

        short filter = combineGroups(collisionGroups);

        CollisionWorld.ClosestRayResultWithUserDataCallback closest =
                new CollisionWorld.ClosestRayResultWithUserDataCallback(from, to);
        closest.collisionFilterGroup = CollisionFilterGroups.ALL_FILTER;
        closest.collisionFilterMask = filter;

        discreteDynamicsWorld.rayTest(from, to, closest);
        if (closest.hasHit()) {
            if (closest.userData instanceof Vector3i) { //We hit a world block
                final EntityRef entityAt = blockEntityRegistry.getEntityAt((Vector3i) closest.userData);
                return new HitResult(entityAt, closest.hitPointWorld, closest.hitNormalWorld, (Vector3i) closest.userData);
            } else if (closest.userData instanceof EntityRef) { //we hit an other entity
                return new HitResult((EntityRef) closest.userData, closest.hitPointWorld, closest.hitNormalWorld);
            } else { //we hit something we don't understand, assume its nothing and log a warning
                logger.warn("Unidentified object was hit in the physics engine: " + closest.userData);
                return new HitResult();
            }
        } else { //nothing was hit
            return new HitResult();
        }
    }
    
    /**
     * Advances the physics engine with the given amount of time in seconds. As
     * long as this time does not exceed 8/60 seconds, the game speed will be
     * constant.
     * @param delta amount of time to advance the engine in seconds.
     */
    @Override
    public void update(float delta) {
        processQueuedBodies();
        applyPendingImpulses();
        try {
            PerformanceMonitor.startActivity("Step Simulation");
            discreteDynamicsWorld.stepSimulation(delta, 8);
            PerformanceMonitor.endActivity();
        } catch (Exception e) {
            logger.error("Error running simulation step.", e);
        }
    }
    
    /**
     * Creates a new rigid body and adds it to the physics engine. The returned
     * RigidBody can be used for various operations. Most of these operations
     * can also be executed by method of this class by giving the entity the
     * body belongs to as additional parameter. If the given entity already had
     * a rigid body attached to it, this body is removed from the physics
     * engine and will no longer be valid.
     *
     * @param entity the entity to create a rigid body for. Must have a
     * LocationComponent, RigidBodyComponent and ShapeComponent. If not an
     * exception is thrown.
     * @return The newly created RigidBody. All exposed methods are ready to be
     * used.
     */
    @Override
    public RigidBody newRigidBody(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        RigidBodyComponent rigidBody = entity.getComponent(RigidBodyComponent.class);
        ConvexShape shape = getShapeFor(entity);
        if (location != null && rigidBody != null && shape != null) {
            float scale = location.getWorldScale();
            shape.setLocalScaling(new Vector3f(scale, scale, scale));

            Vector3f fallInertia = new Vector3f();
            shape.calculateLocalInertia(rigidBody.mass, fallInertia);
            RigidBodyConstructionInfo info = new RigidBodyConstructionInfo(rigidBody.mass, new EntityMotionState(entity), shape, fallInertia);
            BulletRigidBody collider = new BulletRigidBody(info);
            collider.rb.setUserPointer(entity);
            updateKinematicSettings(rigidBody, collider);
            BulletRigidBody oldBody = entityRigidBodies.put(entity, collider);
            addRigidBody(collider, Lists.<CollisionGroup>newArrayList(rigidBody.collisionGroup), rigidBody.collidesWith);
            if (oldBody != null) {
                removeRigidBody(oldBody);
            }
            return collider;
        } else {
            throw new IllegalArgumentException("Can only create a new rigid body for entities with a LocationComponent, RigidBodyComponent and ShapeComponent, this entity misses at least one: " + entity);
        }
    }
    
    @Override
    public boolean removeRigidBody(EntityRef entity) {
        BulletRigidBody rigidBody = entityRigidBodies.remove(entity);
        if(rigidBody != null) {
            removeRigidBody(rigidBody);
            return true;
        } else {
            logger.warn("Deleting non existing rigidBody from physics engine?!");
            return false;
        }
    }
    
    /**
     * Updates the shape and position of the rigidBody belonging to the given
     * entity. If the given entity had no rigidBody in the physics engine,
     * nothing will happen. The return value can be used to see whether or not
     * something has happened.
     *
     * @param entity the entity of which the rigidBody needs updating.
     * @return true if there was already a rigidBody registered for the entity
     * (which is now updated), false otherwise.
     */
    @Override
    public boolean updateRigidBody(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        BulletRigidBody rigidBody = entityRigidBodies.get(entity);

        if (location == null) {
            logger.warn("Updating rigid body of entity that has no "
                    + "LocationComponent?! Nothing is done, except log this"
                    + " warning instead.");
            return false;
        } else if (rigidBody != null) {
            float scale = location.getWorldScale();
            if (Math.abs(rigidBody.rb.getCollisionShape().getLocalScaling(new Vector3f()).x - scale) > BulletGlobals.SIMD_EPSILON) {
                removeRigidBody(rigidBody);
                newRigidBody(entity);
            }

            updateKinematicSettings(entity.getComponent(RigidBodyComponent.class), rigidBody);
            return true;
        } else {
            //If null, the rigid body did not exist yet in the map, which cannot happen, since we are processing a change to the rigid body.
            logger.warn("Updating a non-existing rigid body. Creating a new one "
                    + "instead, also logging this warning since it should not "
                    + "happen. Entity: " + entity);
            newRigidBody(entity);
            return false;
        }

        // TODO: update if mass or collision groups change
    }
    
    /**
     * @param entity
     * @return Returns true if there is a rigidBody in the physics engine
     * related to the given entity, false otherwise.
     */
    @Override
    public boolean hasRigidBody(EntityRef entity) {
        return entityRigidBodies.containsKey(entity);
    }
    
    /**
     * Returns the rigid body associated with the given entity.
     *
     * @param entity
     * @return null if there is no rigid body for the given entity (yet),
     * otherwise it returns the requested RigidBody instance.
     */
    @Override
    public RigidBody getRigidBody(EntityRef entity) {
        RigidBody rb = entityRigidBodies.get(entity);
        if (rb == null) {
            throw new IllegalStateException("Trying to retrieve the rigid body "
                    + "of en entity that has none. Throwing exception with a "
                    + "decent error message instead of returning null. Entity: " + entity);
        }
        return rb;
    }
   
    /**
     * Creates a new trigger. An entity with a trigger attached to it will
     * generate collision pairs when it collides or intersects with other
     * objects. A good example of its usage is picking up items. By creating a
     * trigger for a player, a collision event will be generated when the shape
     * of the player collides with the shape of a dropped item. This event is
     * send by the physics class. Instead it is stored and can be retrieved by
     * the getCollisionPairs() method. The PhysicsSystem class uses this
     * mechanism to generate normal Terasolegy events.
     *
     * @param entity the entity to create a trigger for.
     */
    @Override
    public boolean newTrigger(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        TriggerComponent trigger = entity.getComponent(TriggerComponent.class);
        ConvexShape shape = getShapeFor(entity);
        if (shape != null && location != null && trigger != null) {
            float scale = location.getWorldScale();
            shape.setLocalScaling(new Vector3f(scale, scale, scale));
            List<CollisionGroup> detectGroups = Lists.newArrayList(trigger.detectGroups);
            PairCachingGhostObject triggerObj = createCollider(
                    location.getWorldPosition(), 
                    shape,
                    StandardCollisionGroup.SENSOR.getFlag(), 
                    combineGroups(detectGroups), 
                    CollisionFlags.NO_CONTACT_RESPONSE);
            triggerObj.setUserPointer(entity);
            PairCachingGhostObject oldTrigger = entityTriggers.put(entity, triggerObj);
            if(oldTrigger != null) {
                logger.warn("Creating a trigger for an entity that already has a trigger. Multiple trigger pre entity are not supported. Removing old one.");
                removeCollider(oldTrigger);
                return false;
            } else {
                return true;
            }
        } else {
            logger.warn("Tryig to create trigger for entity without ShapeComponent or without LocationComponent or without TriggerComponent");
            return false;
        }
    }

    @Override
    public boolean removeTrigger(EntityRef entity) {
        GhostObject ghost = entityTriggers.remove(entity);
        if (ghost != null) {
            removeCollider(ghost);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean updateTrigger(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        PairCachingGhostObject triggerObj = entityTriggers.get(entity);

        if(location == null) {
            logger.warn("Trying to update trigger of entity that has no LocationComponent?!");
            return false;
        }
        if (triggerObj != null) {
            float scale = location.getWorldScale();
            if (Math.abs(triggerObj.getCollisionShape().getLocalScaling(new Vector3f()).x - scale) > BulletGlobals.SIMD_EPSILON) {
                discreteDynamicsWorld.removeCollisionObject(triggerObj);
                newTrigger(entity);
            } else {
                triggerObj.setWorldTransform(new Transform(new Matrix4f(location.getWorldRotation(), location.getWorldPosition(), 1.0f)));
            }
            return true;
        } else {
            logger.warn("Trying to update a trigger of an entity that has no trigger. Creating a new trigger instead. Entity: " + entity);
            newTrigger(entity);
            return false;
        }
    }
    
    /**
     * Checks if the given entity has a trigger attached to it.
     * @param entity the entity to check for.
     * @return true if the entity has a trigger, false otherwise.
     */
    @Override
    public boolean hasTrigger(EntityRef entity) {
        return entityTriggers.containsKey(entity);
    }
    
    /**
     * Creates a Collider for the given entity based on the LocationComponent 
     * and CharacterMovementComponent. 
     * All collision flags are set right for a character movement component.
     *
     * @param owner the entity to create the collider for.
     * @return
     */
    @Override
    public CharacterCollider createCharacterCollider(EntityRef owner) {
        LocationComponent locComp = owner.getComponent(LocationComponent.class);
        CharacterMovementComponent movementComp = owner.getComponent(CharacterMovementComponent.class);
        if(locComp == null || movementComp == null) {
            throw new IllegalArgumentException("Expected an entity with a Location component and CharacterMovementComponent.");
        }
        Vector3f pos = locComp.getWorldPosition();
        final float worldScale = locComp.getWorldScale();
        final float height = (movementComp.height - 2 * movementComp.radius) * worldScale;
        final float width = movementComp.radius * worldScale;
        ConvexShape shape =  new CapsuleShape(width, height);
        shape.setMargin(0.1f);
        return createCustomCollider(pos, shape, movementComp.collisionGroup.getFlag(), combineGroups(movementComp.collidesWith),
                CollisionFlags.CHARACTER_OBJECT, owner);
    }
    
    @Override
    public boolean removeCharacterCollider(EntityRef entity) {
        BulletCharacterMoverCollider toRemove = entityColliders.remove(entity);
        if(toRemove == null) {
            logger.warn("Trying to remove CharacterCollider of entity that has "
                    + "no CharacterCollider in the physics engine. Entity: " + entity);
            return false;
        } else {
            removeCollider(toRemove.collider);
            return true;
        }
    }
    
    @Override
    public CharacterCollider getCharacterCollider(EntityRef entity) {
        CharacterCollider cc = entityColliders.get(entity);
        return cc;
    }
    
    @Override
    public boolean hasCharacterCollider(EntityRef entity) {
        return entityColliders.containsKey(entity);
    }
    
    /**
     * @return A set with all entities that have a rigidBody that is active in 
     * the physics engine. A new set is created that is not backed by this class.
     */
    @Override
    public Set<EntityRef> getPhysicsEntities() {
        return new HashSet<EntityRef>(entityRigidBodies.keySet());
    }
    
    /**
     * Warning: Using this iterator to remove elements has an unpredictable
     * behaviour. Do not use this functionality! Instead, store the elements and
     * remove them later with removeRigidBody(EntityRef), or retrieve the
     * entities using getPhysicsEntities(), which is not backed hence you can
     * call removeRigibBody while iterating over all elements.
     *
     * @return An iterator that iterates over all entities that have a rigidBody
     * that is active in the physics engine.
     */
    @Override
    public Iterator<EntityRef> physicsEntitiesIterator() {
        return entityRigidBodies.keySet().iterator();
    }

    /**
     * Wakes up any rigid bodies that are in a square around the given position.
     * @param pos The position around which to wake up objects.
     * @param radius the half-length of the sides of the square.
     */
    @Override
    public void awakenArea(Vector3f pos, float radius) {
        Vector3f min = new Vector3f(pos);
        min.sub(new Vector3f(0.6f, 0.6f, 0.6f));
        Vector3f max = new Vector3f(pos);
        max.add(new Vector3f(0.6f, 0.6f, 0.6f));
        discreteDynamicsWorld.awakenRigidBodiesInArea(min, max);
    }
    
    /**
     * The epsilon value is the value that is considered to be so small that it
     * could just as well be zero. Objects that are closer together than this
     * value are assumes to be colliding.
     *
     * @return The simulation epsilon.
     */
    @Override
    public float getEpsilon() {
        return BulletGlobals.SIMD_EPSILON;
    }

    //*******************Private helper methods**************************\\
    private void removeCollider(CollisionObject collider) {
        discreteDynamicsWorld.removeCollisionObject(collider);
    }

    /**
     * Creates a new Collider. Colliders are similar to rigid bodies, except
     * that they do not respond to forces from the physics engine. They collide
     * with other objects and other objects may move if colliding with a
     * collider, but the collider itself will only respond to movement orders
     * from outside the physics engine. Colliders also detect any objects
     * colliding with them. Allowing them to be used as sensors.
     *
     * @param pos The initial position of the collider.
     * @param shape The shape of this collider.
     * @param groups
     * @param filters 
     * @param collisionFlags
     * @param entity The entity to associate this collider with. Can be null.
     * @return The newly created and added to the physics engine, Collider object.
     */
    private CharacterCollider createCustomCollider(Vector3f pos, ConvexShape shape, short groups, short filters, int collisionFlags, EntityRef entity) {
        if(entityColliders.containsKey(entity)) {
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
    private synchronized void processQueuedBodies() {
        while (!insertionQueue.isEmpty()) {
            RigidBodyRequest request = insertionQueue.poll();
            discreteDynamicsWorld.addRigidBody(request.body.rb, request.groups, request.filter);
        }
        while (!removalQueue.isEmpty()) {
            BulletRigidBody body = removalQueue.poll();
            discreteDynamicsWorld.removeRigidBody(body.rb);
        }
    }
    
    /**
     * Applies all pending impulses to the corresponding rigidBodies and clears
     * the pending impulses.
     */
    private void applyPendingImpulses() {
        for (Map.Entry<EntityRef, BulletRigidBody> entree : entityRigidBodies.entrySet()) {
            BulletRigidBody body = entree.getValue();
            body.rb.applyCentralImpulse(body.pendingImpulse);
            body.pendingImpulse.x = 0;
            body.pendingImpulse.y = 0;
            body.pendingImpulse.z = 0;
        }
    }

    private void addRigidBody(BulletRigidBody body) {
        short filter = (short) (CollisionFilterGroups.DEFAULT_FILTER | CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.SENSOR_TRIGGER);
        insertionQueue.add(new RigidBodyRequest(body, CollisionFilterGroups.DEFAULT_FILTER, filter));
    }

    private void addRigidBody(BulletRigidBody body, List<CollisionGroup> groups, List<CollisionGroup> filter) {
        insertionQueue.add(new RigidBodyRequest(body, combineGroups(groups), combineGroups(filter)));
    }

    private void addRigidBody(BulletRigidBody body, short groups, short filter) {
        insertionQueue.add(new RigidBodyRequest(body, groups, (short) (filter | CollisionFilterGroups.SENSOR_TRIGGER)));
    }

    private void removeRigidBody(BulletRigidBody body) {
        removalQueue.add(body);
    }
    
    /**
     * Returns the shape belonging to the given entity. It currently knows 4 
     * different shapes: Sphere, Capsule, Cylinder or arbitrary.
     * The shape is determined based on the shape component of the given entity.
     * If the entity has somehow got multiple shapes, only one is picked. The
     * order of priority is: Sphere, Capsule, Cylinder, arbitrary.
     * 
     * TODO: Flyweight this (take scale as parameter)
     * 
     * @param entity the entity to get the shape of.
     * @return the shape of the entity, ready to be used by Bullet.
     */
    private ConvexShape getShapeFor(EntityRef entity) {
        BoxShapeComponent box = entity.getComponent(BoxShapeComponent.class);
        if (box != null) {
            Vector3f halfExtents = new Vector3f(box.extents);
            halfExtents.scale(0.5f);
            return new BoxShape(halfExtents);
        }
        SphereShapeComponent sphere = entity.getComponent(SphereShapeComponent.class);
        if (sphere != null) {
            return new SphereShape(sphere.radius);
        }
        CapsuleShapeComponent capsule = entity.getComponent(CapsuleShapeComponent.class);
        if (capsule != null) {
            return new CapsuleShape(capsule.radius, capsule.height);
        }
        CylinderShapeComponent cylinder = entity.getComponent(CylinderShapeComponent.class);
        if (cylinder != null) {
            return new CylinderShape(new Vector3f(cylinder.radius, 0.5f * cylinder.height, cylinder.radius));
        }
        HullShapeComponent hull = entity.getComponent(HullShapeComponent.class);
        if (hull != null) {
            ObjectArrayList<Vector3f> verts = new ObjectArrayList<Vector3f>();
            TFloatIterator iterator = hull.sourceMesh.getVertices().iterator();
            while (iterator.hasNext()) {
                Vector3f newVert = new Vector3f();
                newVert.x = iterator.next();
                newVert.y = iterator.next();
                newVert.z = iterator.next();
                verts.add(newVert);
            }
            return new ConvexHullShape(verts);
        }
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        if (characterMovementComponent != null) {
            return new CapsuleShape(characterMovementComponent.radius, characterMovementComponent.height);
        }
        return null;
    }
    
    private void updateKinematicSettings(RigidBodyComponent rigidBody, BulletRigidBody collider) {
        if (rigidBody.kinematic) {
            collider.rb.setCollisionFlags(collider.rb.getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);
            collider.rb.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
        } else {
            collider.rb.setCollisionFlags(collider.rb.getCollisionFlags() & ~CollisionFlags.KINEMATIC_OBJECT);
            collider.rb.setActivationState(CollisionObject.ACTIVE_TAG);
        }
    }
    
    private PairCachingGhostObject createCollider(Vector3f pos, ConvexShape shape, short groups, short filters, int collisionFlags) {
        Transform startTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), pos, 1.0f));
        PairCachingGhostObject result = new PairCachingGhostObject();
        result.setWorldTransform(startTransform);
        result.setCollisionShape(shape);
        result.setCollisionFlags(collisionFlags);
        discreteDynamicsWorld.addCollisionObject(result, groups, filters);
        return result;
    }
    
    //********************Private helper classes*********************\\

    private static class RigidBodyRequest {
        final BulletRigidBody body;
        final short groups;
        final short filter;

        public RigidBodyRequest(BulletRigidBody body, short groups, short filter) {
            this.body = body;
            this.groups = groups;
            this.filter = filter;
        }
    }
   
    private class BulletRigidBody implements RigidBody {
        private final Transform temp = new Transform();
        private final com.bulletphysics.dynamics.RigidBody rb;
        private final Vector3f pendingImpulse = new Vector3f();

        BulletRigidBody(RigidBodyConstructionInfo info) {
            rb = new com.bulletphysics.dynamics.RigidBody(info);
        }

        @Override
        public void applyImpulse(Vector3f impulse) {
            pendingImpulse.add(impulse);
        }

        @Override
        public void translate(Vector3f translation) {
            rb.translate(translation);
        }

        @Override
        public Quat4f getOrientation(Quat4f out) {
            return rb.getOrientation(out);
        }

        @Override
        public Vector3f getLocation(Vector3f out) {
            return rb.getCenterOfMassPosition(out);
        }

        @Override
        public Vector3f getLinearVelocity(Vector3f out) {
            return rb.getLinearVelocity(out);
        }

        @Override
        public Vector3f getAngularVelocity(Vector3f out) {
            return rb.getAngularVelocity(out);
        }

        @Override
        public void setLinearVelocity(Vector3f lin_vel) {
            rb.setLinearVelocity(lin_vel);
        }

        @Override
        public void setAngularVelocity(Vector3f ang_vel) {
            rb.setAngularVelocity(ang_vel);
        }

        @Override
        public void setOrientation(Quat4f orientation) {
            rb.getWorldTransform(temp);
            temp.setRotation(orientation);
            rb.proceedToTransform(temp);
        }

        @Override
        public void setLocation(Vector3f location) {
            rb.getWorldTransform(temp);
            temp.origin.set(location);
            rb.proceedToTransform(temp);
        }

        @Override
        public void setVelocity(Vector3f lin_vel, Vector3f ang_vel) {
            rb.setLinearVelocity(lin_vel);
            rb.setAngularVelocity(ang_vel);
        }

        @Override
        public void setTransform(Vector3f location, Quat4f orientation) {
            rb.getWorldTransform(temp);
            temp.origin.set(location);
            temp.setRotation(orientation);
            rb.proceedToTransform(temp);
        }

        @Override
        public boolean isActive() {
            return rb.isActive();
        }
    }

    private class BulletCharacterMoverCollider implements CharacterCollider {
        private final Transform temp = new Transform();
        
        //If a class can figure out that its Collider is a BulletCollider, it 
        //is allowed to gain direct access to the bullet body:
        private final PairCachingGhostObject collider;

        private BulletCharacterMoverCollider(Vector3f pos, ConvexShape shape, List<CollisionGroup> groups, List<CollisionGroup> filters, EntityRef owner) {
            this(pos, shape, groups, filters, 0, owner);
        }
        
        private BulletCharacterMoverCollider(Vector3f pos, ConvexShape shape, List<CollisionGroup> groups, List<CollisionGroup> filters, int collisionFlags, EntityRef owner) {
            this(pos, shape, combineGroups(groups), combineGroups(filters), collisionFlags, owner);
        }
        
        private BulletCharacterMoverCollider(Vector3f pos, ConvexShape shape, short groups, short filters, int collisionFlags, EntityRef owner) {
            collider = createCollider(pos, shape, groups, filters, collisionFlags);
            collider.setUserPointer(owner);
        }
        
        @Override
        public Vector3f getLocation(Vector3f out) {
            collider.getWorldTransform(temp);
            return temp.origin;
        }

        @Override
        public Quat4f getOrientation(Quat4f out) {
            collider.getWorldTransform(temp);
            return temp.getRotation(out);
        }

        @Override
        public void setOrientation(Quat4f orientation) {
            collider.getWorldTransform(temp);
            temp.setRotation(orientation);
            collider.setWorldTransform(temp);
        }

        @Override
        public void setLocation(Vector3f loc) {
            collider.getWorldTransform(temp);
            temp.origin.set(loc);
            collider.setWorldTransform(temp);
        }

        @Override
        public void setTransform(Vector3f loc, Quat4f orientation) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public BulletSweepCallback sweep(Vector3f startPos, Vector3f endPos, float allowedPenetration, float slopeFactor) {
            Transform startTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), startPos, 1.0f));
            Transform endTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), endPos, 1.0f));
            BulletSweepCallback callback = new BulletSweepCallback(collider, new Vector3f(0, 1, 0), slopeFactor);
            callback.collisionFilterGroup = collider.getBroadphaseHandle().collisionFilterGroup;
            callback.collisionFilterMask = collider.getBroadphaseHandle().collisionFilterMask;
            collider.convexSweepTest((ConvexShape) (collider.getCollisionShape()), startTransform, endTransform, callback, allowedPenetration);
            return callback;
        }
    }
}

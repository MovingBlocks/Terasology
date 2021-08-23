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
import org.terasology.engine.config.Config;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.location.LocationResynchEvent;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.HitResult;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.physics.components.RigidBodyComponent;
import org.terasology.engine.physics.components.TriggerComponent;
import org.terasology.engine.physics.components.shapes.BoxShapeComponent;
import org.terasology.engine.physics.components.shapes.CapsuleShapeComponent;
import org.terasology.engine.physics.components.shapes.CylinderShapeComponent;
import org.terasology.engine.physics.components.shapes.HullShapeComponent;
import org.terasology.engine.physics.components.shapes.SphereShapeComponent;
import org.terasology.engine.physics.engine.CharacterCollider;
import org.terasology.engine.physics.engine.RigidBody;
import org.terasology.engine.physics.engine.SweepCallback;
import org.terasology.engine.physics.events.BlockImpactEvent;
import org.terasology.engine.physics.events.ChangeVelocityEvent;
import org.terasology.engine.physics.events.CollideEvent;
import org.terasology.engine.physics.events.EntityImpactEvent;
import org.terasology.engine.physics.events.ForceEvent;
import org.terasology.engine.physics.events.ImpactEvent;
import org.terasology.engine.physics.events.ImpulseEvent;
import org.terasology.engine.physics.events.PhysicsResynchEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.OnChangedBlock;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.joml.geom.AABBf;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Physics engine implementation using TeraBullet (a customised version of JBullet).
 */
@RegisterSystem
@Share(Physics.class)
public class BulletPhysics extends BaseComponentSystem implements UpdateSubscriberSystem, Physics {
    public static final int AABB_SIZE = Integer.MAX_VALUE;

    public static final float SIMD_EPSILON = 1.1920929E-7F;

    private static final Logger logger = LoggerFactory.getLogger(BulletPhysics.class);

    private final btCollisionDispatcher dispatcher;
    private final btBroadphaseInterface broadphase;
    private final btDiscreteDynamicsWorld discreteDynamicsWorld;

    private Map<EntityRef, btRigidBody> entityRigidBodies = Maps.newHashMap();
    private Map<EntityRef, BulletCharacterMoverCollider> entityColliders = Maps.newHashMap();
    private Map<EntityRef, btPairCachingGhostObject> entityTriggers = Maps.newHashMap();
    private btPersistentManifoldArray manifolds = new btPersistentManifoldArray();

    private final btCollisionConfiguration defaultCollisionConfiguration;
    private final btSequentialImpulseConstraintSolver sequentialImpulseConstraintSolver;

    private final btGhostPairCallback ghostPairCallback;
    private long lastNetsync;

    @In
    protected BlockEntityRegistry blockEntityRegistry;
    @In
    protected Config config;
    @In
    protected AssetManager assetManager;
    @In
    protected Time time;
    @In
    protected NetworkSystem networkSystem;
    @In
    protected WorldProvider worldProvider;

    /**
     * Creates a Collider for the given entity based on the LocationComponent and CharacterMovementComponent. All
     * collision flags are set right for a character movement component.
     *
     * @param owner the entity to create the collider for.
     * @return
     */
    private ArrayList<btConvexShape> shapes = Lists.newArrayList();

    @Override
    public void initialise() {
        super.initialise();
    }

    public BulletPhysics() {
        ghostPairCallback = new btGhostPairCallback();

        broadphase = new btDbvtBroadphase();
        defaultCollisionConfiguration = new btDefaultCollisionConfiguration();

        dispatcher = new btCollisionDispatcher(defaultCollisionConfiguration);
        sequentialImpulseConstraintSolver = new btSequentialImpulseConstraintSolver();
        discreteDynamicsWorld =
                new btDiscreteDynamicsWorld(dispatcher, broadphase, sequentialImpulseConstraintSolver, defaultCollisionConfiguration);
        discreteDynamicsWorld.setGravity(new Vector3f(0f, -Physics.GRAVITY, 0f));
        discreteDynamicsWorld.getBroadphase().getOverlappingPairCache().setInternalGhostPairCallback(ghostPairCallback);
    }


    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void newRigidBody(OnActivatedComponent event, EntityRef entity, RigidBodyComponent rigidBody, LocationComponent location) {
        entityRigidBodies.computeIfAbsent(entity, e -> {
            btCollisionShape shape = getShapeFor(e);
            if (location != null && rigidBody != null && shape != null) {
                float scale = location.getWorldScale();

                if (rigidBody.mass < 1) {
                    logger.warn("RigidBodyComponent.mass is set to less than 1.0, this can lead to strange behaviour, " +
                            "such as the objects moving through walls. " +
                            "Entity: {}", e);
                }
                Vector3f inertia = new Vector3f();
                shape.calculateLocalInertia(rigidBody.mass, inertia);
                shape.setLocalScaling(new Vector3f(scale, scale, scale));

                btRigidBody.btRigidBodyConstructionInfo info =
                        new btRigidBody.btRigidBodyConstructionInfo(rigidBody.mass,
                                new EntityMotionState(e),
                                shape,
                                inertia);
                btRigidBody bt = new btRigidBody(info);
                info.dispose();

                Vector3f temp = new Vector3f();
                bt.setAngularFactor(temp.set(rigidBody.angularFactor));
                bt.setLinearFactor(temp.set(rigidBody.linearFactor));
                bt.setFriction(rigidBody.friction);

                bt.setAngularVelocity(rigidBody.angularVelocity);
                bt.setLinearVelocity(rigidBody.velocity);
                bt.userData = e;

                bt.setWorldTransform(
                        new Matrix4f()
                                .translationRotateScale(
                                        location.getWorldPosition(new Vector3f()),
                                        location.getWorldRotation(new Quaternionf()),
                                        1.0f));

                discreteDynamicsWorld.addRigidBody(bt,
                        CollisionGroup.combineGroups(rigidBody.collisionGroup),
                        CollisionGroup.combineGroups(rigidBody.collidesWith));
                return bt;
            } else if (shape != null) {
                shape.dispose();
            }
            return null;
        });
    }



    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void updateRigidBody(OnChangedComponent event, EntityRef entity, RigidBodyComponent body, LocationComponent location) {
        btRigidBody rigidBody = entityRigidBodies.get(entity);
        if (rigidBody != null) {
            if (body.kinematic) {
                rigidBody.setCollisionFlags(rigidBody.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
                rigidBody.setActivationState(Collision.DISABLE_DEACTIVATION);
            } else {
                rigidBody.setCollisionFlags(rigidBody.getCollisionFlags() & ~btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
                rigidBody.setActivationState(Collision.ACTIVE_TAG);
            }
            rigidBody.setAngularFactor(rigidBody.getAngularFactor());
            rigidBody.setRestitution(rigidBody.getRestitution());
            rigidBody.setLinearFactor(rigidBody.getLinearFactor());
            rigidBody.setFriction(rigidBody.getFriction());

            discreteDynamicsWorld.removeRigidBody(rigidBody);
            discreteDynamicsWorld.addRigidBody(rigidBody,
                    CollisionGroup.combineGroups(body.collisionGroup),
                    CollisionGroup.combineGroups(body.collidesWith));
        } else {
            newRigidBody(null, entity, body, location);
        }
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void removeRigidBody(BeforeDeactivateComponent event, EntityRef entity) {
        entityRigidBodies.computeIfPresent(entity, (entityRef, rb) -> {
            Matrix4f m = new Matrix4f();
            Vector3f aabbMin = new Vector3f();
            Vector3f aabbMax = new Vector3f();
            rb.getCollisionShape().getAabb(m, aabbMin, aabbMax);
            awakenArea(rb.getWorldTransform().getTranslation(new Vector3f()), (aabbMax.sub(aabbMin)).length() * .5f);

            discreteDynamicsWorld.removeRigidBody(rb);
            rb.dispose();
            return null;
        });
    }

    @ReceiveEvent(components = RigidBodyComponent.class)
    public void onImpulse(ImpulseEvent event, EntityRef entity) {
        btRigidBody bt = entityRigidBodies.get(entity);
        if (bt != null) {
            bt.applyCentralImpulse(event.getImpulse());
        }
    }

    @ReceiveEvent(components = RigidBodyComponent.class)
    public void onForce(ForceEvent event, EntityRef entity) {
        btRigidBody bt = entityRigidBodies.get(entity);
        if (bt != null) {
            bt.applyCentralForce(event.getForce());
        }
    }

    @ReceiveEvent(components = RigidBodyComponent.class)
    public void onChangeVelocity(ChangeVelocityEvent event, EntityRef entity) {
        btRigidBody bt = entityRigidBodies.get(entity);
        if (bt != null) {
            if (event.getAngularVelocity() != null) {
                bt.setAngularVelocity(event.getAngularVelocity());
            }
            if (event.getLinearVelocity() != null) {
                bt.setLinearVelocity(event.getLinearVelocity());
            }
        }
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void onBlockAltered(OnChangedBlock event, EntityRef entity) {
        awakenArea(new Vector3f(event.getBlockPosition()), 0.6f);
    }


    @ReceiveEvent(components = {LocationComponent.class, RigidBodyComponent.class})
    public void onItemImpact(ImpactEvent event, EntityRef entity) {
        btRigidBody rb = entityRigidBodies.get(entity);
        if (rb != null) {
            Vector3f vImpactNormal = new Vector3f(event.getImpactNormal());
            Vector3f vImpactPoint = new Vector3f(event.getImpactPoint());
            Vector3f vImpactSpeed = new Vector3f(event.getImpactSpeed());

            float speedFactor = vImpactSpeed.length();
            vImpactNormal.normalize();
            vImpactSpeed.normalize();

            float dotImpactNormal = vImpactSpeed.dot(vImpactNormal);

            Vector3f impactResult = vImpactNormal.mul(dotImpactNormal);
            impactResult = vImpactSpeed.sub(impactResult.mul(2.0f));
            impactResult.normalize();

            Vector3f vNewLocationVector = (new Vector3f(impactResult)).mul(event.getTravelDistance());
            Vector3f vNewPosition = (new Vector3f(vImpactPoint)).add(vNewLocationVector);
            Vector3f vNewVelocity = (new Vector3f(impactResult)).mul(speedFactor * COLLISION_DAMPENING_MULTIPLIER);

            Matrix4f translation = rb.getWorldTransform();
            Quaternionf quaternion = new Quaternionf().setFromUnnormalized(translation);

            rb.setWorldTransform(new Matrix4f().translationRotateScale(vNewPosition, quaternion, 1.0f));
            rb.setLinearVelocity(vNewVelocity);
            rb.setAngularVelocity(vNewVelocity);
        }
    }


    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    public void newTrigger(OnActivatedComponent event, EntityRef entity, LocationComponent location, TriggerComponent trigger) {
        entityTriggers.computeIfAbsent(entity, e -> {
            btCollisionShape shape = getShapeFor(entity);
            if (shape != null && location != null && trigger != null) {
                float scale = location.getWorldScale();
                shape.setLocalScaling(new Vector3f(scale, scale, scale));

                btPairCachingGhostObject result = new btPairCachingGhostObject();
                Matrix4f startTransform = new Matrix4f().translation(location.getWorldPosition(new Vector3f()));
                result.setWorldTransform(startTransform);
                result.setCollisionShape(shape);
                result.setCollisionFlags(btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
                result.userData = e;

                discreteDynamicsWorld.addCollisionObject(result,
                        trigger.collisionGroup.getFlag(),
                        CollisionGroup.combineGroups(trigger.detectGroups));

                return result;
            } else if(shape != null) {
                shape.dispose();
            }
            return null;
        });
    }

    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    public void updateTrigger(OnChangedComponent event, EntityRef entity, TriggerComponent trigger, LocationComponent location) {
        btPairCachingGhostObject bt = entityTriggers.get(entity);
        if (bt != null) {
            Quaternionf worldRotation = location.getWorldRotation(new Quaternionf());
            Vector3f position = location.getWorldPosition(new Vector3f());
            if (!position.isFinite() || !worldRotation.isFinite()) {
                logger.warn("Can't update Trigger entity with a non-finite position/rotation?! Entity: {}", entity);
                return;
            }
            bt.setWorldTransform(new Matrix4f().translationRotateScale(position, worldRotation, 1.0f));
        } else {
            newTrigger(null, entity, location, trigger);
        }
    }


    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    public void removeTrigger(BeforeDeactivateComponent event, EntityRef entity) {
        entityTriggers.computeIfPresent(entity, (entityRef, collider) -> {
            discreteDynamicsWorld.removeCollisionObject(collider);
            collider.dispose();
            return null;
        });
    }

    @Override
    public void shutdown() {
        super.shutdown();
        this.discreteDynamicsWorld.dispose();
        this.dispatcher.dispose();
        this.defaultCollisionConfiguration.dispose();
        this.entityTriggers.forEach((k, v) -> v.dispose());
        this.entityRigidBodies.forEach((k, v) -> v.dispose());
        this.ghostPairCallback.dispose();
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
                CollisionGroup.combineGroups(collisionFilter), btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);


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

        short filter = CollisionGroup.combineGroups(collisionGroups);

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
                    logger.warn("Unidentified object was hit in the physics engine: {}", collisionObject.userData);
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

        short filter = CollisionGroup.combineGroups(collisionGroups);

        // lookup all the collision item ids for these entities
        Set<Integer> excludedCollisionIds = Sets.newHashSet();
        for (EntityRef excludedEntity : excludedEntities) {
            if (entityRigidBodies.containsKey(excludedEntity)) {
                excludedCollisionIds.add(entityRigidBodies.get(excludedEntity).getBroadphaseHandle().getUid());
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
                        logger.warn("Unidentified object was hit in the physics engine: {}", collisionObject.userData);
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

        PerformanceMonitor.startActivity("Physics Update");
        updateCollisionPairs();
        if (networkSystem.getMode().isServer() && time.getGameTimeInMs() - TIME_BETWEEN_NETSYNCS > lastNetsync) {
            sendSyncMessages();
            lastNetsync = time.getGameTimeInMs();
        }
        Matrix4f transform = new Matrix4f();
        entityRigidBodies.forEach((entity, rigidBody) -> {
            RigidBodyComponent comp = entity.getComponent(RigidBodyComponent.class);
            TriggerComponent trigger = entity.getComponent(TriggerComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);

            // force location component to update and sync trigger state
            if (trigger != null && location != null) {
                updateTrigger(null, entity, trigger, location);
            }

            if (rigidBody.isActive()) {
                comp.velocity.set(rigidBody.getLinearVelocity());
                comp.angularVelocity.set(rigidBody.getAngularVelocity());

                rigidBody.getWorldTransform(transform);

                Vector3f vLocation = transform.getTranslation(new Vector3f());

                Vector3f vDirection = new Vector3f(comp.velocity);
                float fDistanceThisFrame = vDirection.length();
                vDirection.normalize();

                fDistanceThisFrame = fDistanceThisFrame * delta;

                while (true) {
                    HitResult hitInfo = rayTrace(vLocation, vDirection, fDistanceThisFrame + 0.5f, DEFAULT_COLLISION_GROUP);
                    if (hitInfo.isHit()) {
                        Block hitBlock = worldProvider.getBlock(hitInfo.getBlockPosition());
                        if (hitBlock != null) {
                            Vector3f vTravelledDistance = vLocation.sub(hitInfo.getHitPoint());
                            float fTravelledDistance = vTravelledDistance.length();
                            if (fTravelledDistance > fDistanceThisFrame) {
                                break;
                            }
                            if (hitBlock.isPenetrable()) {
                                if (!hitInfo.getEntity().hasComponent(BlockComponent.class)) {
                                    entity.send(new EntityImpactEvent(hitInfo.getHitPoint(), hitInfo.getHitNormal(), comp.velocity,
                                            fDistanceThisFrame, hitInfo.getEntity()));
                                    break;
                                }
                                // decrease the remaining distance to check if we hit a block
                                fDistanceThisFrame = fDistanceThisFrame - fTravelledDistance;
                                vLocation = hitInfo.getHitPoint();
                            } else {
                                entity.send(new BlockImpactEvent(hitInfo.getHitPoint(), hitInfo.getHitNormal(), comp.velocity,
                                        fDistanceThisFrame, hitInfo.getEntity()));
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        });

        PerformanceMonitor.endActivity();
    }

    private void sendSyncMessages() {
        Vector3f tempPosition = new Vector3f();
        Quaternionf tempRotation = new Quaternionf();
        entityRigidBodies.forEach((entity, body) -> {
            if (entity.hasComponent(NetworkComponent.class)) {
                //TODO after implementing rigidbody interface
                if (body.isActive()) {
                    Matrix4f transform = body.getWorldTransform();

                    entity.send(new LocationResynchEvent( transform.getTranslation(tempPosition), tempRotation.setFromUnnormalized(transform)));
                    entity.send(new PhysicsResynchEvent(body.getLinearVelocity(), body.getAngularVelocity()));
                }
            }
        });
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class}, netFilter = RegisterMode.REMOTE_CLIENT)
    public void resynchPhysics(PhysicsResynchEvent event, EntityRef entity) {
        btRigidBody rb = entityRigidBodies.get(entity);
        if (rb != null) {
            rb.setLinearVelocity(event.getVelocity());
            rb.setAngularVelocity(event.getAngularVelocity());
        }
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class}, netFilter = RegisterMode.REMOTE_CLIENT)
    public void resynchLocation(LocationResynchEvent event, EntityRef entity) {
        btRigidBody rb = entityRigidBodies.get(entity);
        if (rb != null) {
            Matrix4f transform = new Matrix4f();
            transform.set(new Matrix4f().translationRotateScale(event.getPosition(), event.getRotation(), 1.0f));
            rb.setWorldTransform(transform);
        }
    }

    private void updateCollisionPairs() {
        discreteDynamicsWorld.getCollisionWorld().performDiscreteCollisionDetection();

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
                            int l = manifoldPoint.getLifeTime();

                            Vector3f a3 = new Vector3f();
                            manifoldPoint.getNormalWorldOnB(a3);


                            if (otherEntity.exists()) {
                                short bCollisionGroup = getCollisionGroupFlag(otherEntity);
                                short aCollidesWith = getCollidesWithGroupFlag(entity);
                                if ((bCollisionGroup & aCollidesWith) != 0
                                        || (otherEntity.hasComponent(BlockComponent.class) && !entity.hasComponent(BlockComponent.class))) {
                                    entity.send(new CollideEvent(otherEntity, a1, a2, manifoldPoint.getDistance(), a3));
                                }
                            }
                            if (entity.exists()) {
                                short aCollisionGroup = getCollisionGroupFlag(entity);
                                short bCollidesWith = getCollidesWithGroupFlag(otherEntity);
                                if ((aCollisionGroup & bCollidesWith) != 0
                                        || (entity.hasComponent(BlockComponent.class) && !otherEntity.hasComponent(BlockComponent.class))) {
                                    otherEntity.send(new CollideEvent(entity, a2, a1, manifoldPoint.getDistance(),
                                            new Vector3f(a3).mul(-1.0f)));
                                }
                            }
                            break;
                        }
                    }
                }
                btBroadphaseProxy.free(p0);
                btBroadphaseProxy.free(p1);

            }
        }
    }

    private short getCollisionGroupFlag(EntityRef entity) {
        CollisionGroup collisionGroup = StandardCollisionGroup.NONE;
        if (entity.hasComponent(TriggerComponent.class)) {
            TriggerComponent entityTrigger = entity.getComponent(TriggerComponent.class);
            collisionGroup = entityTrigger.collisionGroup;
        } else if (entity.hasComponent(RigidBodyComponent.class)) {
            RigidBodyComponent entityRigidBody = entity.getComponent(RigidBodyComponent.class);
            collisionGroup = entityRigidBody.collisionGroup;
        }
        return collisionGroup.getFlag();
    }

    private short getCollidesWithGroupFlag(EntityRef entity) {
        List<CollisionGroup> collidesWithGroup = Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.NONE);
        if (entity.hasComponent(TriggerComponent.class)) {
            TriggerComponent entityTrigger = entity.getComponent(TriggerComponent.class);
            collidesWithGroup = entityTrigger.detectGroups;
        } else if (entity.hasComponent(RigidBodyComponent.class)) {
            RigidBodyComponent entityRigidBody = entity.getComponent(RigidBodyComponent.class);
            collidesWithGroup = entityRigidBody.collidesWith;
        }
        return CollisionGroup.combineGroups(collidesWithGroup);
    }



//    @Override
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
                CollisionGroup.combineGroups(movementComp.collidesWith),
                btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT, owner);
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
        return null;
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


    /**
     * INTERNAL class use at your own risk.
     * @return
     */
    public btDiscreteDynamicsWorld getDiscreteDynamicsWorld() {
        return this.discreteDynamicsWorld;
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
            this(pos, shape, CollisionGroup.combineGroups(groups), CollisionGroup.combineGroups(filters), collisionFlags, owner);
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

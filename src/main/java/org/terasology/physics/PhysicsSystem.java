/*
 * Copyright 2012
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

package org.terasology.physics;

import com.bulletphysics.BulletGlobals;
import gnu.trove.iterator.TFloatIterator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.newdawn.slick.tests.DistanceFieldTest;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventPriority;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.math.AABB;
import org.terasology.math.Vector3fUtil;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.physics.shapes.CapsuleShapeComponent;
import org.terasology.physics.shapes.CylinderShapeComponent;
import org.terasology.physics.shapes.HullShapeComponent;
import org.terasology.physics.shapes.SphereShapeComponent;

import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalConvexResult;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalShapeInfo;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.ConvexResultCallback;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.DescriptorProtos.SourceCodeInfo.Location;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class PhysicsSystem implements EventHandlerSystem, UpdateSubscriberSystem {

    private BulletPhysics physics;
    private Map<EntityRef, RigidBody> entityRigidBodies = Maps.newHashMap();
    private Map<EntityRef, PairCachingGhostObject> entityTriggers = Maps.newHashMap();
    List<CollisionPair> lastCollisionPairs = Lists.newArrayList();
    private int skipProcessingFrames = 4;

    @Override
    public void initialise() {
        physics = CoreRegistry.get(BulletPhysics.class);
        skipProcessingFrames = 4;
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void newRigidBody(AddComponentEvent event, EntityRef entity) {
        createRigidBody(entity);
    }

    private void createRigidBody(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        RigidBodyComponent rigidBody = entity.getComponent(RigidBodyComponent.class);
        ConvexShape shape = getShapeFor(entity);
        if (shape != null) {
            float scale = location.getWorldScale();
           shape.setLocalScaling(new Vector3f(scale, scale, scale));
            
            Vector3f fallInertia = new Vector3f();
            shape.calculateLocalInertia(rigidBody.mass, fallInertia);
            RigidBodyConstructionInfo info = new RigidBodyConstructionInfo(rigidBody.mass, new EntityMotionState(entity), shape, fallInertia);
            RigidBody collider = new RigidBody(info);
            
            collider.setFriction(rigidBody.friction);
            collider.setRestitution(rigidBody.restitution);
            collider.setAngularVelocity(new Vector3f(rigidBody.velocity,rigidBody.velocity,rigidBody.velocity));
            collider.setAngularFactor(rigidBody.velocity);
            collider.setLinearVelocity(new Vector3f(rigidBody.velocity, rigidBody.velocity, rigidBody.velocity));
            collider.setGravity(new Vector3f(rigidBody.gravity, rigidBody.gravity, rigidBody.gravity));
            collider.setDamping(rigidBody.damping, rigidBody.damping);
            collider.setCcdMotionThreshold(rigidBody.ccdMotionThreshold);
            collider.setCcdSweptSphereRadius(rigidBody.ccdSweptSphereRadius);
            collider.setUserPointer(entity);
            
   
            RigidBody oldBody = entityRigidBodies.put(entity, collider);
            physics.addRigidBody(collider, Lists.<CollisionGroup>newArrayList(rigidBody.collisionGroup), rigidBody.collidesWith);
            if (oldBody != null) {
                physics.removeRigidBody(oldBody);
            }
        }
    }

    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    public void newTrigger(AddComponentEvent event, EntityRef entity) {
        createTrigger(entity);
    }

    private void createTrigger(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        TriggerComponent trigger = entity.getComponent(TriggerComponent.class);
        ConvexShape shape = getShapeFor(entity);
        if (shape != null) {
            float scale = location.getWorldScale();
            shape.setLocalScaling(new Vector3f(scale, scale, scale));
            List<CollisionGroup> detectGroups = Lists.newArrayList(trigger.detectGroups);
            PairCachingGhostObject triggerObj = physics.createCollider(location.getWorldPosition(), shape, Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.SENSOR), detectGroups, CollisionFlags.NO_CONTACT_RESPONSE);
            triggerObj.setUserPointer(entity);
            entityTriggers.put(entity, triggerObj);
        }
    }

    @ReceiveEvent(components = {RigidBodyComponent.class})
    public void onImpulse(ImpulseEvent event, EntityRef entity) {
        RigidBody body = entityRigidBodies.get(entity);
        if (body != null) {
            body.applyCentralImpulse(event.getImpulse());
        }
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void removeRigidBody(RemovedComponentEvent event, EntityRef entity) {
        RigidBody body = entityRigidBodies.remove(entity);
        if (body != null) {
            physics.removeRigidBody(body);
        }
    }

    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    public void removeTrigger(RemovedComponentEvent event, EntityRef entity) {
        GhostObject ghost = entityTriggers.remove(entity);
        if (ghost != null) {
            physics.removeCollider(ghost);
        }
    }

    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    public void updateTrigger(ChangedComponentEvent event, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        PairCachingGhostObject triggerObj = entityTriggers.get(entity);

        if (triggerObj != null) {
            float scale = location.getWorldScale();
            if (Math.abs(triggerObj.getCollisionShape().getLocalScaling(new Vector3f()).x - scale) > BulletGlobals.SIMD_EPSILON) {
            	physics.removeCollider(triggerObj);
                createTrigger(entity);
            } else {
                triggerObj.setWorldTransform(new Transform(new Matrix4f(location.getWorldRotation(), location.getWorldPosition(), 1.0f)));
            }
        }

        // TODO: update if detectGroups changed
    }
    
    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void updateRigidBody(ChangedComponentEvent event, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        RigidBodyComponent rigid = entity.getComponent(RigidBodyComponent.class);
        RigidBody rigidBody = entityRigidBodies.get(entity);

        if (rigidBody != null) {
            float scale = location.getWorldScale();
            float mass = 1/rigidBody.getInvMass();           
            BroadphaseProxy broadphaseProxy = rigidBody.getBroadphaseProxy();
            short rigidFlag = rigid.collisionGroup.getFlag();
            if (Math.abs(rigidBody.getCollisionShape().getLocalScaling(new Vector3f()).x - scale) > BulletGlobals.SIMD_EPSILON
            	|| mass != rigid.mass 
            	|| (broadphaseProxy != null && broadphaseProxy.collisionFilterGroup != rigidFlag)) {
            	physics.removeRigidBody(rigidBody);
                createRigidBody(entity);
            }
        }

        // TODO: update if mass or collision groups change
    }

    // TODO: Flyweight this (take scale as parameter)
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
   
    private SweepCallback sweep(Vector3f from, Vector3f to, GhostObject collider, float slopeFactor, float allowedPenetration) {
        Transform startTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), from, 1.0f));
        Transform endTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), to, 1.0f));
        SweepCallback callback = new SweepCallback(collider, new Vector3f(0, 1, 0), slopeFactor);
        callback.collisionFilterGroup = collider.getBroadphaseHandle().collisionFilterGroup;
        callback.collisionFilterMask = collider.getBroadphaseHandle().collisionFilterMask;
        collider.convexSweepTest((ConvexShape) (collider.getCollisionShape()), startTransform, endTransform, callback, allowedPenetration);
        return callback;
    }
    
    private static class SweepCallback extends CollisionWorld.ClosestConvexResultCallback {
        protected CollisionObject me;
        protected final Vector3f up;
        protected float minSlopeDot;

        public SweepCallback(CollisionObject me, final Vector3f up, float minSlopeDot) {
            super(new Vector3f(), new Vector3f());
            this.me = me;
            this.up = up;
            this.minSlopeDot = minSlopeDot;
        }

        @Override
        public float addSingleResult(CollisionWorld.LocalConvexResult convexResult, boolean normalInWorldSpace) {
            if (convexResult.hitCollisionObject == me) {
                return 1.0f;
            }

            return super.addSingleResult(convexResult, normalInWorldSpace);
        }
    }
 
    /**
     * Performs a sweep collision test and returns the results as a list of HitResult<br/>
     * You have to use different Transforms for start and end (at least distance > 0.4f).
     * SweepTest will not see a collision if it starts INSIDE an object and is moving AWAY from its center.
     */
    public List<HitResult> sweepTest(CollisionObject shape, Transform start, Transform end) {
        List<HitResult> results = new LinkedList<HitResult>();
        CollisionShape cshape = shape.getCollisionShape();
        if (cshape == null || !(cshape instanceof ConvexShape)) {
            return results;
        }
//        InternalSweepListener callback =  new InternalSweepListener(results);
//        callback.collisionFilterGroup = shape.getBroadphaseHandle().collisionFilterGroup;
//        callback.collisionFilterMask = shape.getBroadphaseHandle().collisionFilterMask;
        physics.getWorld().convexSweepTest((ConvexShape) cshape, start, end, new InternalSweepListener(results));
        return results;
    }

    private class InternalSweepListener extends CollisionWorld.ConvexResultCallback {
        private List<HitResult> results;
        public InternalSweepListener(List<HitResult> results) {
            this.results = results;
        }
        @Override
        public float addSingleResult(LocalConvexResult lcr, boolean bln) {
        	if( lcr.hitCollisionObject.getUserPointer() instanceof EntityRef){
        		EntityRef entity = (EntityRef) lcr.hitCollisionObject.getUserPointer();
        		results.add(new HitResult(entity, lcr.hitPointLocal, lcr.hitNormalLocal));
        	}
            return lcr.hitFraction;
        }
    }
    
    @Override
    public void update(float delta) {
        // TODO: This shouldn't be necessary once this is correctly sequenced after the main physics update
        if (skipProcessingFrames > 0) {
            skipProcessingFrames--;
            return;
        }
        
        List<CollisionPair> collisionPairs = Lists.newArrayList();
        DynamicsWorld world = physics.getWorld();
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
                            collisionPairs.add(new CollisionPair(entity, otherEntity, manifoldPoint));
                            break;
                        }
                    }
                }
            }
        }
        
        for (CollisionPair pair : collisionPairs) {
            if (pair.b.exists()) {
                pair.a.send(new CollideEvent(pair.b,pair.manifoldPoint.positionWorldOnB, pair.manifoldPoint.normalWorldOnB));
            }
            if (pair.a.exists()) {
                pair.b.send(new CollideEvent(pair.a,pair.manifoldPoint.positionWorldOnB, pair.manifoldPoint.normalWorldOnB));
            }
        }

        lastCollisionPairs.removeAll(collisionPairs);
        for(CollisionPair pair : lastCollisionPairs){
            if (pair.b.exists()) {
            	pair.a.send(new ExitCollisionEvent(pair.b));
            }
            if (pair.a.exists()) {
            	pair.b.send(new ExitCollisionEvent(pair.a));
            }
        }
        lastCollisionPairs = collisionPairs;
    }

    private static class CollisionPair {
        EntityRef a;
        EntityRef b;
        ManifoldPoint manifoldPoint;
        
        public CollisionPair(EntityRef a, EntityRef b, ManifoldPoint manifold) {
            this.a = a;
            this.b = b;
            this.manifoldPoint = manifold;
        }
    }

    private class EntityMotionState extends MotionState {
        private EntityRef entity;

        public EntityMotionState(EntityRef entity) {
            this.entity = entity;
        }

        @Override
        public Transform getWorldTransform(Transform transform) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            if (loc != null) {
                // NOTE: JBullet ignores scale anyway
                transform.set(new Matrix4f(loc.getWorldRotation(), loc.getWorldPosition(), 1));
            }
            return transform;
        }

        @Override
        public void setWorldTransform(Transform transform) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            if (loc != null) {
                loc.setWorldPosition(transform.origin);
                loc.setWorldRotation(transform.getRotation(new Quat4f()));
                entity.saveComponent(loc);
            }
        }
    }
}

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

import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.google.common.collect.Maps;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.rendering.physics.BulletPhysicsRenderer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Map;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class PhysicsSystem implements EventHandlerSystem, UpdateSubscriberSystem {

    private BulletPhysicsRenderer physics;
    private Map<EntityRef, RigidBody> entityRigidBodies = Maps.newHashMap();
    private Map<EntityRef, PairCachingGhostObject> entityTriggers = Maps.newHashMap();

    @Override
    public void initialise() {
        physics = CoreRegistry.get(BulletPhysicsRenderer.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void newRigidBody(AddComponentEvent event, EntityRef entity) {
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
            collider.setUserPointer(entity);
            RigidBody oldBody = entityRigidBodies.put(entity, collider);
            physics.addRigidBody(collider, CollisionFilterGroups.DEFAULT_FILTER, (short) (CollisionFilterGroups.DEFAULT_FILTER | CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.KINEMATIC_FILTER | CollisionFilterGroups.SENSOR_TRIGGER));
            if (oldBody != null) {
                physics.removeRigidBody(oldBody);
            }
        }
    }

    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    public void newTrigger(AddComponentEvent event, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        TriggerComponent trigger = entity.getComponent(TriggerComponent.class);
        ConvexShape shape = getShapeFor(entity);
        if (shape != null) {
            float scale = location.getWorldScale();
            shape.setLocalScaling(new Vector3f(scale, scale, scale));
            PairCachingGhostObject triggerObj = physics.createCollider(location.getWorldPosition(), shape, CollisionFilterGroups.SENSOR_TRIGGER, (short) (CollisionFilterGroups.DEFAULT_FILTER | CollisionFilterGroups.SENSOR_TRIGGER), CollisionFlags.NO_CONTACT_RESPONSE);
            triggerObj.setUserPointer(entity);
            entityTriggers.put(entity, triggerObj);
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

    private ConvexShape getShapeFor(EntityRef entity) {
        BoxShapeComponent box = entity.getComponent(BoxShapeComponent.class);
        if (box != null) {
            Vector3f halfExtents = new Vector3f(box.extents);
            halfExtents.scale(0.5f);
            return new BoxShape(halfExtents);
        }
        return null;
    }

    @Override
    public void update(float delta) {
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
                            entity.send(new CollideEvent(otherEntity));
                            otherEntity.send(new CollideEvent(entity));
                            break;
                        }
                    }
                }
            }


            LocationComponent location = entity.getComponent(LocationComponent.class);
            if (location != null) {
                trigger.setWorldTransform(new Transform(new Matrix4f(location.getWorldRotation(), location.getWorldPosition(), 1.0f)));
            }
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

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

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
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
public class PhysicsSystem implements EventHandlerSystem {

    private BulletPhysicsRenderer physics;
    private Map<EntityRef, RigidBody> entityRigidBodies = Maps.newHashMap();

    @Override
    public void initialise() {
        physics = CoreRegistry.get(BulletPhysicsRenderer.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components={RigidBodyComponent.class, LocationComponent.class})
    public void newRigidBody(AddComponentEvent event, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        RigidBodyComponent rigidBody = entity.getComponent(RigidBodyComponent.class);
        ConvexShape shape = getShapeFor(entity);
        float scale = location.getWorldScale();
        shape.setLocalScaling(new Vector3f(scale,scale,scale));
        if (shape != null) {
            Vector3f fallInertia = new Vector3f();
            shape.calculateLocalInertia(rigidBody.mass, fallInertia);
            RigidBodyConstructionInfo info = new RigidBodyConstructionInfo(rigidBody.mass, new EntityMotionState(entity), shape, fallInertia);
            RigidBody collider = new RigidBody(info);
            collider.setUserPointer(entity);
            RigidBody oldBody = entityRigidBodies.put(entity, collider);
            physics.addRigidBody(collider, CollisionFilterGroups.DEFAULT_FILTER, (short)(CollisionFilterGroups.DEFAULT_FILTER | CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.KINEMATIC_FILTER));
            if (oldBody != null) {
                physics.removeRigidBody(oldBody);
            }
        }
    }

    @ReceiveEvent(components={RigidBodyComponent.class, LocationComponent.class})
    public void removeRigidBody(RemovedComponentEvent event, EntityRef entity) {
        RigidBody body = entityRigidBodies.remove(entity);
        if (body != null) {
            physics.removeRigidBody(body);
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
            }
        }
    }
}

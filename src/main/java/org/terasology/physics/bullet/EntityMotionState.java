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

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.location.LocationComponent;

/**
 * This motion state is used for rigid bodies. The character mover is not
 * relocated by the physics engine, and triggers are also dependant on the
 * LocationComponent. For normal rigid bodies this dependency is inverted and
 * the location is determined by the physics engine, which occurs though the use
 * of this type of MotionState.
 *
 * @author Immortius
 */
public class EntityMotionState extends MotionState {
    private EntityRef entity;

    /**
     * Only the BulletPhysics class is expected to create instances.
     *
     * @param entity The entity to relate this motion state to and set the
     * LocationComponent of.
     */
    EntityMotionState(EntityRef entity) {
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
            Quat4f rot = new Quat4f();
            transform.getRotation(rot);
            if (!transform.origin.equals(loc.getWorldPosition()) || !rot.equals(loc.getWorldRotation())) {
                loc.setWorldPosition(transform.origin);
                loc.setWorldRotation(transform.getRotation(new Quat4f()));
                entity.saveComponent(loc);
            }
        }
    }
    
}

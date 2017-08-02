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

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.linearmath.LinearMathJNI;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.VecMath;
import org.terasology.math.geom.Vector3f;

import javax.vecmath.Matrix4f;

/**
 * This motion state is used to connect rigid body entities to their rigid body in the bullet physics engine.
 * Bullet reads the initial state of the rigid body out of the entity, and then updates its location and rotation
 * as it moves under physics.
 *
 */
public class EntityMotionState extends btMotionState {
    private EntityRef entity;

    /**
     * Only the BulletPhysics class is expected to create instances.
     *
     * @param entity The entity to relate this motion state to and set the
     *               LocationComponent of.
     */
    EntityMotionState(EntityRef entity) {
        super();
        this.entity = entity;
    }

    @Override
    public void getWorldTransform(Matrix4 worldTrans) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f location = loc.getWorldPosition();
        worldTrans.set(new Vector3(location.x,location.y,location.z),VecMath.to(loc.getWorldRotation()));
    }

    @Override
    public void setWorldTransform(Matrix4 transform) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            loc.setWorldPosition(VecMath.from(transform.getTranslation(Vector3.Zero)));
            loc.setWorldRotation(VecMath.from(transform.getRotation(new Quaternion())));
        }
    }

}

/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.physics.bullet.shapes;

import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.physics.shapes.CollisionShape;


public class BulletCylinderShape extends  BulletCollisionShape {

    private final btCylinderShape cylinderShape;

    public BulletCylinderShape(Vector3f halfExtents) {

        this.cylinderShape = new btCylinderShape(halfExtents);
        this.underlyingShape = cylinderShape;
    }

    @Override
    public CollisionShape rotate(Quaternionf rot) {
        return this;
    }
}

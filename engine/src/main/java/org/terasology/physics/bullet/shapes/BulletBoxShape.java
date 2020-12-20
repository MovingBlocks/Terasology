/*
 * Copyright 2017 MovingBlocks
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

import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.physics.shapes.BoxShape;
import org.terasology.physics.shapes.CollisionShape;

public class BulletBoxShape extends BulletCollisionShape implements BoxShape {
    private final btBoxShape boxShape;

    public BulletBoxShape(Vector3f halfExtents) {
        boxShape = new btBoxShape(halfExtents);
        underlyingShape = boxShape;
    }

    @Override
    public CollisionShape rotate(Quaternionf rot) {
        Vector3f halfExtentsWithMargin = new Vector3f(boxShape.getHalfExtentsWithMargin());
        return new BulletBoxShape(halfExtentsWithMargin.rotate(rot).absolute());
    }

    @Override
    public Vector3f getHalfExtentsWithoutMargin() {
        return boxShape.getHalfExtentsWithoutMargin().mul(2);
    }
}

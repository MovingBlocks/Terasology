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


import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import org.joml.AABBf;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.physics.shapes.CollisionShape;

public abstract class BulletCollisionShape implements CollisionShape {
    public btCollisionShape underlyingShape;

    @Override
    public AABBf getAABB(Vector3fc origin, Quaternionfc rotation, float scale) {

        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f();
        Matrix4f m = new Matrix4f();
        underlyingShape.getAabb(m, min, max);

        return new AABBf(min, max).translate(origin);
    }

}

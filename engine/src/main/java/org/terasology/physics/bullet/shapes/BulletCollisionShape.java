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


import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;
import org.terasology.math.AABB;
import org.terasology.math.VecMath;

public abstract class BulletCollisionShape implements org.terasology.physics.shapes.CollisionShape {
    public CollisionShape underlyingShape;

    @Override
    public AABB getAABB(org.terasology.math.Transform transform) {
        Transform t = toBulletTransform(transform);

        javax.vecmath.Vector3f min = new javax.vecmath.Vector3f();
        javax.vecmath.Vector3f max = new javax.vecmath.Vector3f();
        underlyingShape.getAabb(t, min, max);

        return AABB.createMinMax(VecMath.from(min), VecMath.from(max));
    }

    protected static Transform toBulletTransform(org.terasology.math.Transform transform) {
        return new Transform(
                new javax.vecmath.Matrix4f(VecMath.to(transform.rotation),
                        VecMath.to(transform.origin), transform.scale)
        );
    }
}

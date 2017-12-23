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

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.linearmath.Transform;
import org.terasology.math.AABB;
import org.terasology.math.VecMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.shapes.CollisionShape;

public class BulletBoxShape extends BulletCollisionShape implements org.terasology.physics.shapes.BoxShape {
    private final BoxShape boxShape;

    public BulletBoxShape(Vector3f halfExtents) {
        boxShape = new BoxShape(VecMath.to(halfExtents));
        underlyingShape = boxShape;
    }

    @Override
    public CollisionShape rotate(Quat4f rot) {
        return null;
    }

    @Override
    public Vector3f getHalfExtentsWithoutMargin(Vector3f vector3f) {
        javax.vecmath.Vector3f out = new javax.vecmath.Vector3f();
        return VecMath.from(boxShape.getHalfExtentsWithoutMargin(out));
    }
}

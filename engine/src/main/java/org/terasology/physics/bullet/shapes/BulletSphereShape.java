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

import com.bulletphysics.collision.shapes.SphereShape;
import org.terasology.math.geom.Quat4f;
import org.terasology.physics.shapes.CollisionShape;

public class BulletSphereShape extends BulletCollisionShape implements org.terasology.physics.shapes.SphereShape {
    private final SphereShape sphereShape;

    public BulletSphereShape(float radius) {
        sphereShape = new SphereShape(radius);
        underlyingShape = sphereShape;
    }

    @Override
    public CollisionShape rotate(Quat4f rot) {
        return this;
    }
}

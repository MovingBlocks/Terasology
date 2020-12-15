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
package org.terasology.physics.shapes;

import org.joml.AABBf;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.terasology.math.AABB;

/**
 * The base type representing a collision shape in the physics engine.
 */
public interface CollisionShape {
    /**
     * Returns the axis-aligned bounding box ({@link AABB}) of the transformed shape.
     *
     * @return The {@link AABBf} bounding the shape.
     */
    AABBf getAABB(Vector3fc origin, Quaternionfc rotation, float scale);

    /**
     * Returns an identical shape rotated through the rotation angles represented by {@code rot}.
     *
     * @param rot The quaternion representation of the rotation.
     * @return The rotated shape.
     */
    CollisionShape rotate(Quaternionf rot);
}

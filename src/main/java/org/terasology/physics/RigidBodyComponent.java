/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;
import org.terasology.world.block.ForceBlockActive;

import java.util.List;

/**
 * @author Immortius
 */
@ForceBlockActive
public class RigidBodyComponent implements Component {
    @Replicate
    public float mass = 10.0f;
    @Replicate
    public boolean kinematic = false;

    @Replicate
    public CollisionGroup collisionGroup = StandardCollisionGroup.DEFAULT;
    @Replicate
    public List<CollisionGroup> collidesWith = Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD, StandardCollisionGroup.KINEMATIC);
}

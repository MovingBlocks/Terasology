/*
 * Copyright 2012
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

import java.util.List;

import org.terasology.entitySystem.Component;

import com.google.common.collect.Lists;

/**
 * @author Immortius
 */
public class RigidBodyComponent implements Component {
    public float mass = 10.0f;
    public float restitution = 0;
    public float friction = 1;
    public float damping = 0;
    public float velocity = 0;
    public float gravity = 1;
    public float ccdMotionThreshold = 0;
    public float ccdSweptSphereRadius = 0;

    public CollisionGroup collisionGroup = StandardCollisionGroup.DEFAULT;
    public List<CollisionGroup> collidesWith = Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD, StandardCollisionGroup.KINEMATIC);
}

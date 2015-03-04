/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.particles.components;

import org.terasology.entitySystem.Component;
import org.terasology.math.AABB;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;

/**
 * Created by Linus on 1-3-2015.
 */
public class ParticleEmitterComponent implements Component {
    public float spawnRateMean = 10.0f;
    public float spawnRateStdDev = 1.0f;

    public Vector3f spawnPositionMin = new Vector3f();
    public Vector3f spawnPositionMax = new Vector3f();

    public float minVelocity = 0.0f;
    public float maxVelocity = 0.0f;

    public Vector3f velocityDirection = new Vector3f();
    public Vector3f velocityDirectionRandomness = new Vector3f();

    public Vector4f color = new Vector4f(0.7f, 0.7f, 0.7f, 1.0f);
    public Vector4f colorRandomness = new Vector4f();
}

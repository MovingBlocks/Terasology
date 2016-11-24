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
package org.terasology.rendering.particles.components.generators;

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 7-3-2015.
 */
public class VelocityRangeGeneratorComponent implements Component {
    public Vector3f minCoords;
    public Vector3f maxCoords;

    public VelocityRangeGeneratorComponent(final Vector3f minCoords, final Vector3f maxCoords) {
        this.minCoords = new Vector3f(minCoords);
        this.maxCoords = new Vector3f(maxCoords);
    }

    public VelocityRangeGeneratorComponent() {
        this.minCoords = new Vector3f();
        this.maxCoords = new Vector3f();
    }
}

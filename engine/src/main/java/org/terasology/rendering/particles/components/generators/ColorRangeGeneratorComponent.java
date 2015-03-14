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
import org.terasology.math.geom.Vector4f;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.utilities.random.Random;

/**
 * Created by Linus on 7-3-2015.
 */
public class ColorRangeGeneratorComponent implements Component {

    public Vector4f minColorComponents;
    public Vector4f maxColorComponents;

    public ColorRangeGeneratorComponent(final Vector4f minColorComponents, final Vector4f maxColorComponents) {
        this.minColorComponents = new Vector4f(minColorComponents);
        this.maxColorComponents = new Vector4f(maxColorComponents);
    }

    public ColorRangeGeneratorComponent() {
        minColorComponents = new Vector4f();
        maxColorComponents = new Vector4f();
    }
}

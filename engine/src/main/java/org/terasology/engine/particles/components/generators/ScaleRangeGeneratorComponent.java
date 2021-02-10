/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.particles.components.generators;

import org.joml.Vector3f;
import org.terasology.entitySystem.Component;
import org.terasology.module.sandbox.API;

/**
 *
 */
@API
public class ScaleRangeGeneratorComponent implements Component {
    public Vector3f minScale;
    public Vector3f maxScale;

    public ScaleRangeGeneratorComponent(final Vector3f min, final Vector3f max) {
        minScale = new Vector3f(min);
        maxScale = new Vector3f(max);
    }

    public ScaleRangeGeneratorComponent() {
        minScale = new Vector3f();
        maxScale = new Vector3f();
    }
}

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
public class PositionRangeGeneratorComponent implements Component {

    public Vector3f minPosition;
    public Vector3f maxPosition;

    public PositionRangeGeneratorComponent(final Vector3f minPosition, final Vector3f maxPosition) {
        this.minPosition = new Vector3f(minPosition);
        this.maxPosition = new Vector3f(maxPosition);
    }

    public PositionRangeGeneratorComponent() {
        minPosition = new Vector3f();
        maxPosition = new Vector3f();
    }
}

/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.core.world.generator.worldGenerators;

import org.terasology.entitySystem.Component;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.nui.properties.TextField;

/**
 * Some configs for {@link PerlinWorldGenerator}
 *
 * @author Martin Steiger
 */
public class PerlinWorldConfigComponent implements Component {

    @TextField(label = "World Title")
    private String worldTitle = "New World";

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the grass density for forests")
    private float forestGrassDensity = 0.3f;

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the grass density for plains")
    private float plainsGrassDensity = 0.2f;

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the grass density for snow")
    private float snowGrassDensity = 0.001f;

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the grass density for mountains")
    private float mountainGrassDensity = 0.2f;

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the grass density for deserts")
    private float desertGrassDensity = 0.001f;

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the coal density")
    private float coalDensity = 0.050f;

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the iron density")
    private float ironDensity = 0.010f;

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the copper density")
    private float copperDensity = 0.010f;

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the gold density")
    private float goldDensity = 0.005f;

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the diamond density")
    private float diamondDensity = 0.001f;

    @Range(min = 0, max = 1.0f, increment = 0.001f, precision = 3, description = "Define the gravel density")
    private float gravelDensity = 0.050f;

}

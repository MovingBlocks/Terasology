/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.core.world.viewer.layers;

import java.util.Map;
import java.util.function.Function;

import org.terasology.core.world.generator.rasterizers.FloraType;
import org.terasology.rendering.nui.Color;

import com.google.common.collect.Maps;

/**
 * Maps {@link FloraType} to color.
 */
public class CoreFloraColors implements Function<FloraType, Color> {

    private final Map<FloraType, Color> floraColors = Maps.newHashMap();

    public CoreFloraColors() {
        floraColors.put(FloraType.GRASS, new Color(0x0c907780));
        floraColors.put(FloraType.FLOWER, new Color(0xddda1180));
        floraColors.put(FloraType.MUSHROOM, new Color(0x88991180));
    }

    @Override
    public Color apply(FloraType biome) {
        Color color = floraColors.get(biome);
        return color;
    }
}

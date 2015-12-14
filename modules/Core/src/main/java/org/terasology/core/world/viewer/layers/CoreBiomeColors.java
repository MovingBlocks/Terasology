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

import org.terasology.core.world.CoreBiome;
import org.terasology.rendering.nui.Color;
import org.terasology.world.biomes.Biome;

import com.google.common.collect.Maps;

/**
 * TODO Type description
 */
public class CoreBiomeColors implements Function<CoreBiome, Color> {

    private final Map<Biome, Color> biomeColors = Maps.newHashMap();

    public CoreBiomeColors() {
        biomeColors.put(CoreBiome.DESERT, new Color(0xb0a087ff));
        biomeColors.put(CoreBiome.MOUNTAINS, new Color(0x899a47ff));
        biomeColors.put(CoreBiome.PLAINS, new Color(0x80b068ff));
        biomeColors.put(CoreBiome.SNOW, new Color(0x99ffffff));
        biomeColors.put(CoreBiome.FOREST, new Color(0x439765ff));
        biomeColors.put(CoreBiome.OCEAN, new Color(0x44447aff));
        biomeColors.put(CoreBiome.BEACH, new Color(0xd0c087ff));
    }

    @Override
    public Color apply(CoreBiome biome) {
        Color color = biomeColors.get(biome);
        return color;
    }

    /**
     * @param biome the biome
     * @param color the new color
     */
    public void setBiomeColor(Biome biome, Color color) {
        this.biomeColors.put(biome, color);
    }
}

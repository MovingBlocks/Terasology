/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.core.logic.generators;

import com.google.common.collect.ImmutableMap;
import org.terasology.core.world.generator.chunkGenerators.*;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.LSystemRule;
import org.terasology.world.block.BlockManager;

import java.util.Map;

public abstract class DefaultGenerators {

    public static void addDefaultForestGenerators(ForestGenerator mngr) {

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        // Cactus
        TreeGenerator cactus = new TreeGeneratorCactus().setTrunkType(blockManager.getBlock("core:Cactus")).setGenerationProbability(0.05f);

        // Oak
        TreeGenerator oakTree = new SeedTreeGenerator().setBlock(blockManager.getBlock("core:OakSaplingGenerated")).setGenerationProbability(0.08f);

        // Add the trees to the generator lists
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.MOUNTAINS, oakTree);
        //mngr.addTreeGenerator(WorldBiomeProvider.Biome.MOUNTAINS, pineTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, oakTree);
        //mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, pineTree);
        //mngr.addTreeGenerator(WorldBiomeProvider.Biome.FOREST, oakVariationTree);

        //mngr.addTreeGenerator(WorldBiomeProvider.Biome.SNOW, birkTree);

        //mngr.addTreeGenerator(WorldBiomeProvider.Biome.PLAINS, redTree);
        mngr.addTreeGenerator(WorldBiomeProvider.Biome.PLAINS, oakTree);

        mngr.addTreeGenerator(WorldBiomeProvider.Biome.DESERT, cactus);
    }
}

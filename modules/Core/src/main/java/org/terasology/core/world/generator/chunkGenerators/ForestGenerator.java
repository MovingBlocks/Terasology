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

package org.terasology.core.world.generator.chunkGenerators;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.terasology.core.world.WorldBiomeProvider;
import org.terasology.core.world.generator.BiomeProviderDependentSecondPassGenerator;
import org.terasology.math.Vector3i;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.ChunkView;
import org.terasology.world.chunks.ChunkConstants;

import java.util.Map;

/**
 * @author Immortius
 */
public class ForestGenerator implements BiomeProviderDependentSecondPassGenerator {
    private String seed;
    private WorldBiomeProvider biomeProvider;

    private ListMultimap<WorldBiomeProvider.Biome, TreeGenerator> treeGenerators = ArrayListMultimap.create();

    public void addTreeGenerator(WorldBiomeProvider.Biome type, TreeGenerator gen) {
        treeGenerators.put(type, gen);
    }

    @Override
    public void postProcessChunk(Vector3i pos, ChunkView view) {
        FastRandom random = new FastRandom(seed.hashCode() ^ (pos.x + 39L * (pos.y + 39L * pos.z)));
        for (int y = 32; y < ChunkConstants.SIZE_Y; y++) {
            for (int x = 4; x < ChunkConstants.SIZE_X; x += 4) {
                for (int z = 4; z < ChunkConstants.SIZE_Z; z += 4) {
                    Vector3i worldPos = new Vector3i(pos);
                    worldPos.mult(new Vector3i(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z));
                    worldPos.add(x, y, z);
                    WorldBiomeProvider.Biome biome = biomeProvider.getBiomeAt(worldPos.x, worldPos.z);

                    int randX = x + random.nextInt(-3, 3);
                    int randZ = z + random.nextInt(-3, 3);

                    TreeGenerator treeGen = random.nextItem(treeGenerators.get(biome));

                    if (treeGen != null && treeGen.canGenerateAt(view, randX, y, randZ) && random.nextFloat() < treeGen.getGenerationProbability()) {
                        treeGen.generate(view, random, randX, y, randZ);
                    }
                }
            }
        }
    }

    @Override
    public void setWorldSeed(String value) {
        this.seed = value;
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider value) {
        this.biomeProvider = value;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    @Override
    public void setInitParameters(final Map<String, String> initParameters) {
    }

}

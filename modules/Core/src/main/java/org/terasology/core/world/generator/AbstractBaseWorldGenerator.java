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
package org.terasology.core.world.generator;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.core.world.internal.WorldBiomeProviderImpl;
import org.terasology.engine.SimpleUri;
import org.terasology.math.Vector3i;
import org.terasology.rendering.nui.Color;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.BaseChunkGenerator;
import org.terasology.world.generator.FirstPassGenerator;
import org.terasology.world.generator.SecondPassGenerator;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.WorldGenerator2DPreview;

import java.util.Arrays;
import java.util.List;

/**
 * @author Immortius
 */
public abstract class AbstractBaseWorldGenerator implements WorldGenerator, WorldGenerator2DPreview {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBaseWorldGenerator.class);

    private String worldSeed;
    private WorldBiomeProvider biomeProvider;
    private final List<FirstPassGenerator> firstPassGenerators = Lists.newArrayList();
    private final List<SecondPassGenerator> secondPassGenerators = Lists.newArrayList();
    private final SimpleUri uri;

    public AbstractBaseWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    public abstract void initialize();

    @Override
    public final SimpleUri getUri() {
        return uri;
    }

    @Override
    public void setWorldSeed(final String seed) {
        worldSeed = seed;
        biomeProvider = new WorldBiomeProviderImpl(seed);
        for (final BaseChunkGenerator generator : firstPassGenerators) {
            setBiome(generator);
            generator.setWorldSeed(seed);
        }
        for (final BaseChunkGenerator generator : secondPassGenerators) {
            setBiome(generator);
            generator.setWorldSeed(seed);
        }
    }

    protected final void register(final FirstPassGenerator generator) {
        registerBaseChunkGenerator(generator);
        firstPassGenerators.add(generator);
    }

    protected final void register(final SecondPassGenerator generator) {
        registerBaseChunkGenerator(generator);
        secondPassGenerators.add(generator);
    }

    private void registerBaseChunkGenerator(final BaseChunkGenerator generator) {
        setBiome(generator);
        generator.setWorldSeed(worldSeed);
    }

    private void setBiome(BaseChunkGenerator generator) {
        generator.setWorldBiomeProvider(biomeProvider);
    }

    @Override
    public void createChunk(final Chunk chunk) {
        for (final FirstPassGenerator generator : firstPassGenerators) {
            generator.generateChunk(chunk);
        }
    }

    @Override
    public void applySecondPass(final Vector3i chunkPos, final ChunkView view) {
        for (final SecondPassGenerator generator : secondPassGenerators) {
            generator.postProcessChunk(chunkPos, view);
        }
    }

    @Override
    public float getFog(float x, float y, float z) {
        return biomeProvider.getFogAt(x, y, z);
    }

    @Override
    public float getTemperature(float x, float y, float z) {
        return biomeProvider.getTemperatureAt((int) x, (int) z);
    }

    @Override
    public float getHumidity(float x, float y, float z) {
        return biomeProvider.getHumidityAt((int) x, (int) z);
    }

    @Override
    public Color get(String layerName, int x, int z) {
        switch (layerName) {
            case "Biome":
                WorldBiomeProvider.Biome biome = biomeProvider.getBiomeAt(x, z);
                switch (biome) {
                    case DESERT:
                        return Color.YELLOW;
                    case FOREST:
                        return Color.GREEN;
                    case MOUNTAINS:
                        return new Color(240, 120, 120);
                    case PLAINS:
                        return new Color(220, 220, 60);
                    case SNOW:
                        return Color.WHITE;
                    default:
                        return Color.GREY;
                }
            case "Humidity":
                float hum = biomeProvider.getHumidityAt(x, z);
                return new Color(hum * 0.2f, hum * 0.2f, hum);
            case "Temperature":
                float temp = biomeProvider.getTemperatureAt(x, z);
                return new Color(temp, temp * 0.2f, temp * 0.2f);
            default:
                return new Color();
        }
    }

    @Override
    public Iterable<String> getLayers() {
        return Arrays.asList("Biome", "Humidity", "Temperature");
    }
    
    @Override
    public Optional<WorldConfigurator> getConfigurator() {
        return Optional.absent();
    }
}

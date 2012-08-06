/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.world.generator.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.terasology.world.generator.BaseChunkGenerator;
import org.terasology.world.generator.core.ChunkGeneratorManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Read and write the ChunkGeneratorManager from/to a JSON file.
 * 
 * @author Mathias Kalb
 */
public final class ChunkGeneratorJSONFormat {

    public static void write(final ChunkGeneratorManager chunkGeneratorManager, final BufferedWriter writer) throws IOException {
        ChunkGeneratorJSONFormat.newGson().toJson(new JSONChunkGeneratorManager(chunkGeneratorManager), writer);
    }

    public static ChunkGeneratorManager read(final BufferedReader reader) throws IOException {
        try {
            final JSONChunkGeneratorManager jsonChunkGeneratorManager = ChunkGeneratorJSONFormat.newGson().fromJson(reader, JSONChunkGeneratorManager.class);

            return jsonChunkGeneratorManager.createChunkGeneratorManager();
        } catch (final JsonSyntaxException e) {
            throw new IOException("Failed to load chunkGeneratorManager", e);
        } catch (final IOException e) {
            throw new IOException("Failed to load chunkGeneratorManager", e);
        }
    }

    private static Gson newGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    private static final class JSONChunkGeneratorManager {

        private final String className;
        private final List<JSONBaseChunkGenerator> baseChunkGenerators;

        JSONChunkGeneratorManager(final ChunkGeneratorManager chunkGeneratorManager) {
            className = chunkGeneratorManager.getClass().getName();
            baseChunkGenerators = new ArrayList<ChunkGeneratorJSONFormat.JSONBaseChunkGenerator>();
            for (final BaseChunkGenerator baseChunkGenerator : chunkGeneratorManager.getBaseChunkGenerators()) {
                baseChunkGenerators.add(new JSONBaseChunkGenerator(baseChunkGenerator));
            }
        }

        @SuppressWarnings("rawtypes")
        ChunkGeneratorManager createChunkGeneratorManager() throws IOException {
            try {
                final Class chunkGeneratorManagerClass = Class.forName(className);
                final ChunkGeneratorManager chunkGeneratorManager = (ChunkGeneratorManager) chunkGeneratorManagerClass.newInstance();

                for (final JSONBaseChunkGenerator jsonBaseChunkGenerator : baseChunkGenerators) {
                    chunkGeneratorManager.registerChunkGenerator(jsonBaseChunkGenerator.createBaseChunkGenerator());
                }

                return chunkGeneratorManager;
            } catch (final ClassNotFoundException e) {
                throw new IOException(e);
            } catch (final InstantiationException e) {
                throw new IOException(e);
            } catch (final IllegalAccessException e) {
                throw new IOException(e);
            }
        }
    }

    private static final class JSONBaseChunkGenerator {
        private final String className;
        private final Map<String, String> initParameters;

        JSONBaseChunkGenerator(final BaseChunkGenerator baseChunkGenerator) {
            className = baseChunkGenerator.getClass().getName();
            initParameters = baseChunkGenerator.getInitParameters();
        }

        @SuppressWarnings("rawtypes")
        BaseChunkGenerator createBaseChunkGenerator() throws IOException {
            try {
                final Class baseChunkGeneratorClass = Class.forName(className);
                final BaseChunkGenerator baseChunkGenerator = (BaseChunkGenerator) baseChunkGeneratorClass.newInstance();
                baseChunkGenerator.setInitParameters(initParameters);
                return baseChunkGenerator;
            } catch (final ClassNotFoundException e) {
                throw new IOException(e);
            } catch (final InstantiationException e) {
                throw new IOException(e);
            } catch (final IllegalAccessException e) {
                throw new IOException(e);
            }
        }
    }

}

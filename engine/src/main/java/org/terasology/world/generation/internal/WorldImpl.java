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
package org.terasology.world.generation.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.World;
import org.terasology.world.generation.WorldDataProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public class WorldImpl implements World {

    private final long seed;
    private final Map<Class<? extends WorldDataProvider>, WorldDataProvider> providers;
    private final List<WorldRasterizer> rasterizers;

    public WorldImpl(long seed, Map<Class<? extends WorldDataProvider>, WorldDataProvider> providers, Collection<WorldRasterizer> rasterizers) {
        this.seed = seed;
        this.providers = ImmutableMap.copyOf(providers);
        this.rasterizers = ImmutableList.copyOf(rasterizers);
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public <T extends WorldDataProvider> T getWorldDataProvider(Class<T> providerClass) {
        return providerClass.cast(providers.get(providerClass));
    }

    @Override
    public void rasterizeChunk(CoreChunk chunk) {
        for (WorldRasterizer rasterizer : rasterizers) {
            rasterizer.generateChunk(chunk);
        }
    }

}

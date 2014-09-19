/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.generator.internal;

import com.google.common.base.Optional;
import org.terasology.engine.SimpleUri;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.World;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;

public class RemoteWorldGenerator implements WorldGenerator {

    private String worldSeed;

    @Override
    public void initialize() {
    }

    @Override
    public SimpleUri getUri() {
        return null;
    }

    @Override
    public void setWorldSeed(String seed) {
        worldSeed = seed;
    }

    @Override
    public void createChunk(CoreChunk chunk) {
    }

    @Override
    public Optional<WorldConfigurator> getConfigurator() {
        return Optional.absent();
    }

    @Override
    public World getWorld() {
        return null;
    }
}

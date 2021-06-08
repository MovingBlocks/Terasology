// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.unittest.stubs;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.generation.EntityBuffer;
import org.terasology.engine.world.generation.World;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.WorldConfigurator;
import org.terasology.engine.world.generator.WorldGenerator;

import static com.google.common.base.Preconditions.checkNotNull;

@RegisterWorldGenerator(id = "stub", displayName = "Stub")
public class StubWorldGenerator implements WorldGenerator {
    private final SimpleUri uri;

    public StubWorldGenerator() {
        this(new SimpleUri("unittest", "stub"));
    }

    public StubWorldGenerator(SimpleUri uri) {
        this.uri = checkNotNull(uri);
    }

    @Override
    public SimpleUri getUri() {
        return uri;
    }

    @Override
    public String getWorldSeed() {
        return null;
    }

    @Override
    public void setWorldSeed(String seed) {

    }

    @Override
    public void createChunk(Chunk chunk, EntityBuffer buffer) {

    }

    @Override
    public void initialize() {

    }

    @Override
    public WorldConfigurator getConfigurator() {
        return null;
    }

    @Override
    public World getWorld() {
        return null;
    }
}

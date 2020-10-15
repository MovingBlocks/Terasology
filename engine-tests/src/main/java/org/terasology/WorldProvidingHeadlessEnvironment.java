// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.naming.Name;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.internal.EntityAwareWorldProvider;
import org.terasology.world.internal.WorldProviderCore;
import org.terasology.world.internal.WorldProviderWrapper;

/**
 * Environment with a MapWorldProvider and BlockManager. Useful to get headless environment with a generated world.
 */
public class WorldProvidingHeadlessEnvironment extends HeadlessEnvironment {

    public WorldProvidingHeadlessEnvironment(Name ... modules) {
        super(modules);
    }

    public void setupWorldProvider(WorldGenerator generator) {
        generator.initialize();
        WorldProviderCore stub = new MapWorldProvider(generator, context.get(BlockManager.class),
                context.get(ExtraBlockDataManager.class));
        WorldProvider world = new WorldProviderWrapper(stub, context.get(ExtraBlockDataManager.class));
        CoreRegistry.put(WorldProvider.class, world);
        CoreRegistry.put(BlockEntityRegistry.class, new EntityAwareWorldProvider(stub,
                context.get(ComponentSystemManager.class)));
    }

}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine;

import org.terasology.naming.Name;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.internal.EntityAwareWorldProvider;
import org.terasology.engine.world.internal.WorldProviderCore;
import org.terasology.engine.world.internal.WorldProviderWrapper;

/**
 * Environment with a MapWorldProvider and BlockManager. Useful to get headless environment with a generated world.
 *
 * You should to use {@link TestResourceLocks#CORE_REGISTRY} for using this class.
 */
public class WorldProvidingHeadlessEnvironment extends HeadlessEnvironment {

    public WorldProvidingHeadlessEnvironment(Name ... modules) {
        super(modules);
    }

    public void setupWorldProvider(WorldGenerator generator) {
        generator.initialize();
        WorldProviderCore stub = new MapWorldProvider(generator, context.get(BlockManager.class), context.get(ExtraBlockDataManager.class));
        WorldProvider world = new WorldProviderWrapper(stub, context.get(ExtraBlockDataManager.class));
        CoreRegistry.put(WorldProvider.class, world);
        CoreRegistry.put(BlockEntityRegistry.class, new EntityAwareWorldProvider(stub, context));
    }

}

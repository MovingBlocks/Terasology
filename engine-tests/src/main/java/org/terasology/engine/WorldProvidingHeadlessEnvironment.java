/*
 * Copyright 2017 MovingBlocks
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

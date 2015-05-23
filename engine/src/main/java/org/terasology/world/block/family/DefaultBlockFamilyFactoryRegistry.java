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
package org.terasology.world.block.family;

import com.google.common.collect.Maps;

import java.util.Map;

public class DefaultBlockFamilyFactoryRegistry implements BlockFamilyFactoryRegistry {
    private Map<String, BlockFamilyFactory> registryMap = Maps.newHashMap();
    private BlockFamilyFactory defaultBlockFamilyFactory = new SymmetricBlockFamilyFactory();

    public void setBlockFamilyFactory(String id, BlockFamilyFactory blockFamilyFactory) {
        registryMap.put(id.toLowerCase(), blockFamilyFactory);
    }

    @Override
    public BlockFamilyFactory getBlockFamilyFactory(String blockFamilyFactoryId) {
        if (blockFamilyFactoryId == null || blockFamilyFactoryId.isEmpty()) {
            return defaultBlockFamilyFactory;
        }
        BlockFamilyFactory factory = registryMap.get(blockFamilyFactoryId.toLowerCase());
        if (factory == null) {
            return defaultBlockFamilyFactory;
        }
        return factory;
    }

    public void clear() {
        registryMap.clear();
    }
}

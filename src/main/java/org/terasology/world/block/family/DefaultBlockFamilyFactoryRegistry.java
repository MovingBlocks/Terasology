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
}

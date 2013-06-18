package org.terasology.world.block.family;

import com.google.common.collect.Maps;

import java.util.Map;

public class DefaultBlockFamilyFactoryRegistry implements BlockFamilyFactoryRegistry {
    private Map<String, BlockFamilyFactory> registryMap = Maps.newHashMap();
    private BlockFamilyFactory defaultBlockFamilyFactory;

    public void setDefaultBlockFamilyFactory(BlockFamilyFactory defaultBlockFamilyFactory) {
        this.defaultBlockFamilyFactory = defaultBlockFamilyFactory;
    }

    public void setBlockFamilyFactory(String id, BlockFamilyFactory blockFamilyFactory) {
        registryMap.put(id.toLowerCase(), blockFamilyFactory);
    }

    @Override
    public BlockFamilyFactory getBlockFamilyFactory(String blockFamilyFactoryId) {
        if (blockFamilyFactoryId == null)
            return defaultBlockFamilyFactory;
        return registryMap.get(blockFamilyFactoryId.toLowerCase());
    }
}

package org.terasology.world.block.family;

public interface BlockFamilyFactoryRegistry {
    /**
     * @param id Case-insensitive
     * @return
     */
    public BlockFamilyFactory getBlockFamilyFactory(String id);
}

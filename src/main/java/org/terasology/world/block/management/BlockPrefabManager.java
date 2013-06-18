package org.terasology.world.block.management;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;

/**
 * @author Immortius
 */
public class BlockPrefabManager implements BlockRegistrationListener {

    private EntityManager entityManager;
    private PrefabManager prefabManager;
    private BlockManager blockManager;

    public BlockPrefabManager(EntityManager entityManager, BlockManager blockManager) {
        this.entityManager = entityManager;
        this.prefabManager = entityManager.getPrefabManager();
        this.blockManager = blockManager;

        updateExistingBlocks();
    }

    @Override
    public void onBlockFamilyRegistered(BlockFamily family) {
        for (Block block : family.getBlocks()) {
            updateBlock(block);
        }
    }

    private void updateExistingBlocks() {
        for (BlockFamily blockFamily : blockManager.listRegisteredBlockFamilies()) {
            for (Block block : blockFamily.getBlocks()) {
                updateBlock(block);
            }
        }
    }

    private void updateBlock(Block block) {
        String prefab = block.getPrefab();
        boolean keepActive = block.isKeepActive();
        boolean requiresLifecycleEvents = false;
        if (!prefab.isEmpty()) {
            Prefab blockPrefab = prefabManager.getPrefab(prefab);
            for (Component comp : blockPrefab.iterateComponents()) {
                ComponentMetadata<?> metadata = entityManager.getComponentLibrary().getMetadata(comp.getClass());
                if (metadata.isForceBlockActive()) {
                    keepActive = true;
                    break;
                }
                if (metadata.isBlockLifecycleEventsRequired()) {
                    requiresLifecycleEvents = true;
                }
            }
        }
        block.setKeepActive(keepActive);
        block.setLifecycleEventsRequired(requiresLifecycleEvents && !keepActive);
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.internal;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;

import java.util.Optional;

/**
 */
public class BlockPrefabManager implements BlockRegistrationListener {

    private EntityManager entityManager;
    private BlockManager blockManager;

    public BlockPrefabManager(EntityManager entityManager, BlockManager blockManager) {
        this.entityManager = entityManager;
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
        Optional<Prefab> prefab = block.getPrefab();
        boolean keepActive = block.isKeepActive();
        boolean requiresLifecycleEvents = false;
        if (prefab.isPresent()) {
            for (Component comp : prefab.get().iterateComponents()) {
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

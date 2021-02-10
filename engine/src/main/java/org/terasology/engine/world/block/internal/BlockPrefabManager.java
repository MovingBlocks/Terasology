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
package org.terasology.world.block.internal;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;

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

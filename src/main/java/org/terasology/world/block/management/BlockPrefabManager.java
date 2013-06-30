/*
 * Copyright 2013 Moving Blocks
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

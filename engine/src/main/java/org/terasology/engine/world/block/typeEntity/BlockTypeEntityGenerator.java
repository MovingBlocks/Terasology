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
package org.terasology.world.block.typeEntity;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.network.NetworkComponent;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.internal.BlockRegistrationListener;

import java.util.Optional;

/**
 */
public class BlockTypeEntityGenerator implements BlockRegistrationListener {

    private EntityManager entityManager;
    private PrefabManager prefabManager;
    private BlockManager blockManager;
    private Prefab blockTypePrefab;

    public BlockTypeEntityGenerator(EntityManager entityManager, BlockManager blockManager) {
        this.entityManager = entityManager;
        this.prefabManager = entityManager.getPrefabManager();
        this.blockManager = blockManager;
        blockTypePrefab = entityManager.getPrefabManager().getPrefab("engine:blockType");

        connectExistingEntities();
        generateForExistingBlocks();
    }

    private void connectExistingEntities() {
        for (EntityRef entity : entityManager.getEntitiesWith(BlockTypeComponent.class)) {
            BlockTypeComponent blockTypeComp = entity.getComponent(BlockTypeComponent.class);
            if (blockTypeComp.block == null) {
                entity.destroy();
            } else {
                blockTypeComp.block.setEntity(entity);
            }
        }
    }

    private void generateForExistingBlocks() {
        for (BlockFamily blockFamily : blockManager.listRegisteredBlockFamilies()) {
            for (Block block : blockFamily.getBlocks()) {
                if (!block.getEntity().exists()) {
                    generateBlockTypeEntity(block);
                }
            }
        }
    }

    @Override
    public void onBlockFamilyRegistered(BlockFamily family) {
        for (Block block : family.getBlocks()) {
            generateBlockTypeEntity(block);
        }
    }

    private void generateBlockTypeEntity(Block block) {
        EntityBuilder builder = entityManager.newBuilder(blockTypePrefab);
        builder.getComponent(BlockTypeComponent.class).block = block;
        // TODO: Copy across settings as necessary
        Optional<Prefab> prefab = block.getPrefab();
        if (prefab.isPresent()) {
            for (Component comp : prefab.get().iterateComponents()) {
                if (!(comp instanceof NetworkComponent)) {
                    builder.addComponent(entityManager.getComponentLibrary().copy(comp));
                }
            }
        }
        block.setEntity(builder.build());
    }
}

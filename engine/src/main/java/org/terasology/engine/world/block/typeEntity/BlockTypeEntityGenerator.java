// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.typeEntity;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.internal.BlockRegistrationListener;

import java.util.Optional;

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

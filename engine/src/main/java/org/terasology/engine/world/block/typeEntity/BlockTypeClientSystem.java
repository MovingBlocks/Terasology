// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.typeEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;

/**
 */
@RegisterSystem(RegisterMode.REMOTE_CLIENT)
public class BlockTypeClientSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(BlockTypeClientSystem.class);

    @Override
    public void shutdown() {
        for (Block block : CoreRegistry.get(BlockManager.class).listRegisteredBlocks()) {
            block.setEntity(EntityRef.NULL);
        }
    }

    @ReceiveEvent(components = BlockTypeComponent.class)
    public void onReceivedTypeEntity(OnAddedComponent event, EntityRef entity) {
        Block block = entity.getComponent(BlockTypeComponent.class).block;
        if (block != null) {
            block.setEntity(entity);
        } else {
            logger.error("Received block type entity with missing block type");
        }
    }

}

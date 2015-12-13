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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

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

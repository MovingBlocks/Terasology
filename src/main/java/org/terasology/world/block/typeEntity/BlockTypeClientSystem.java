package org.terasology.world.block.typeEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.REMOTE_CLIENT)
public class BlockTypeClientSystem implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(BlockTypeClientSystem.class);

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
        for (Block block : CoreRegistry.get(BlockManager.class).listRegisteredBlocks()) {
            block.setEntity(EntityRef.NULL);
        }
    }

    @ReceiveEvent(components=BlockTypeComponent.class)
    public void onReceivedTypeEntity(OnAddedComponent event, EntityRef entity) {
        Block block = entity.getComponent(BlockTypeComponent.class).block;
        if (block != null) {
            block.setEntity(entity);
        } else {
            logger.error("Received block type entity with missing block type");
        }
    }

}

package org.terasology.componentSystem.action;

import org.terasology.components.actions.ExplosionActionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.componentSystem.block.BlockEntityLookup;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.rendering.physics.BulletPhysicsRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ExplosionAction implements EventHandlerSystem {

    private IWorldProvider worldProvider;
    private FastRandom random = new FastRandom();
    private BulletPhysicsRenderer physicsRenderer;
    private BlockEntityLookup blockEntityLookup; 

    public void initialise() {
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        physicsRenderer = CoreRegistry.get(BulletPhysicsRenderer.class);
        blockEntityLookup = CoreRegistry.get(ComponentSystemManager.class).get(BlockEntityLookup.class);
    }

    @ReceiveEvent(components = {ExplosionActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        Vector3f origin = event.getLocation();
        if (origin == null) return;

        Vector3i blockPos = new Vector3i();
        for (int i = 0; i < 256; i++) {
            // TODO: Add a randomVector3f method to FastRandom?
            Vector3f direction = new Vector3f(random.randomFloat(), random.randomFloat(), random.randomFloat());
            direction.normalize();
            Vector3f impulse = new Vector3f(direction);
            impulse.scale(800000);

            for (int j = 0; j < 4; j++) {
                Vector3f target = new Vector3f(origin);

                target.x += direction.x * j;
                target.y += direction.y * j;
                target.z += direction.z * j;
                blockPos.set((int) target.x, (int) target.y, (int) target.z);
                byte currentBlockType = worldProvider.getBlock(blockPos);

                if (currentBlockType == 0x0)
                    continue;

                Block currentBlock = BlockManager.getInstance().getBlock(currentBlockType);

                /* PHYSICS */
                if (currentBlock.isDestructible()) {
                    // TODO: this should be handled centrally somewhere. Actions shouldn't be determining world behaviour
                    // like what happens when a block is destroyed.
                    worldProvider.setBlock(blockPos, (byte)0x0, true, true);
                    
                    EntityRef blockEntity = blockEntityLookup.getEntityAt(blockPos);
                    if (blockEntity != null) {
                        blockEntity.destroy();
                    }
                    physicsRenderer.addTemporaryBlock(target, currentBlockType, impulse, BulletPhysicsRenderer.BLOCK_SIZE.FULL_SIZE);
                }
            }
        }
    }
}

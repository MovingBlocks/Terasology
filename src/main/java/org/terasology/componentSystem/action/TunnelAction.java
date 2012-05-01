package org.terasology.componentSystem.action;

import org.terasology.componentSystem.block.BlockEntityRegistry;
import org.terasology.components.actions.TunnelActionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
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
public class TunnelAction implements EventHandlerSystem {
    
    private IWorldProvider worldProvider;
    private FastRandom random = new FastRandom();
    private BulletPhysicsRenderer physicsRenderer;
    private BlockEntityRegistry blockEntityRegistry;
    
    public void initialise() {
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        physicsRenderer = CoreRegistry.get(BulletPhysicsRenderer.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
    }

    @ReceiveEvent(components= TunnelActionComponent.class)
    public void onActivate(ActivateEvent event, EntityRef entity) {

        Vector3f dir = new Vector3f(event.getDirection());
        Vector3f origin = new Vector3f(event.getLocation());
        Vector3i blockPos = new Vector3i();
        for (int s = 4; s <= 10000; s += 30) {
            origin.add(dir);

            for (int i = 0; i < 64; i++) {
                Vector3f direction = random.randomVector3f();
                direction.normalize();
                Vector3f impulse = new Vector3f(direction);
                impulse.scale(800000);

                for (int j = 0; j < 3; j++) {
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

                        EntityRef blockEntity = blockEntityRegistry.getEntityAt(blockPos);
                        blockEntity.destroy();
                        physicsRenderer.addTemporaryBlock(target, currentBlockType, impulse, BulletPhysicsRenderer.BLOCK_SIZE.FULL_SIZE);
                    }
                }
            }
        }
    }
}

package org.terasology.componentSystem.action;

import org.terasology.components.actions.TunnelActionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.world.BlockEntityRegistry;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.rendering.physics.BulletPhysicsRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem
public class TunnelAction implements EventHandlerSystem {

    private static int MAX_DESTROYED_BLOCKS = 100;

    private WorldProvider worldProvider;
    private FastRandom random = new FastRandom();
    private BulletPhysicsRenderer physicsRenderer;
    private BlockEntityRegistry blockEntityRegistry;

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        physicsRenderer = CoreRegistry.get(BulletPhysicsRenderer.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = TunnelActionComponent.class)
    public void onActivate(ActivateEvent event, EntityRef entity) {

        Vector3f dir = new Vector3f(event.getDirection());
        Vector3f origin = new Vector3f(event.getOrigin());
        Vector3i blockPos = new Vector3i();

        int blockCounter = MAX_DESTROYED_BLOCKS;
        for (int s = 4; s <= 10000; s += 30) {
            origin.add(dir);

            for (int i = 0; i < 64; i++) {
                Vector3f direction = random.randomVector3f();
                direction.normalize();
                Vector3f impulse = new Vector3f(direction);
                impulse.scale(80);

                for (int j = 0; j < 3; j++) {
                    Vector3f target = new Vector3f(origin);

                    target.x += direction.x * j;
                    target.y += direction.y * j;
                    target.z += direction.z * j;

                    blockPos.set((int) target.x, (int) target.y, (int) target.z);

                    Block currentBlock = worldProvider.getBlock(blockPos);

                    if (currentBlock.getId() == 0x0)
                        continue;

                    if (currentBlock.isDestructible()) {
                        worldProvider.setBlock(blockPos, BlockManager.getInstance().getAir(), currentBlock);

                        EntityRef blockEntity = blockEntityRegistry.getEntityAt(blockPos);
                        blockEntity.destroy();
                        physicsRenderer.addTemporaryBlock(target, currentBlock.getId(), impulse, BulletPhysicsRenderer.BLOCK_SIZE.FULL_SIZE);

                        blockCounter--;
                    }

                    if (blockCounter <= 0)
                        return;
                }
            }
        }
    }
}

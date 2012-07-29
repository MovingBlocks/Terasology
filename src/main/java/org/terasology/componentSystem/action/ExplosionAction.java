package org.terasology.componentSystem.action;

import org.terasology.components.actions.ExplosionActionComponent;
import org.terasology.components.block.BlockComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.DroppedBlockFactory;
import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.world.BlockEntityRegistry;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.ImpulseEvent;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem(authorativeOnly = true)
public class ExplosionAction implements EventHandlerSystem {

    private WorldProvider worldProvider;
    private FastRandom random = new FastRandom();
    private BulletPhysics physicsRenderer;
    private BlockEntityRegistry blockEntityRegistry;
    private DroppedBlockFactory droppedBlockFactory;

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        physicsRenderer = CoreRegistry.get(BulletPhysics.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        droppedBlockFactory = new DroppedBlockFactory(CoreRegistry.get(EntityManager.class));
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {ExplosionActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        ExplosionActionComponent explosionComp = entity.getComponent(ExplosionActionComponent.class);
        Vector3f origin = null;
        switch (explosionComp.relativeTo) {
            case Self:
                LocationComponent loc = entity.getComponent(LocationComponent.class);
                if (loc != null) {
                    origin = loc.getWorldPosition();
                } else {
                    BlockComponent blockComp = entity.getComponent(BlockComponent.class);
                    if (blockComp != null) {
                        origin = blockComp.getPosition().toVector3f();
                    }
                }
                break;
            case Instigator:
                origin = event.getInstigatorLocation();
                break;
            default:
                origin = event.getTargetLocation();
                break;
        }

        if (origin == null) {
            return;
        }

        Vector3i blockPos = new Vector3i();
        for (int i = 0; i < 256; i++) {
            // TODO: Add a randomVector3f method to FastRandom?
            Vector3f direction = new Vector3f(random.randomFloat(), random.randomFloat(), random.randomFloat());
            direction.normalize();
            Vector3f impulse = new Vector3f(direction);
            impulse.scale(150);

            for (int j = 0; j < 4; j++) {
                Vector3f target = new Vector3f(origin);

                target.x += direction.x * j;
                target.y += direction.y * j;
                target.z += direction.z * j;
                blockPos.set((int) target.x, (int) target.y, (int) target.z);
                Block currentBlock = worldProvider.getBlock(blockPos);

                if (currentBlock.getId() == 0)
                    continue;

                /* PHYSICS */
                if (currentBlock.isDestructible()) {
                    // TODO: this should be handled centrally somewhere. Actions shouldn't be determining world behaviour
                    // like what happens when a block is destroyed.
                    worldProvider.setBlock(blockPos, BlockManager.getInstance().getAir(), currentBlock);

                    EntityRef blockEntity = blockEntityRegistry.getEntityAt(blockPos);
                    blockEntity.destroy();
                    if (random.randomInt(4) == 0) {
                        EntityRef block = droppedBlockFactory.newInstance(target, currentBlock.getBlockFamily(), 5);
                        block.send(new ImpulseEvent(impulse));
                    }
                }
            }
        }
    }
}

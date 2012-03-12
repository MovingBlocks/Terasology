package org.terasology.logic.systems;

import org.terasology.components.BlockComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.componentSystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.FullHealthEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockEntitySystem implements EventHandlerSystem {
    private static byte EmptyBlockId = 0x0;

    private IWorldProvider worldProvider;

    public void initialise() {
        worldProvider = CoreRegistry.get(IWorldProvider.class);
    }

    @ReceiveEvent(components={BlockComponent.class})
    public void onDestroyed(NoHealthEvent event, EntityRef entity)
    {
        if (worldProvider == null) return;
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        worldProvider.setBlock(blockComp.getPosition(), EmptyBlockId, true, true);
        // TODO: Need more central handling of these flow on effects

        // TODO: A bunch of notification?

        // Remove the upper block if it's a billboard
        byte upperBlockType = worldProvider.getBlock(blockComp.getPosition().x, blockComp.getPosition().y + 1, blockComp.getPosition().z);
        if (BlockManager.getInstance().getBlock(upperBlockType).getBlockForm() == Block.BLOCK_FORM.BILLBOARD) {
            worldProvider.setBlock(blockComp.getPosition().x, blockComp.getPosition().y + 1, blockComp.getPosition().z, (byte) 0x0, true, true);
            // TODO: Particles here too?
        }

        // TODO: Particle Entities
        //worldRenderer.getBlockParticleEmitter().setOrigin(blockPos.toVector3d());
        //worldRenderer.getBlockParticleEmitter().emitParticles(256, currentBlockType);
        // TODO: Sounds
        //AudioManager.play("RemoveBlock", 0.6f);

        // TODO: Pickups
        /* PHYSICS */
        //Vector3d pos = blockPos.toVector3d();
        //_player.getParent().getBulletRenderer().addLootableBlocks(new Vector3f(pos), block);

        entity.destroy();
    }
    
    @ReceiveEvent(components={BlockComponent.class})
    public void onRepaired(FullHealthEvent event, EntityRef entity) {
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        if (blockComp.temporary) {
            entity.destroy();
        }
    }

}

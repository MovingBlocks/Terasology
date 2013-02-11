package org.terasology.fences.componentSystem;

import org.terasology.components.InventoryComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.actions.AccessInventoryActionComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.events.OpenInventoryEvent;
import org.terasology.fences.components.FenceGateComponent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.GUIManager;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.rendering.gui.windows.UIScreenContainer;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector3f;

@RegisterComponentSystem
public class FenceGateSystem  implements EventHandlerSystem {

    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    public void initialise() {

    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {FenceGateComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        FenceGateComponent fenceGateComponent = entity.getComponent( FenceGateComponent.class );
        fenceGateComponent.isClosed = !fenceGateComponent.isClosed;
        entity.saveComponent(fenceGateComponent);

        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        if (blockComp == null) {
            event.cancel();
            return;
        }

        Vector3i primePos = new Vector3i(blockComp.getPosition());
        Block primeBlock = worldProvider.getBlock(primePos);

        Block newBlock = null;
        if ( fenceGateComponent.isClosed ){
            newBlock = BlockManager.getInstance().getBlockFamily("fences:FenceGateClosed").getBlockFor(primeBlock.getDirection(), Side.FRONT);
        }else{
            newBlock = BlockManager.getInstance().getBlockFamily("fences:FenceGateOpen").getBlockFor( primeBlock.getDirection(), Side.FRONT );

        }

        if ( newBlock != null ){
            blockEntityRegistry.setBlock(primePos, newBlock, primeBlock, entity);
        }

    }

}

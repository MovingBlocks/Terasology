/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.fences.componentSystem;

import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.fences.components.FenceGateComponent;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;

/**
 * System for opening and closing fence gates
 * @author Small-Jeeper
 */
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

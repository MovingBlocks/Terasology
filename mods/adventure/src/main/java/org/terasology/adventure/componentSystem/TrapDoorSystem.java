package org.terasology.adventure.componentSystem;

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

/**
 * Trapdoor system. Inspired by the fence(gate)-system.
 *
 * @author MrBarsack <mrbarsack@gmail.com>
 */

import org.terasology.adventure.components.TrapDoorComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;

@RegisterComponentSystem
public class TrapDoorSystem implements EventHandlerSystem {


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

    @ReceiveEvent(components = {TrapDoorComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        TrapDoorComponent trapDoorComponent = entity.getComponent( TrapDoorComponent.class );
        trapDoorComponent.isClosed = !trapDoorComponent.isClosed;
        entity.saveComponent(trapDoorComponent);

        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        if (blockComp == null) {
            event.cancel();
            return;
        }

        Vector3i primePos = new Vector3i(blockComp.getPosition());
        Block primeBlock = worldProvider.getBlock(primePos);

        Block newBlock = null;
        if ( trapDoorComponent.isClosed ){
            newBlock = BlockManager.getInstance().getBlockFamily("adventure:TrapDoorOpen").getBlockFor(primeBlock.getDirection(), Side.FRONT);
        }else{
            newBlock = BlockManager.getInstance().getBlockFamily("adventure:TrapDoorClosed").getBlockFor( primeBlock.getDirection(), Side.FRONT );

        }

        if ( newBlock != null ){
            blockEntityRegistry.setBlock(primePos, newBlock, primeBlock, entity);
        }

    }
}

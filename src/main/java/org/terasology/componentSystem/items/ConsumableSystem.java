/*
 * Copyright 2012 Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
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
package org.terasology.componentSystem.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.ConsumableComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.EatEvent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;

/**
 * System providing inventory related functionality
 *
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
@RegisterComponentSystem
public class ConsumableSystem implements EventHandlerSystem {
    private static final Logger logger = LoggerFactory.getLogger(ConsumableSystem.class);

    @In
    private WorldProvider worldProvider;
    
    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {ConsumableComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        logger.info("Activating food");
        BlockComponent block = entity.getComponent(BlockComponent.class);
        ConsumableComponent consum = entity.getComponent(ConsumableComponent.class);
        EntityRef player =event.getInstigator();
        
        //reduce blocks uses
        consum.uses-=1;
        
        //eat it
        player.send(new EatEvent(player,consum.filling));
        
        //destroy it no more uses
        if(consum.uses<=0){
        	Block currentBlock = worldProvider.getBlock(block.getPosition());
        	worldProvider.setBlock(block.getPosition(), BlockManager.getInstance().getAir(), currentBlock);
        	entity.destroy();
        }
        
    }
    
}

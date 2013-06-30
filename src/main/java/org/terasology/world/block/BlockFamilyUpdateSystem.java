/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.world.block;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.WorldProvider;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockFamilyUpdateSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(BlockFamilyUpdateSystem.class);

    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    public BlockFamilyUpdateSystem() {
        logger.info("Creating system");
    }

    @Override
    public void update(float delta) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void initialise() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void blockUpdate(OnChangedBlock event, EntityRef blockEntity) {
        for (Side side : Side.values()) {
            Vector3i neighborLocation = new Vector3i(event.getBlockPosition());
            neighborLocation.add(side.getVector3i());
            Block neighborBlock = worldProvider.getBlock(neighborLocation);
            Block neighborBlockAfterUpdate = neighborBlock.getBlockFamily().getBlockForNeighborUpdate(worldProvider, blockEntityRegistry, neighborLocation, neighborBlock);
            if (neighborBlock != neighborBlockAfterUpdate) {
                worldProvider.setBlock(neighborLocation, neighborBlockAfterUpdate, neighborBlock);
            }
        }
    }
}

package org.terasology.signalling.blockFamily;

import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Direction;
import org.terasology.math.Vector3i;
import org.terasology.signalling.components.SignalConductorComponent;
import org.terasology.signalling.components.SignalConsumerComponent;
import org.terasology.signalling.components.SignalProducerComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.family.ConnectToSixSidesFamilyFactory;
import org.terasology.world.block.family.ConnectionCondition;
import org.terasology.world.block.family.RegisterBlockFamilyFactory;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterBlockFamilyFactory("cable")
public class SignalCableBlockFamilyFactory extends ConnectToSixSidesFamilyFactory {
    public SignalCableBlockFamilyFactory() {
        super(
                new ConnectionCondition() {
                    @Override
                    public boolean isConnectingTo(Vector3i blockLocation, Direction connectDirection, WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry) {
                        Vector3i neighborLocation = new Vector3i(blockLocation);
                        neighborLocation.add(connectDirection.getVector3i());
                        EntityRef neighborEntity = blockEntityRegistry.getBlockEntityAt(neighborLocation);
                        return (neighborEntity != null &&
                                (neighborEntity.hasComponent(SignalConductorComponent.class)
                                        || neighborEntity.hasComponent(SignalProducerComponent.class)
                                || neighborEntity.hasComponent(SignalConsumerComponent.class)));
                    }
                });
    }
}

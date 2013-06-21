package org.terasology.signalling.blockFamily;

import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Direction;
import org.terasology.math.DirectionsUtil;
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
                        final EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(blockLocation);

                        final SignalConductorComponent conductorComponent = blockEntity.getComponent(SignalConductorComponent.class);
                        byte cableConnections = conductorComponent.connectionSides;
                        // Cable cannot be connected to that side
                        if (!DirectionsUtil.hasDirection(cableConnections, connectDirection))
                            return false;

                        EntityRef neighborEntity = blockEntityRegistry.getBlockEntityAt(neighborLocation);
                        return blockEntity != null && neighborEntity != null &&
                                connectsToNeighbor(connectDirection, neighborEntity);
                    }
                });
    }

    private static boolean connectsToNeighbor(Direction connectDirection, EntityRef neighborEntity) {
        final Direction oppositeDirection = connectDirection.reverse();

        final SignalConductorComponent neighborConductorComponent = neighborEntity.getComponent(SignalConductorComponent.class);
        if (neighborConductorComponent != null && DirectionsUtil.hasDirection(neighborConductorComponent.connectionSides, oppositeDirection))
            return true;

        final SignalConsumerComponent neighborConsumerComponent = neighborEntity.getComponent(SignalConsumerComponent.class);
        if (neighborConsumerComponent != null && DirectionsUtil.hasDirection(neighborConsumerComponent.connectionSides, oppositeDirection))
            return true;

        final SignalProducerComponent neighborProducerComponent = neighborEntity.getComponent(SignalProducerComponent.class);
        if (neighborProducerComponent != null && DirectionsUtil.hasDirection(neighborProducerComponent.connectionSides, oppositeDirection))
            return true;

        return false;
    }
}

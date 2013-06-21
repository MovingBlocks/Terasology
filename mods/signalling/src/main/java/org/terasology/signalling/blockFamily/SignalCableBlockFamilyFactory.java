package org.terasology.signalling.blockFamily;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Direction;
import org.terasology.math.Sides;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.signalling.components.SignalConductorComponent;
import org.terasology.signalling.components.SignalConsumerComponent;
import org.terasology.signalling.components.SignalProducerComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.ConnectToSixSidesFamilyFactory;
import org.terasology.world.block.family.ConnectionCondition;
import org.terasology.world.block.family.RegisterBlockFamilyFactory;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterBlockFamilyFactory("cable")
public class SignalCableBlockFamilyFactory extends ConnectToSixSidesFamilyFactory {
    public SignalCableBlockFamilyFactory() {
        super(new SignalCableConnectionCondition(), (byte) 63);
    }

    private static class SignalCableConnectionCondition implements ConnectionCondition {
        @Override
        public boolean isConnectingTo(Vector3i blockLocation, Side connectSide, WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry) {
            Vector3i neighborLocation = new Vector3i(blockLocation);
            neighborLocation.add(connectSide.getVector3i());

            EntityRef neighborEntity = blockEntityRegistry.getBlockEntityAt(neighborLocation);
            return neighborEntity != null && connectsToNeighbor(connectSide, neighborEntity);
        }

        private boolean connectsToNeighbor(Side connectSide, EntityRef neighborEntity) {
            final Side oppositeDirection = connectSide.reverse();

            final SignalConductorComponent neighborConductorComponent = neighborEntity.getComponent(SignalConductorComponent.class);
            if (neighborConductorComponent != null && Sides.hasSide(neighborConductorComponent.connectionSides, oppositeDirection))
                return true;

            final SignalConsumerComponent neighborConsumerComponent = neighborEntity.getComponent(SignalConsumerComponent.class);
            if (neighborConsumerComponent != null && Sides.hasSide(neighborConsumerComponent.connectionSides, oppositeDirection))
                return true;

            final SignalProducerComponent neighborProducerComponent = neighborEntity.getComponent(SignalProducerComponent.class);
            if (neighborProducerComponent != null && Sides.hasSide(neighborProducerComponent.connectionSides, oppositeDirection))
                return true;

            return false;
        }
    }
}

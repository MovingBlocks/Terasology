package org.terasology.signalling.componentSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.math.Vector3i;
import org.terasology.signalling.components.*;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.OneCrucialSideFamily;

import java.util.EnumMap;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ScrewdriverSystem implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ScrewdriverSystem.class);
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private EnumMap<Side, Side> sideOrder = new EnumMap<>(Side.class);

    @Override
    public void initialise() {
        sideOrder.put(Side.FRONT, Side.LEFT);
        sideOrder.put(Side.LEFT, Side.BACK);
        sideOrder.put(Side.BACK, Side.RIGHT);
        sideOrder.put(Side.RIGHT, Side.TOP);
        sideOrder.put(Side.TOP, Side.BOTTOM);
        sideOrder.put(Side.BOTTOM, Side.FRONT);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {ScrewdriverComponent.class})
    public void rotateGate(ActivateEvent event, EntityRef screwdriver) {
        final EntityRef target = event.getTarget();
        if (target.hasComponent(SignalGateComponent.class)) {
            final Vector3i targetLocation = new Vector3i(event.getTargetLocation());
            final Block block = worldProvider.getBlock(targetLocation);
            final BlockFamily blockFamily = block.getBlockFamily();
            if (blockFamily instanceof OneCrucialSideFamily) {
                final OneCrucialSideFamily gateBlockFamily = (OneCrucialSideFamily) blockFamily;
                final Side currentSide = gateBlockFamily.getBlockSide(block);
                final Side newSide = sideOrder.get(currentSide);

                if (worldProvider.setBlock(targetLocation, gateBlockFamily.getBlockForSide(newSide), block)) {
                    final EntityRef gateEntity = blockEntityRegistry.getBlockEntityAt(targetLocation);

                    final SignalProducerComponent signalProducer = gateEntity.getComponent(SignalProducerComponent.class);
                    final SignalConsumerComponent signalConsumer = gateEntity.getComponent(SignalConsumerComponent.class);

                    signalConsumer.connectionSides = 0;
                    gateEntity.saveComponent(signalConsumer);

                    final byte newSideBit = SideBitFlag.getSide(newSide);
                    signalProducer.connectionSides = newSideBit;
                    signalConsumer.connectionSides = (byte) (63 - newSideBit);

                    gateEntity.saveComponent(signalProducer);
                    gateEntity.saveComponent(signalConsumer);

                    if (newSide != Side.FRONT) {
                        gateEntity.addComponent(new SignalGateRotatedComponent());
                    }
                }
            }
        }
    }
}

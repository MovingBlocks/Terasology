package org.terasology.signalling.components;

import org.terasology.entitySystem.Component;
import org.terasology.world.block.RequiresBlockLifecycleEvents;

@RequiresBlockLifecycleEvents
public class SignalProducerComponent implements Component {
    public byte connectionSides;
    public int signalStrength;
}

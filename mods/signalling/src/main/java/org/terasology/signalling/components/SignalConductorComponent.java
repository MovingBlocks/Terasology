package org.terasology.signalling.components;

import org.terasology.entitySystem.Component;
import org.terasology.world.block.RequiresBlockLifecycleEvents;

@RequiresBlockLifecycleEvents
public class SignalConductorComponent implements Component {
    public byte connectionSides;
}

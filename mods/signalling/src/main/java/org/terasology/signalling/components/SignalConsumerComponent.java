package org.terasology.signalling.components;

import org.terasology.entitySystem.Component;
import org.terasology.world.block.RequiresBlockLifecycleEvents;

@RequiresBlockLifecycleEvents
public class SignalConsumerComponent implements Component {
    public byte connectionSides;
    public SignalConsumerComponent.Mode mode = Mode.AT_LEAST_ONE;

    public enum Mode {
        AT_LEAST_ONE, ALL_CONNECTED
    }
}

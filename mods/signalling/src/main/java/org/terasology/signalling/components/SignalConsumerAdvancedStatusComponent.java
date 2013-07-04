package org.terasology.signalling.components;

import org.terasology.entitySystem.Component;

public class SignalConsumerAdvancedStatusComponent implements Component {
    public byte sidesWithSignals;
    public byte sidesWithoutSignals;
}

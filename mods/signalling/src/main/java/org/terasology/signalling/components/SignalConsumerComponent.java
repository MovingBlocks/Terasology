package org.terasology.signalling.components;

import org.terasology.entitySystem.Component;

public class SignalConsumerComponent implements Component {
    public byte connectionSides;
    public boolean hasSignal;
}

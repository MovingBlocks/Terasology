package org.terasology.game.client;

import org.terasology.entitySystem.Event;


public interface ReusableEvent extends Event {

    void reset();
}

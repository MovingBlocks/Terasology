package org.terasology.signalling.gui;

import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ServerEvent
public class SetSignalDelayEvent extends NetworkEvent {
    private long time;

    public SetSignalDelayEvent(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}

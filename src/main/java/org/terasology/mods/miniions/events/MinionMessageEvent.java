package org.terasology.mods.miniions.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.mods.miniions.utilities.MinionMessage;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 24/05/12
 * Time: 4:03
 * Message event : send info to player message queue
 */
public class MinionMessageEvent extends AbstractEvent {

    private MinionMessage minionMessage;

    public MinionMessageEvent() {
        minionMessage = null;
    }

    public MinionMessageEvent(MinionMessage minionmessage) {
        minionMessage = minionmessage;
    }

    public MinionMessage getMinionMessage() {
        return minionMessage;
    }
}

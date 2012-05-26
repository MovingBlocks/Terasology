package org.terasology.mods.miniions.utilities;

import org.terasology.entitySystem.EntityRef;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 24/05/12
 * Time: 22:28
 * To change this template use File | Settings | File Templates.
 */
public class MinionMessage implements Comparable<MinionMessage>{

    private MinionMessageType minionMessageType;
    private String messageTitle;
    private String messageDescription;
    private String messageContent;
    private EntityRef Minion;
    private EntityRef Player;
    private int index;

    public MinionMessage(MinionMessageType minionmessagetype, String messagetitle, String messagedescription, String messagecontent, EntityRef minion, EntityRef player){
        minionMessageType = minionmessagetype;
        messageTitle = messagetitle;
        messageDescription = messagedescription;
        messageContent = messagecontent;
        Minion = minion;
        Player = player;
    }

    public MinionMessageType getMinionMessageType() {
        return minionMessageType;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public String getMessageDescription() {
        return messageDescription;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public EntityRef getMinion() {
        return Minion;
    }

    public EntityRef getPlayer() {
        return Player;
    }

    @Override
    public int compareTo(MinionMessage o) {
        if(this.getMinionMessageType().ordinal() < o.getMinionMessageType().ordinal())
            return -1;
        else if (this.getMinionMessageType().ordinal() > o.getMinionMessageType().ordinal())
            return 1;
        return 0;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}

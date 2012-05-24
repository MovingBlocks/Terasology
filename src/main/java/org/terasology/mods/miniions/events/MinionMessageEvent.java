package org.terasology.mods.miniions.events;

import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

import javax.vecmath.Vector3f;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 24/05/12
 * Time: 4:03
 * Message event : send info to player message queue
 */
public class MinionMessageEvent extends AbstractEvent {
    private EntityRef instigator;
    private EntityRef target;
    private Vector3f origin;

    private MessageType messageType;

    private String[] messageContent;

    public enum MessageType {
        Urgent, //red
        Normal, //white
        Info,   //blue
        Debug   //yellow
    }

    public MinionMessageEvent(EntityRef target, EntityRef instigator, MessageType messageType, String[] messageContent) {
        this(target, instigator, new Vector3f(), messageType, messageContent);
    }

    public MinionMessageEvent(EntityRef target, EntityRef instigator, Vector3f origin, MessageType messageType, String[] messageContent) {
        this.instigator = instigator;
        this.target = target;
        this.origin = origin;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String[] getMessageContent() {
        return messageContent;
    }

    public EntityRef getTarget() {
        return target;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getInstigatorLocation() {
        LocationComponent loc = instigator.getComponent(LocationComponent.class);
        if (loc != null) {
            return loc.getWorldPosition();
        }
        return new Vector3f();
    }
}

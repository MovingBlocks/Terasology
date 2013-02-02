package org.terasology.entitySystem.metadata;

import org.terasology.entitySystem.Event;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.OwnerEvent;
import org.terasology.network.ServerEvent;

/**
 * @author Immortius
 */
public class EventMetadata<T extends Event> extends ClassMetadata<T> {

    private NetworkEventType networkEventType;
    private String uri;
    private boolean lagCompensated = false;
    private boolean skipInstigator = false;

    public EventMetadata(Class<T> simpleClass, String uri) throws NoSuchMethodException {
        super(simpleClass, uri);
        this.uri = uri;
        if (simpleClass.getAnnotation(ServerEvent.class) != null) {
            networkEventType = NetworkEventType.SERVER;
            lagCompensated = simpleClass.getAnnotation(ServerEvent.class).lagCompensate();
        } else if (simpleClass.getAnnotation(OwnerEvent.class) != null) {
            networkEventType = NetworkEventType.OWNER;
        } else if (simpleClass.getAnnotation(BroadcastEvent.class) != null) {
            networkEventType = NetworkEventType.BROADCAST;
            skipInstigator = simpleClass.getAnnotation(BroadcastEvent.class).skipInstigator();
        }
    }

    public boolean isNetworkEvent() {
        return networkEventType != null;
    }

    public boolean isLagCompensated() {
        return lagCompensated;
    }

    public boolean isSkipInstigator() {
        return skipInstigator;
    }

    public NetworkEventType getNetworkEventType() {
        return networkEventType;
    }

    public String getId() {
        return uri;
    }
}

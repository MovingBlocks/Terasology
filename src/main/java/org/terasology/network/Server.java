package org.terasology.network;

import org.jboss.netty.channel.Channel;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.protobuf.NetData;

/**
 * Used to interact with a remote server (from client end)
 *
 * @author Immortius
 */
public class Server {
    private Channel channel;
    private EventSerializer eventSerializer;

    public Server(Channel channel) {
        this.channel = channel;
    }

    void setEventSerializer(EventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

    public void send(Event event, int targetId) {
        NetData.NetMessage message = NetData.NetMessage.newBuilder()
                .setType(NetData.NetMessage.Type.EVENT)
                .setEvent(NetData.EventMessage.newBuilder()
                        .setEvent(eventSerializer.serialize(event))
                        .setTargetId(targetId))
                .build();
        channel.write(message);
    }

}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.protobuf.NetData;
import org.terasology.engine.world.chunks.remoteChunkProvider.ChunkReadyListener;

/**
 */
public interface Server extends ChunkReadyListener {

    EntityRef getClientEntity();

    String getRemoteAddress();

    ServerInfoMessage getInfo();

    void send(Event event, EntityRef target);

    void update(boolean netTick);

    void queueMessage(NetData.NetMessage message);

    void setComponentDirty(int netId, Class<? extends Component> componentType);

    NetMetricSource getMetrics();

}

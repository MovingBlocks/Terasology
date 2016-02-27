/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.network;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.protobuf.NetData;
import org.terasology.world.chunks.remoteChunkProvider.ChunkReadyListener;

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

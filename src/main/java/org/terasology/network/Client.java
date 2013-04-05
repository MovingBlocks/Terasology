/*
 * Copyright 2013 Moving Blocks
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

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.world.chunks.ChunkRegionListener;

/**
 * @author Immortius
 */
public interface Client extends ChunkRegionListener {
    boolean isAwaitingConnectMessage();

    String getName();

    void disconnect();

    void update(boolean netTick);

    EntityRef getEntity();

    void send(Event event, EntityRef target);

    int getViewDistance();

    boolean isLocal();

}

/*
 * Copyright 2014 MovingBlocks
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

/**
 * The component that marks an entity as being a Client Info Entity.
 */
@Replicate
public final class ClientInfoComponent implements Component {

    /**
     * When a client connects, the game searches a client info component for the client id ({@link Client#getId()}).
     * If it finds one it is gets reused, otherwise a new one will be created.
     *
     * The field does not get replicated as there is no need to tell the clients the player ids.
     *
     */
    @NoReplicate
    public String playerId;

    /**
     * Set to the client entity if it is connected, otherwise it is EntityRef.NULL.
     */
    @Replicate
    public EntityRef client = EntityRef.NULL;
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.nui.Color;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.chunks.ChunkRegionListener;

/**
 * A client is the connection between a player (local or remote) and the game.
 *
 */
public interface Client extends ChunkRegionListener {

    String getName();

    String getId();

    Color getColor();

    void disconnect();

    void update(boolean netTick);

    EntityRef getEntity();

    void send(Event event, EntityRef target);

    ViewDistance getViewDistance();

    boolean isLocal();

    void setViewDistanceMode(ViewDistance viewDistance);
}

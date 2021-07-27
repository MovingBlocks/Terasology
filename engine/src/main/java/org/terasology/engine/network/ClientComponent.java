// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * The component that marks an entity as being a Client Entity (essentially, a player) and ties them to a
 * client info entity (for replicated information) and character entity (their body).
 *
 */
public class ClientComponent implements Component {
    public boolean local;

    @Replicate
    public EntityRef clientInfo = EntityRef.NULL;

    @Replicate
    public EntityRef character = EntityRef.NULL;

    public EntityRef camera = EntityRef.NULL;
}

// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * PingStockComponent stock the ping information of one user.
 * <p>
 * Might be used to stock ping information and display it in future.
 */
public final class PingComponent implements Component<PingComponent> {

    @Replicate
    public Map<EntityRef, Long> pings = new HashMap<>();

    public void setValues(Map<EntityRef, Long> values) {
        pings.clear();
        pings.putAll(values);
    }

    public Map<EntityRef, Long> getValues() {
        return new HashMap<>(pings);
    }

    @Override
    public void copyFrom(PingComponent other) {
        this.setValues(other.getValues());
    }
}

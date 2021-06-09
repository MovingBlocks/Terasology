// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PingStockComponent stock the ping information of one user.
 * <p>
 * Might be used to stock ping information and display it in future.
 */
public final class PingStockComponent implements Component<PingStockComponent> {

    // TODO Map<EntityRef,Long> is not supported for replication (no type handler),
    // therefore keys and values are replicated via lists.
    // Not the best solution for performance but for <100 players and low update rates it should do the job

    @Replicate
    private List<EntityRef> pingKeys = new ArrayList<>();
    @Replicate
    private List<Long> pingValues = new ArrayList<>();

    public void setValues(Map<EntityRef, Long> values) {
        pingKeys.clear();
        pingValues.clear();
        for (Map.Entry<EntityRef, Long> entry : values.entrySet()) {
            pingKeys.add(entry.getKey());
            pingValues.add(entry.getValue());
        }
    }

    public Map<EntityRef, Long> getValues() {
        Map<EntityRef, Long> returnValues = new HashMap<>();
        for (int i = 0; i < pingKeys.size(); i++) {
            returnValues.put(pingKeys.get(i), pingValues.get(i));
        }
        return returnValues;
    }

    @Override
    public void copy(PingStockComponent other) {
        this.setValues(other.getValues());
    }
}

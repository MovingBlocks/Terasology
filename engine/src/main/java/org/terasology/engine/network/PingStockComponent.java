/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.engine.network;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PingStockComponent stock the ping information of one user.
 * <p>
 * Might be used to stock ping information and display it in future.
 */
public final class PingStockComponent implements Component {

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
}

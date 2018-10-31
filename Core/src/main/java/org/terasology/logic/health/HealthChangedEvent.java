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

package org.terasology.logic.health;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * This event (or a subtype) is sent whenever health changes
 *
 */
public class HealthChangedEvent implements Event {
    private EntityRef instigator;
    private int change;

    public HealthChangedEvent(EntityRef instigator, int change) {
        this.instigator = instigator;
        this.change = change;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    /**
     * @return The amount by which health changed. This is capped, so if the entity received 9999 damage and only had 10 health, it will be -10.
     */
    public int getHealthChange() {
        return change;
    }
}

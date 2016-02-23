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
import org.terasology.entitySystem.prefab.Prefab;

/**
 * This event is sent after damage has been dealt to an entity.
 *
 */
public class OnDamagedEvent extends HealthChangedEvent {
    private Prefab damageType;
    private int fullAmount;

    public OnDamagedEvent(int fullAmount, int change, Prefab damageType, EntityRef instigator) {
        super(instigator, change);
        this.fullAmount = fullAmount;
        this.damageType = damageType;
    }

    public int getDamageAmount() {
        return fullAmount;
    }

    public Prefab getType() {
        return damageType;
    }

}

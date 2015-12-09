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
import org.terasology.entitySystem.prefab.Prefab;

/**
 * This event should be sent to cause damage to an entity.
 *
 */
public class DoDamageEvent implements Event {
    private int amount;
    private Prefab damageType;
    private EntityRef instigator;
    private EntityRef directCause;

    public DoDamageEvent(int amount) {
        this(amount, EngineDamageTypes.DIRECT.get());
    }

    public DoDamageEvent(int amount, Prefab damageType) {
        this(amount, damageType, EntityRef.NULL);
    }

    public DoDamageEvent(int amount, Prefab damageType, EntityRef instigator) {
        this(amount, damageType, instigator, EntityRef.NULL);
    }

    /**
     * @param amount     The amount of damage being caused
     * @param damageType The type of the damage being dealt
     * @param instigator The instigator of the damage (which entity caused it)
     * @param directCause       Tool used to cause the damage
     */
    public DoDamageEvent(int amount, Prefab damageType, EntityRef instigator, EntityRef directCause) {
        this.amount = amount;
        this.damageType = damageType;
        this.instigator = instigator;
        this.directCause = directCause;
    }

    public int getAmount() {
        return amount;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public Prefab getDamageType() {
        return damageType;
    }

    public EntityRef getDirectCause() {
        return directCause;
    }
}

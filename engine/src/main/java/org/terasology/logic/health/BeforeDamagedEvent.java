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
import org.terasology.entitySystem.event.AbstractConsumableValueModifiableEvent;
import org.terasology.entitySystem.prefab.Prefab;

/**
 * This event is sent to allow damage to be modified or cancelled, before it is processed.
 * <br><br>
 * Damage modifications are accumulated as additions/subtractions (modifiers) and multipliers.
 *
 */
public class BeforeDamagedEvent extends AbstractConsumableValueModifiableEvent {
    private Prefab damageType;
    private EntityRef instigator;
    private EntityRef directCause;

    public BeforeDamagedEvent(int baseDamage, Prefab damageType, EntityRef instigator, EntityRef directCause) {
        super(baseDamage);
        this.damageType = damageType;
        this.instigator = instigator;
        this.directCause = directCause;
    }

    public Prefab getDamageType() {
        return damageType;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getDirectCause() {
        return directCause;
    }
}

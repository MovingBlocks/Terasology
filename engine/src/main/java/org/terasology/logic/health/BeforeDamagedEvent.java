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

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ConsumableEvent;
import org.terasology.entitySystem.prefab.Prefab;

/**
 * This event is sent to allow damage to be modified or cancelled, before it is processed.
 * <p/>
 * Damage modifications are accumulated as additions/subtractions (modifiers) and multipliers.
 *
 * @author Immortius
 */
public class BeforeDamagedEvent implements ConsumableEvent {
    private boolean consumed;

    private int baseDamage;
    private Prefab damageType;
    private EntityRef instigator;
    private EntityRef directCause;

    private TFloatList multipliers = new TFloatArrayList();
    private TIntList modifiers = new TIntArrayList();

    public BeforeDamagedEvent(int baseDamage, Prefab damageType, EntityRef instigator, EntityRef directCause) {
        this.baseDamage = baseDamage;
        this.damageType = damageType;
        this.instigator = instigator;
        this.directCause = directCause;
    }

    public int getBaseDamage() {
        return baseDamage;
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

    public TFloatList getMultipliers() {
        return multipliers;
    }

    public TIntList getModifiers() {
        return modifiers;
    }

    public void multiply(float amount) {
        this.multipliers.add(amount);
    }

    public void add(int amount) {
        modifiers.add(amount);
    }

    public void subtract(int amount) {
        modifiers.add(-amount);
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public void consume() {
        consumed = true;
    }
}

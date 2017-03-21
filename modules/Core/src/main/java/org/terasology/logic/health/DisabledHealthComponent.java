/*
 * Copyright 2016 MovingBlocks
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

import org.terasology.entitySystem.Component;


public final class DisabledHealthComponent extends HealthComponent {

    public DisabledHealthComponent(){
    }

    public DisabledHealthComponent(HealthComponent health){
        this.maxHealth = health.maxHealth;
        this.regenRate = health.regenRate;
        this.waitBeforeRegen = health.waitBeforeRegen;

        this.fallingDamageSpeedThreshold = health.fallingDamageSpeedThreshold;
        this.horizontalDamageSpeedThreshold = health.horizontalDamageSpeedThreshold;
        this.excessSpeedDamageMultiplier = health.excessSpeedDamageMultiplier;

        this.currentHealth = health.currentHealth;

        this.nextRegenTick = health.nextRegenTick;
        this.destroyEntityOnNoHealth = health.destroyEntityOnNoHealth;
    }
}




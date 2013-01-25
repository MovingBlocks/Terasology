/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.components;

import org.terasology.entitySystem.Component;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class HealthComponent implements Component {
    // Configuration options
    public int maxHealth = 20;
    public float regenRate = 0.0f;
    public float waitBeforeRegen = 0.0f;

    public float fallingDamageSpeedThreshold = 20f;
    public float crashingDamageSpeedThreshold = 4.5f;
    public float excessSpeedDamageMultiplier = 10f;

    public int currentHealth = 20;

    // Regen info
    public float timeSinceLastDamage = 0.0f;
    public float partialRegen = 0.0f;

    public HealthComponent() {
    }

    public HealthComponent(int maxHealth, float regenRate, float waitBeforeRegen) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.regenRate = regenRate;
        this.waitBeforeRegen = waitBeforeRegen;
    }
}

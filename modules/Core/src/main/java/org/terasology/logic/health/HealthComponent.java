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
import org.terasology.network.Replicate;
import org.terasology.rendering.nui.properties.TextField;

/**
 */
public final class HealthComponent implements Component {
    // Configuration options
    @Replicate
    public int maxHealth = 20;
    @Replicate
    public float regenRate;
    @Replicate
    public float waitBeforeRegen;

    // TODO: Should these be in a separate component?
    @Replicate
    public float fallingDamageSpeedThreshold = 20;
    @Replicate
    public float horizontalDamageSpeedThreshold = 100;
    @Replicate
    public float excessSpeedDamageMultiplier = 10f;

    @Replicate
    @TextField
    public int currentHealth = 20;

    // Regen info
    public long nextRegenTick;

    public boolean destroyEntityOnNoHealth;

    public HealthComponent() {
    }

    public HealthComponent(int maxHealth, float regenRate, float waitBeforeRegen) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.regenRate = regenRate;
        this.waitBeforeRegen = waitBeforeRegen;
    }
}

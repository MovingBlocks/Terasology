package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class HealthComponent extends AbstractComponent {
    // Configuration options
    public int maxHealth = 20;
    public float regenRate = 0.0f;
    public float waitBeforeRegen = 0.0f;

    public float fallingDamageSpeedThreshold = 20;
    public float excessSpeedDamageMultiplier = 10f;

    public int currentHealth = 20;

    // Regen info
    public float timeSinceLastDamage = 0.0f;
    public float partialRegen = 0.0f;

    public HealthComponent() {}

    public HealthComponent(int maxHealth, float regenRate, float waitBeforeRegen) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.regenRate = regenRate;
        this.waitBeforeRegen = waitBeforeRegen;
    }
}

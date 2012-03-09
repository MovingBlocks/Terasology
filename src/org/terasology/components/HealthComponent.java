package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class HealthComponent extends AbstractComponent {
    public int maxHealth = 20;
    public int currentHealth = 20;

    public float regenRate = 0.0f;
    public float waitBeforeRegen = 0.0f;
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

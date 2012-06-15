package org.terasology.componentSystem.common;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.HealthComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.DamageEvent;
import org.terasology.events.FullHealthEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.events.VerticalCollisionEvent;
import org.terasology.game.CoreRegistry;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem(authorativeOnly = true)
public class HealthSystem implements EventHandlerSystem, UpdateSubscriberSystem {

    private EntityManager entityManager;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(HealthComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health.currentHealth <= 0) continue;

            if (health.currentHealth == health.maxHealth || health.regenRate == 0)
                continue;

            health.timeSinceLastDamage += delta;
            if (health.timeSinceLastDamage >= health.waitBeforeRegen) {
                health.partialRegen += delta * health.regenRate;
                if (health.partialRegen >= 1) {
                    health.currentHealth = Math.min(health.maxHealth, health.currentHealth + (int) health.partialRegen);
                    health.partialRegen %= 1f;
                    if (health.currentHealth == health.maxHealth) {
                        entity.send(new FullHealthEvent());
                    }
                }
            }
            entity.saveComponent(health);
        }
    }

    @ReceiveEvent(components = {HealthComponent.class})
    public void onDamage(DamageEvent event, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);
        applyDamage(entity, health, event.getAmount(), event.getInstigator());
    }

    @ReceiveEvent(components = {HealthComponent.class})
    public void onLand(VerticalCollisionEvent event, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);

        if (event.getVelocity().y < 0 && -event.getVelocity().y > health.fallingDamageSpeedThreshold) {
            int damage = (int) ((-event.getVelocity().y - health.fallingDamageSpeedThreshold) * health.excessSpeedDamageMultiplier);
            if (damage > 0) {
                applyDamage(entity, health, damage, null);
            }
        }
    }

    private void applyDamage(EntityRef entity, HealthComponent health, int damageAmount, EntityRef instigator) {
        if (health.currentHealth <= 0) return;

        health.timeSinceLastDamage = 0;
        health.currentHealth -= damageAmount;
        if (health.currentHealth <= 0) {
            entity.send(new NoHealthEvent(instigator));
        }
        entity.saveComponent(health);
    }
}

package org.terasology.logic.systems;

import org.terasology.components.HealthComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.componentSystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.componentSystem.UpdateSubscriberSystem;
import org.terasology.events.DamageEvent;
import org.terasology.events.FullHealthEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.CoreRegistry;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class HealthSystem implements EventHandlerSystem, UpdateSubscriberSystem {

    private EntityManager entityManager;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    public void update(float delta) {
        float deltaSeconds = delta / 1000;
        
        for (EntityRef entity : entityManager.iteratorEntities(HealthComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);

            if (health.currentHealth < health.maxHealth && health.regenRate > 0) {
                health.timeSinceLastDamage += deltaSeconds;
                if (health.timeSinceLastDamage >= health.waitBeforeRegen) {
                    health.partialRegen += deltaSeconds * health.regenRate;
                    if (health.partialRegen >= 1) {
                        health.currentHealth = Math.min(health.maxHealth, health.currentHealth + (int)health.partialRegen);
                        health.partialRegen %= 1f;
                        if (health.currentHealth == health.maxHealth) {
                            entity.send(new FullHealthEvent());
                        }
                    }
                }
                entity.saveComponent(health);
            }
        }
    }
    
    @ReceiveEvent(components = {HealthComponent.class})
    public void onDamage(DamageEvent event, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);
        health.timeSinceLastDamage = 0;
        health.currentHealth -= event.getAmount();
        if (health.currentHealth <= 0) {
            entity.send(new NoHealthEvent());
        }
        entity.saveComponent(health);
    }
}

/*
 * Copyright 2013 Moving Blocks
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

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.engine.CoreRegistry;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class HealthSystem implements ComponentSystem, UpdateSubscriberSystem {

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
                        entity.send(new FullHealthEvent(entity, health.maxHealth));
                    } else {
                        entity.send(new HealthChangedEvent(entity, health.currentHealth, health.maxHealth));
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
        if (health.currentHealth <= 0) {
            return;
        }
        health.timeSinceLastDamage = 0;
        health.currentHealth -= damageAmount;
        if (health.currentHealth <= 0) {
            entity.send(new NoHealthEvent(instigator, health.maxHealth));
        } else {
            entity.send(new HealthChangedEvent(instigator, health.currentHealth, health.maxHealth));
        }
        entity.saveComponent(health);
    }
}

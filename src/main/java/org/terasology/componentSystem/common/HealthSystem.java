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
package org.terasology.componentSystem.common;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.HealthComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.DamageEvent;
import org.terasology.events.FullHealthEvent;
import org.terasology.events.HealthChangedEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.events.VerticalCollisionEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.types.GameType;
import org.terasology.math.TeraMath;

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
    
    @ReceiveEvent(components = {HealthComponent.class})
    public void onCrash(HorizontalCollisionEvent event, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);

        float velocity = (TeraMath.fastAbs(event.getVelocity().x)+TeraMath.fastAbs(event.getVelocity().z))/2;        
        
        if (velocity > health.crashingDamageSpeedThreshold) {
            int damage = (int) ((velocity - health.crashingDamageSpeedThreshold) * health.excessSpeedDamageMultiplier);
            if (damage > 0) {
                applyDamage(entity, health, damage, null);
            }
        }
    }
    
    

    private void applyDamage(EntityRef entity, HealthComponent health, int damageAmount, EntityRef instigator) {
        CoreRegistry.get(GameType.class).onPlayerDamageHook(entity, health, damageAmount, instigator);
    }
}

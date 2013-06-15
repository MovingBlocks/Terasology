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

import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.engine.CoreRegistry;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.network.ClientComponent;

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
        for (EntityRef entity : entityManager.getEntitiesWith(HealthComponent.class)) {
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
    public void onHeal(HealEvent event, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);
        applyHealing(entity, health, event.getAmount(), event.getInstigator());
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
        if (health.currentHealth <= 0 || damageAmount <= 0) {
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

    private void applyHealing(EntityRef entity, HealthComponent health, int healAmount, EntityRef instigator) {
        if (health.currentHealth <= 0 || healAmount <= 0) {
            return;
        }
        health.currentHealth += healAmount;
        if (health.currentHealth >= health.maxHealth) {
            health.currentHealth = health.maxHealth;
            entity.send(new FullHealthEvent(instigator, health.maxHealth));
        } else {
            entity.send(new HealthChangedEvent(instigator, health.currentHealth, health.maxHealth));
        }
        entity.saveComponent(health);
    }

    // Debug commands
    @Command(shortDescription = "Reduce the player's health by an amount", runOnServer = true)
    public void damage(@CommandParam("amount") int amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new DamageEvent(amount, clientComp.character));
    }

    @Command(shortDescription = "Restores your health to max", runOnServer = true)
    public String health(EntityRef clientEntity) {
        ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
        clientComp.character.send(new HealEvent(100000, clientComp.character));
        return "Health restored";
    }

    @Command(shortDescription = "Restores your health by an amount", runOnServer = true)
    public void health(@CommandParam("amount") int amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new HealEvent(amount, clientComp.character));
    }

    @Command(shortDescription = "Set max health", runOnServer = true)
    public void setMaxHealth(@CommandParam("max") int max, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            health.maxHealth = max;
            health.currentHealth = health.maxHealth;
            clientComp.character.saveComponent(health);
            clientComp.character.send(new FullHealthEvent(clientComp.character, health.maxHealth));
        }
    }

    @Command(shortDescription = "Set regen rate", runOnServer = true)
    public void setRegenRaterate(@CommandParam("rate") float rate, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            health.regenRate = rate;
            clientComp.character.saveComponent(health);
        }
    }

    @Command(shortDescription = "Show your health")
    public String showHealth(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            return "Your health:" + health.currentHealth + " max:" + health.maxHealth + " regen:" + health.regenRate + " partRegen:" + health.partialRegen;
        }
        return "I guess you're dead?";
    }
}

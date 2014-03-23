/*
 * Copyright 2013 MovingBlocks
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

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.math.TeraMath;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class HealthSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(HealthSystem.class);

    @In
    private EntityManager entityManager;

    @In
    private org.terasology.engine.Time time;

    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(HealthComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health.currentHealth <= 0) {
                continue;
            }

            if (health.currentHealth == health.maxHealth || health.regenRate == 0) {
                continue;
            }

            int healAmount = 0;
            while (time.getGameTimeInMs() >= health.nextRegenTick) {
                healAmount++;
                health.nextRegenTick = health.nextRegenTick + (long) (1000 / health.regenRate);
            }

            if (healAmount > 0) {
                checkHeal(entity, healAmount, entity, health);
                entity.saveComponent(health);
            }
        }
    }

    private void checkHeal(EntityRef entity, int healAmount, EntityRef instigator) {
        checkHeal(entity, healAmount, instigator, null);
    }

    private void checkHeal(EntityRef entity, int healAmount, EntityRef instigator, HealthComponent health) {
        BeforeHealEvent beforeHeal = entity.send(new BeforeHealEvent(healAmount, instigator));
        if (!beforeHeal.isConsumed()) {
            int modifiedAmount = calculateTotal(beforeHeal.getBaseHeal(), beforeHeal.getMultipliers(), beforeHeal.getModifiers());
            if (modifiedAmount > 0) {
                doHeal(entity, modifiedAmount, instigator, health);
            } else if (modifiedAmount < 0) {
                doDamage(entity, -modifiedAmount, EngineDamageTypes.HEALING.get(), instigator, EntityRef.NULL, health);
            }
        }
    }

    private void doHeal(EntityRef entity, int healAmount, EntityRef instigator, HealthComponent targetHealthComponent) {
        HealthComponent health = targetHealthComponent;
        if (health == null) {
            health = entity.getComponent(HealthComponent.class);
        }
        int healedAmount = Math.min(health.currentHealth + healAmount, health.maxHealth) - health.currentHealth;
        health.currentHealth += healedAmount;
        entity.saveComponent(health);
        entity.send(new OnHealedEvent(healAmount, healedAmount, instigator));
        if (health.currentHealth == health.maxHealth) {
            entity.send(new FullHealthEvent(instigator));
        }
    }

    private void doDamage(EntityRef entity, int damageAmount, Prefab damageType, EntityRef instigator, EntityRef directCause, HealthComponent targetHealthComponent) {
        HealthComponent health = targetHealthComponent;
        if (health == null) {
            health = entity.getComponent(HealthComponent.class);
        }
        int damagedAmount = health.currentHealth - Math.max(health.currentHealth - damageAmount, 0);
        health.currentHealth -= damagedAmount;
        health.nextRegenTick = time.getGameTimeInMs() + TeraMath.floorToInt(health.waitBeforeRegen * 1000);
        entity.saveComponent(health);
        entity.send(new OnDamagedEvent(damageAmount, damagedAmount, damageType, instigator));
        if (health.currentHealth == 0) {
            entity.send(new DestroyEvent(instigator, directCause, damageType));
        }
    }

    @ReceiveEvent(components = {HealthComponent.class})
    public void onDamage(DoDamageEvent event, EntityRef entity) {
        checkDamage(entity, event.getAmount(), event.getDamageType(), event.getInstigator(), event.getDirectCause(), null);
    }

    private void checkDamage(EntityRef entity, int amount, Prefab damageType, EntityRef instigator, EntityRef directCause, HealthComponent health) {
        BeforeDamagedEvent beforeDamage = entity.send(new BeforeDamagedEvent(amount, damageType, instigator, directCause));
        if (!beforeDamage.isConsumed()) {
            int damageAmount = TeraMath.floorToInt(beforeDamage.getResultValue());
            if (damageAmount > 0) {
                doDamage(entity, damageAmount, damageType, instigator, directCause, health);
            } else {
                doHeal(entity, -damageAmount, instigator, health);
            }
        }
    }

    @ReceiveEvent
    public void onDestroy(DestroyEvent event, EntityRef entity, HealthComponent healthComponent) {
        BeforeDestroyEvent destroyCheck = new BeforeDestroyEvent(event.getInstigator(), event.getDirectCause(), event.getDamageType());
        entity.send(destroyCheck);
        if (!destroyCheck.isConsumed()) {
            entity.send(new DoDestroyEvent(event.getInstigator(), event.getDirectCause(), event.getDamageType()));
            if (healthComponent.destroyEntityOnNoHealth) {
                entity.destroy();
            }
        }
    }

    private int calculateTotal(int base, TFloatList multipliers, TIntList modifiers) {
        // For now, add all modifiers and multiply by all multipliers. Negative modifiers cap to zero, but negative
        // multipliers remain (so damage can be flipped to healing)

        float total = base;
        TIntIterator modifierIter = modifiers.iterator();
        while (modifierIter.hasNext()) {
            total += modifierIter.next();
        }
        total = Math.max(0, total);
        if (total == 0) {
            return 0;
        }
        TFloatIterator multiplierIter = multipliers.iterator();
        while (multiplierIter.hasNext()) {
            total *= multiplierIter.next();
        }
        return TeraMath.floorToInt(total);

    }

    @ReceiveEvent(components = {HealthComponent.class})
    public void onHeal(DoHealEvent event, EntityRef entity) {
        checkHeal(entity, event.getAmount(), event.getInstigator());
    }

    // TODO: This should be in a separate system
    @ReceiveEvent(components = {HealthComponent.class})
    public void onLand(VerticalCollisionEvent event, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);

        if (event.getVelocity().y < 0 && -event.getVelocity().y > health.fallingDamageSpeedThreshold) {
            int damage = (int) ((-event.getVelocity().y - health.fallingDamageSpeedThreshold) * health.excessSpeedDamageMultiplier);
            if (damage > 0) {
                checkDamage(entity, damage, EngineDamageTypes.PHYSICAL.get(), EntityRef.NULL, EntityRef.NULL, health);
            }
        }
    }

    @ReceiveEvent(components = {HealthComponent.class})
    public void onCrash(HorizontalCollisionEvent event, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);

        Vector3f vel = new Vector3f(event.getVelocity());
        vel.y = 0;
        float speed = vel.length();

        if (speed > health.horizontalDamageSpeedThreshold) {
            int damage = (int) ((speed - health.horizontalDamageSpeedThreshold) * health.excessSpeedDamageMultiplier);
            if (damage > 0) {
                checkDamage(entity, damage, EngineDamageTypes.PHYSICAL.get(), EntityRef.NULL, EntityRef.NULL, health);
            }
        }
    }

    // Debug commands
    @Command(shortDescription = "Reduce the player's health by an amount", runOnServer = true)
    public String damage(@CommandParam("amount") int amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new DoDamageEvent(amount, EngineDamageTypes.DIRECT.get(), clientComp.character));
        
        return "Inflicted damage of " + amount;
    }

    @Command(shortDescription = "Restores your health to max", runOnServer = true)
    public String health(EntityRef clientEntity) {
        ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
        clientComp.character.send(new DoHealEvent(100000, clientComp.character));
        return "Health restored";
    }

    @Command(shortDescription = "Restores your health by an amount", runOnServer = true)
    public void health(@CommandParam("amount") int amount, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new DoHealEvent(amount, clientComp.character));
    }

    @Command(shortDescription = "Set max health", runOnServer = true)
    public String setMaxHealth(@CommandParam("max") int max, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            doHeal(clientComp.character, health.maxHealth, clientComp.character, health);
        }
        return "Max health set to " + max;
    }

    @Command(shortDescription = "Set regen rate", runOnServer = true)
    public String setRegenRate(@CommandParam("rate") float rate, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            health.regenRate = rate;
            clientComp.character.saveComponent(health);
        }
        return "Set regeneration rate to " + rate;
    }

    @Command(shortDescription = "Show your health")
    public String showHealth(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            return "Your health:" + health.currentHealth + " max:" + health.maxHealth + " regen:" + health.regenRate;
        }
        return "I guess you're dead?";
    }
}

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

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.audio.events.PlaySoundForOwnerEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterSoundComponent;
import org.terasology.logic.characters.CharacterSoundSystem;
import org.terasology.logic.characters.events.AttackEvent;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.util.Optional;

/**
 * This system reacts to OnDamageEvent events and lowers health on the HealthComponent.
 * This system also handls horizontal and vertical crashes of entities with HealthComponents.
 *
 * Logic flow for damage:
 * - OnDamageEvent
 * - BeforeDamageEvent
 * - (HealthComponent saved)
 * - OnDamagedEvent
 * - DestroyEvent (if no health)
 *
 * Logic flow for healing:
 * - DoHealEvent
 * - BeforeHealEvent
 * - (HealthComponent saved)
 * - OnHealedEvent
 * - FullHealthEvent (if at full health)
 *
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class HealthAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private EntityManager entityManager;

    @In
    private org.terasology.engine.Time time;

    private Random random = new FastRandom();

    @Override
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
            healAmount = regenerateHealth(health, healAmount);

            checkHealed(entity, health, healAmount);
        }
    }


    /**
     * Override the default behavior for an attack, causing it damage as opposed to just destroying it or doing nothing.
     */
    @ReceiveEvent(components = HealthComponent.class, netFilter = RegisterMode.AUTHORITY)
    public void onAttackEntity(AttackEvent event, EntityRef targetEntity) {
        damageEntity(event, targetEntity);
    }

    static void damageEntity(AttackEvent event, EntityRef targetEntity) {
        int damage = 1;
        Prefab damageType = EngineDamageTypes.PHYSICAL.get();
        // Calculate damage from item
        ItemComponent item = event.getDirectCause().getComponent(ItemComponent.class);
        if (item != null) {
            damage = item.baseDamage;
            if (item.damageType != null) {
                damageType = item.damageType;
            }
        }

        targetEntity.send(new DoDamageEvent(damage, damageType, event.getInstigator(), event.getDirectCause()));
        // consume the event so that the health system can take priority over default engine behavior
        event.consume();
    }

    private int regenerateHealth(HealthComponent health, int healAmount) {
        int newHeal = healAmount;
        while (time.getGameTimeInMs() >= health.nextRegenTick) {
            newHeal++;
            health.nextRegenTick = health.nextRegenTick + (long) (1000 / health.regenRate);
        }
        return newHeal;
    }

    private void checkHealed(EntityRef entity, HealthComponent health, int healAmount) {
        if (healAmount > 0) {
            checkHeal(entity, healAmount, entity);
            entity.saveComponent(health);
        }
    }

    private void checkHeal(EntityRef entity, int healAmount, EntityRef instigator) {
        BeforeHealEvent beforeHeal = entity.send(new BeforeHealEvent(healAmount, instigator));
        if (!beforeHeal.isConsumed()) {
            int modifiedAmount = calculateTotal(beforeHeal.getBaseHeal(), beforeHeal.getMultipliers(), beforeHeal.getModifiers());
            if (modifiedAmount > 0) {
                doHeal(entity, modifiedAmount, instigator);
            } else if (modifiedAmount < 0) {
                doDamage(entity, -modifiedAmount, EngineDamageTypes.HEALING.get(), instigator, EntityRef.NULL);
            }
        }
    }

    private void doHeal(EntityRef entity, int healAmount, EntityRef instigator) {
        HealthComponent health = entity.getComponent(HealthComponent.class);
        if (health != null) {
            int healedAmount = Math.min(health.currentHealth + healAmount, health.maxHealth) - health.currentHealth;
            health.currentHealth += healedAmount;
            entity.saveComponent(health);
            entity.send(new OnHealedEvent(healAmount, healedAmount, instigator));
            if (health.currentHealth == health.maxHealth) {
                entity.send(new FullHealthEvent(instigator));
            }
        }
    }

    private void doDamage(EntityRef entity, int damageAmount, Prefab damageType, EntityRef instigator, EntityRef directCause) {
        HealthComponent health = entity.getComponent(HealthComponent.class);
        if (health != null) {
            int damagedAmount = health.currentHealth - Math.max(health.currentHealth - damageAmount, 0);
            health.currentHealth -= damagedAmount;
            health.nextRegenTick = time.getGameTimeInMs() + TeraMath.floorToInt(health.waitBeforeRegen * 1000);
            entity.saveComponent(health);
            entity.send(new OnDamagedEvent(damageAmount, damagedAmount, damageType, instigator));
            if (health.currentHealth == 0 && health.destroyEntityOnNoHealth) {
                entity.send(new DestroyEvent(instigator, directCause, damageType));
            }
        }
    }

    @ReceiveEvent
    public void onDamage(DoDamageEvent event, EntityRef entity) {
        checkDamage(entity, event.getAmount(), event.getDamageType(), event.getInstigator(), event.getDirectCause());
    }

    private void checkDamage(EntityRef entity, int amount, Prefab damageType, EntityRef instigator, EntityRef directCause) {
        BeforeDamagedEvent beforeDamage = entity.send(new BeforeDamagedEvent(amount, damageType, instigator, directCause));
        if (!beforeDamage.isConsumed()) {
            int damageAmount = TeraMath.floorToInt(beforeDamage.getResultValue());
            if (damageAmount > 0) {
                doDamage(entity, damageAmount, damageType, instigator, directCause);
            } else {
                doHeal(entity, -damageAmount, instigator);
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
                checkDamage(entity, damage, EngineDamageTypes.PHYSICAL.get(), EntityRef.NULL, EntityRef.NULL);
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
                checkDamage(entity, damage, EngineDamageTypes.PHYSICAL.get(), EntityRef.NULL, EntityRef.NULL);
            }
        }
    }

    // Debug commands

    @Command(value = "kill", shortDescription = "Reduce the player's health to zero", runOnServer = true,
            requiredPermission = PermissionManager.NO_PERMISSION)
    public void killCommand(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            clientComp.character.send(new DestroyEvent(clientComp.character, EntityRef.NULL, EngineDamageTypes.DIRECT.get()));
        }
    }

    @Command(shortDescription = "Reduce the player's health by an amount", runOnServer = true)
    public String damage(@Sender EntityRef client, @CommandParam("amount") int amount) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new DoDamageEvent(amount, EngineDamageTypes.DIRECT.get(), clientComp.character));

        return "Inflicted damage of " + amount;
    }

    @Command(shortDescription = "Restores your health to max", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String healthMax(@Sender EntityRef clientEntity) {
        ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
        clientComp.character.send(new DoHealEvent(100000, clientComp.character));
        return "Health restored";
    }

    @Command(shortDescription = "Restores your health by an amount", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public void health(@Sender EntityRef client, @CommandParam("amount") int amount) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new DoHealEvent(amount, clientComp.character));
    }

    @Command(shortDescription = "Set max health", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setMaxHealth(@Sender EntityRef client, @CommandParam("max") int max) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            doHeal(clientComp.character, health.maxHealth, clientComp.character);
        }
        return "Max health set to " + max;
    }

    @Command(shortDescription = "Set health regen rate", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setRegenRate(@Sender EntityRef client, @CommandParam("rate") float rate) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            health.regenRate = rate;
            clientComp.character.saveComponent(health);
        }
        return "Set health regeneration rate to " + rate;
    }

    @Command(shortDescription = "Show your health")
    public String showHealth(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            return "Your health:" + health.currentHealth + " max:" + health.maxHealth + " regen:" + health.regenRate;
        }
        return "I guess you're dead?";
    }


    @Command(shortDescription = "Land without breaking a leg", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String softLanding(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            health.fallingDamageSpeedThreshold = 85f;
            health.excessSpeedDamageMultiplier = 2f;
            clientComp.character.saveComponent(health);

            return "Soft landing mode activated";
        }

        return "";
    }

    @Command(shortDescription = "Restore default collision damage values", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String restoreCollisionDamage(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);

        Optional<Prefab> prefab = Assets.get(new ResourceUrn("engine:player"), Prefab.class);
        HealthComponent healthDefault = prefab.get().getComponent(HealthComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null && healthDefault != null) {
            health.fallingDamageSpeedThreshold = healthDefault.fallingDamageSpeedThreshold;
            health.horizontalDamageSpeedThreshold = healthDefault.horizontalDamageSpeedThreshold;
            health.excessSpeedDamageMultiplier = healthDefault.excessSpeedDamageMultiplier;
            clientComp.character.saveComponent(health);
        }

        return "Normal collision damage values restored";
    }


    @ReceiveEvent
    public void onCrash(HorizontalCollisionEvent event, EntityRef entity, CharacterSoundComponent characterSounds, HealthComponent healthComponent) {
        Vector3f horizVelocity = new Vector3f(event.getVelocity());
        horizVelocity.y = 0;
        float velocity = horizVelocity.length();

        if (velocity > healthComponent.horizontalDamageSpeedThreshold) {
            if (characterSounds.lastSoundTime + CharacterSoundSystem.MIN_TIME < time.getGameTimeInMs()) {
                StaticSound sound = random.nextItem(characterSounds.landingSounds);
                if (sound != null) {
                    entity.send(new PlaySoundEvent(sound, characterSounds.landingVolume));
                    characterSounds.lastSoundTime = time.getGameTimeInMs();
                    entity.saveComponent(characterSounds);
                }
            }
        }
    }

    @ReceiveEvent
    public void onDamaged(OnDamagedEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        if (characterSounds.lastSoundTime + CharacterSoundSystem.MIN_TIME < time.getGameTimeInMs()) {

            // play the sound of damage hitting the character for everyone
            DamageSoundComponent damageSounds = event.getType().getComponent(DamageSoundComponent.class);
            if (damageSounds != null && !damageSounds.sounds.isEmpty()) {
                StaticSound sound = random.nextItem(damageSounds.sounds);
                if (sound != null) {
                    entity.send(new PlaySoundEvent(sound, 1f));
                }
            }

            // play the sound of a client's character being damaged to the client
            if (!characterSounds.damageSounds.isEmpty()) {
                StaticSound sound = random.nextItem(characterSounds.damageSounds);
                if (sound != null) {
                    entity.send(new PlaySoundForOwnerEvent(sound, characterSounds.damageVolume));
                }
            }

            characterSounds.lastSoundTime = time.getGameTimeInMs();
            entity.saveComponent(characterSounds);

        }
    }
}

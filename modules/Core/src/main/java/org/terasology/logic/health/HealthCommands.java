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

import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.Share;
import org.terasology.utilities.Assets;

import java.util.Optional;

@RegisterSystem
@Share(HealthCommands.class)
public class HealthCommands extends BaseComponentSystem {
    @Command(value = "kill", shortDescription = "Reduce the player's health to zero", runOnServer = true,
            requiredPermission = PermissionManager.NO_PERMISSION)
    public void killCommand(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            clientComp.character.send(new DestroyEvent(clientComp.character, EntityRef.NULL, EngineDamageTypes.DIRECT.get()));
        }
    }

    @Command(shortDescription = "Reduce the player's health by an amount", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
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
        return "Health fully restored";
    }

    @Command(shortDescription = "Restores your health by an amount", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String heal(@Sender EntityRef client, @CommandParam("amount") int amount) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        clientComp.character.send(new DoHealEvent(amount, clientComp.character));
        return "Health restored for " + amount;
    }

    @Command(shortDescription = "Set max health", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setMaxHealth(@Sender EntityRef client, @CommandParam("max") int max) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        float oldMaxHealth = health.maxHealth;
        if (health != null) {
            health.maxHealth = max;
            clientComp.character.saveComponent(health);
        }
        return "Max health changed from " + oldMaxHealth + " to " + max;
    }

    @Command(shortDescription = "Set health regen rate", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setRegenRate(@Sender EntityRef client, @CommandParam("rate") float rate) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        float oldRegenRate = health.regenRate;
        if (health != null) {
            health.regenRate = rate;
            clientComp.character.saveComponent(health);
        }
        return "Health regeneration changed from " + oldRegenRate + " to " + rate;
    }

    @Command(shortDescription = "Show your health", requiredPermission = PermissionManager.NO_PERMISSION)
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
}

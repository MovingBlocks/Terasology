// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.nakama;

import com.google.gson.JsonObject;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.inventory.events.GiveItemEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

/**
 * Entity system that bridges Gestalt chat events to the NakamaSubSystem
 * and provides console commands for cross-game item linking.
 */
@RegisterSystem
public class NakamaSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    private NakamaSubSystem nakamaSubSystem;

    public void setNakamaSubSystem(NakamaSubSystem subsystem) {
        this.nakamaSubSystem = subsystem;
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onChatMessage(ChatMessageEvent event, EntityRef entity) {
        if (nakamaSubSystem != null && nakamaSubSystem.isConnected()) {
            EntityRef from = event.getFrom();
            String playerName = "Unknown";
            DisplayNameComponent displayName = from.getComponent(DisplayNameComponent.class);
            if (displayName != null) {
                playerName = displayName.name;
            }
            nakamaSubSystem.sendChatMessage(playerName, event.getMessage());
        }
    }

    @Command(shortDescription = "Send an item link to the Bifrost channel",
             helpText = "Shares an item by name to connected games. Usage: link <itemName>")
    public String link(@Sender EntityRef sender, @CommandParam("itemName") String itemName) {
        if (nakamaSubSystem == null || !nakamaSubSystem.isConnected()) {
            return "Nakama not connected";
        }
        boolean sent = nakamaSubSystem.sendItemLink(itemName, "Item from Terasology");
        return sent ? "Beamed out: [" + itemName + "]" : "Failed to send item link";
    }

    @Command(shortDescription = "View the last received item link",
             helpText = "Shows details of the most recently beamed item from another game.")
    public String viewitem(@Sender EntityRef sender) {
        if (nakamaSubSystem == null) {
            return "Nakama not connected";
        }
        JsonObject link = nakamaSubSystem.getLastItemLink();
        if (link == null) {
            return "No item link received yet";
        }
        String name = link.has("name") ? link.get("name").getAsString() : "???";
        String desc = link.has("description") ? link.get("description").getAsString() : "";
        String game = link.has("game") ? link.get("game").getAsString() : "unknown";
        String player = link.has("player") ? link.get("player").getAsString() : "???";
        float price = link.has("price") ? link.get("price").getAsFloat() : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("=== Beamed Item ===\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("From: ").append(player).append(" (").append(game).append(")\n");
        if (!desc.isEmpty()) {
            sb.append("Desc: ").append(desc).append("\n");
        }
        if (price > 0) {
            sb.append("Value: ").append(String.format("%.0f", price)).append(" credits");
        }
        return sb.toString();
    }

    @Command(shortDescription = "Materialize the last beamed item as a token",
             helpText = "Creates a Bifrost Token item in your inventory from the last received item link.",
             runOnServer = true)
    public String materialize(@Sender EntityRef sender) {
        if (nakamaSubSystem == null) {
            return "Nakama not connected";
        }
        JsonObject link = nakamaSubSystem.consumeItemLink();
        if (link == null) {
            return "No item link to materialize";
        }
        String name = link.has("name") ? link.get("name").getAsString() : "Bifrost Token";
        String game = link.has("game") ? link.get("game").getAsString() : "unknown";
        String desc = link.has("description") ? link.get("description").getAsString() : "";

        // Create a proper inventory item entity
        EntityRef token = entityManager.create();

        DisplayNameComponent displayName = new DisplayNameComponent();
        displayName.name = "Bifrost: " + name;
        displayName.description = "Beamed from " + game + ". " + desc;
        token.addComponent(displayName);

        ItemComponent itemComponent = new ItemComponent();
        itemComponent.stackId = "bifrost:" + name.toLowerCase().replace(' ', '_');
        itemComponent.maxStackSize = 1;
        itemComponent.stackCount = 1;
        token.addComponent(itemComponent);

        // Give the item to the player's character
        EntityRef character = sender.getComponent(ClientComponent.class).character;
        GiveItemEvent giveEvent = new GiveItemEvent(character);
        token.send(giveEvent);

        if (giveEvent.isHandled()) {
            return "Materialized: [" + name + "] from " + game + " — check your inventory!";
        } else {
            token.destroy();
            return "Could not materialize [" + name + "] — inventory full?";
        }
    }
}

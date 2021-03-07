// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.subsystem.discordrpc;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.game.Game;
import org.terasology.logic.afk.AfkEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.event.LocalPlayerInitializedEvent;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;

import java.time.OffsetDateTime;

/**
 * It's a system that runs when a single player or multi player game has been started to process some stuff throw the
 * {@link DiscordRPCSubSystem}.
 *
 * @see DiscordRPCSubSystem
 */
@RegisterSystem(RegisterMode.CLIENT)
public final class DiscordRPCSystem extends BaseComponentSystem {
    private static final String UPDATE_PARTY_SIZE_ID = "discord-rpc:party-size";
    private static final long UPDATE_PARTY_SIZE_PERIOD = 25L * 1000L;

    @In
    private Game game;

    @In
    private LocalPlayer player;

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    private int onlinePlayers;

    @Override
    public void initialise() {
        onlinePlayers = 1;

        DiscordRPCSubSystem.discover();
    }

    @Override
    public void preBegin() {
        DiscordRPCSubSystem.setGameplayName("Custom");
        DiscordRPCSubSystem.setState(null);
        DiscordRPCSubSystem.setStartTimestamp(null);
    }

    @Override
    public void postBegin() {
        DiscordRPCSubSystem.setStartTimestamp(OffsetDateTime.now());
        setPartyState();
    }

    @Override
    public void shutdown() {
        if (delayManager.hasPeriodicAction(player.getClientEntity(), UPDATE_PARTY_SIZE_ID)) {
            delayManager.cancelPeriodicAction(player.getClientEntity(), UPDATE_PARTY_SIZE_ID);
        }

        DiscordRPCSubSystem.reset();
        DiscordRPCSubSystem.setState("In Main Menu");
        DiscordRPCSubSystem.setStartTimestamp(null);
    }

    @ReceiveEvent
    public void onPlayerInitialized(LocalPlayerInitializedEvent event, EntityRef player) {
        /* Adds the periodic action when the player is hosting or playing online to update party size */
        if (networkSystem.getMode() != NetworkMode.NONE) {
            delayManager.addPeriodicAction(player, UPDATE_PARTY_SIZE_ID, 0, UPDATE_PARTY_SIZE_PERIOD);
        }
    }

    @ReceiveEvent
    public void onAfk(AfkEvent event, EntityRef entityRef) {
        if (isServer() && player.getClientEntity().equals(entityRef)) {
            return;
        }

        if (event.isAfk()) {
            DiscordRPCSubSystem.setState("Idle");
            DiscordRPCSubSystem.resetPartyInfo();
        } else {
            setPartyState();
        }
    }

    @ReceiveEvent
    public void onPeriodicTrigger(PeriodicActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(UPDATE_PARTY_SIZE_ID)) {
            onlinePlayers = 0;
            entityManager.getEntitiesWith(ClientComponent.class).forEach(ignored -> onlinePlayers++);
            DiscordRPCSubSystem.setPartyInfo(onlinePlayers, 99);
        }
    }

    private void setPartyState() {
        final NetworkMode networkMode = networkSystem.getMode();

        String mode = "Playing Online";
        if (networkMode == NetworkMode.DEDICATED_SERVER) {
            mode = "Hosting";
        } else if (networkMode == NetworkMode.NONE) {
            mode = "Playing Solo";
            DiscordRPCSubSystem.setPartyInfo(1, 1);
        }

        DiscordRPCSubSystem.setState(mode);
        if (networkMode != NetworkMode.NONE) {

            /* The player is playing online or hosting a game */
            DiscordRPCSubSystem.setPartyInfo(onlinePlayers, 99);
        }
    }

    private boolean isServer() {
        NetworkMode networkMode = networkSystem.getMode();
        return networkMode != NetworkMode.CLIENT && networkMode != NetworkMode.DEDICATED_SERVER;
    }
}

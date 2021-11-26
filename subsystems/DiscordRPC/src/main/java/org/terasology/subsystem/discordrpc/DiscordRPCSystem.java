// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.subsystem.discordrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.game.Game;
import org.terasology.engine.logic.afk.AfkEvent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.event.LocalPlayerInitializedEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.time.OffsetDateTime;

/**
 * It's a system that runs when a single player or multi player game has been started to process some stuff throw the {@link
 * DiscordRPCSubSystem}.
 *
 * @see DiscordRPCSubSystem
 */
@RegisterSystem(RegisterMode.CLIENT)
public final class DiscordRPCSystem extends BaseComponentSystem {
    private static final String UPDATE_PARTY_SIZE_ID = "discord-rpc:party-size";
    private static final long UPDATE_PARTY_SIZE_PERIOD = 25L * 1000L;

    private static final Logger logger = LoggerFactory.getLogger(DiscordRPCSystem.class);

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
        if (player != null) {
            EntityRef client = player.getClientEntity();
            if (delayManager != null && delayManager.hasPeriodicAction(client, UPDATE_PARTY_SIZE_ID)) {
                delayManager.cancelPeriodicAction(client, UPDATE_PARTY_SIZE_ID);
            }
        }
        DiscordRPCSubSystem.reset();
        DiscordRPCSubSystem.setState("In Main Menu");
        DiscordRPCSubSystem.setStartTimestamp(null);
    }

    /**
     * Adds the periodic action when the player is hosting or playing online to update party size
     */
    @ReceiveEvent
    public void onPlayerInitialized(LocalPlayerInitializedEvent event, EntityRef localPlayer) {
        NetworkMode mode = networkSystem.getMode();
        if (mode != NetworkMode.NONE) {
            //FIXME: The 'delayManager' is only available on the authority system and is not initialized on clients.
            //       This will fail with a NPE when a clients tries to join a game. See #4742.
            if (mode.isAuthority()) {
                delayManager.addPeriodicAction(localPlayer, UPDATE_PARTY_SIZE_ID, 0, UPDATE_PARTY_SIZE_PERIOD);
            } else {
                logger.warn("The 'DelayManager' is not available on non-authority system. Not scheduling '{}' periodic action. " +
                        "See #4742.", UPDATE_PARTY_SIZE_ID);
            }
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

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.afk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.game.Game;
import org.terasology.engine.input.events.KeyDownEvent;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.physics.events.MovedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.input.Keyboard;

@RegisterSystem(RegisterMode.CLIENT)
public class AfkClientSystem extends BaseComponentSystem {

    /**
     * Duration in milliseconds (ms).
     */
    public static final long AFK_FREEDOM = 5 * 1000;

    private static final Logger logger = LoggerFactory.getLogger(AfkClientSystem.class);

    /**
     * Duration in milliseconds (ms).
     */
    private static final long AFK_TIMEOUT = 5 * 60 * 1000;

    @In
    private Console console;

    @In
    private LocalPlayer localPlayer;

    @In
    private NetworkSystem networkSystem;

    @In
    private NUIManager nuiManager;

    @In
    private Game game;

    @In
    private Time time;

    private long lastActive;

    @Override
    public void postBegin() {
        updateActive();
    }

    @Command(
        value = "afk",
        shortDescription = "Say that you are AFK to others",
        requiredPermission = PermissionManager.NO_PERMISSION
    )
    public void onCommand() {
        if (requireConnection()) {
            console.addMessage("Failed! You need to be connected to use this command.");
            return;
        }
        updateActive();
        EntityRef entity = localPlayer.getClientEntity();
        AfkComponent component = entity.getComponent(AfkComponent.class);
        component.afk = !component.afk;
        entity.addOrSaveComponent(component);
        if (component.afk) {
            console.addMessage("[AFK] You are AFK now!");
        } else {
            console.addMessage("[AFK] You are no longer AFK!");
        }
        AfkRequest request = new AfkRequest(entity, component.afk);
        entity.send(request);
    }

    @ReceiveEvent
    public void onDetectAfk(AfkDetectEvent event, EntityRef entityRef) {
        EntityRef entity = localPlayer.getClientEntity();
        long afkTime = time.getGameTimeInMs() - lastActive;
        if (afkTime >= AFK_TIMEOUT) {
            AfkComponent component = entity.getComponent(AfkComponent.class);
            if (!component.afk) {
                component.afk = true;
                AfkRequest request = new AfkRequest(entity, true);
                entity.send(request);
            }
        }
    }

    @ReceiveEvent
    public void onAfk(AfkEvent event, EntityRef entityRef) {
        if (requireConnection()) {
            return;
        }
        EntityRef entity = event.getTarget();
        AfkComponent component = entity.getComponent(AfkComponent.class);
        if (component != null) {
            component.afk = event.isAfk();
            entity.addOrSaveComponent(component);
        }
    }

    @ReceiveEvent
    public void onMove(MovedEvent movedEvent, EntityRef entity) {
        if (requireConnection()) {
            return;
        }
        if (!disable()) {
            updateActive();
        }
    }

    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent
    public void onKeyDown(KeyDownEvent event, EntityRef entity) {
        if (requireConnection()) {
            return;
        }
        AfkComponent component = entity.getComponent(AfkComponent.class);
        if (component != null && component.afk) {
            long afkTime = time.getGameTimeInMs() - lastActive;
            if (afkTime <= AFK_FREEDOM || event.getKey() == Keyboard.Key.ESCAPE) {
                return;
            }
            updateActive();
            disable();
        }
    }

    public void onAfkScreenClosed() {
        disable();
        updateActive();
    }

    public long getLastActive() {
        return lastActive;
    }


    private void updateActive() {
        lastActive = time.getGameTimeInMs();
    }

    private boolean disable() {
        EntityRef clientEntity = localPlayer.getClientEntity();
        AfkComponent component = clientEntity.getComponent(AfkComponent.class);
        if (component != null && component.afk) {
            component.afk = false;
            clientEntity.addOrSaveComponent(component);
            AfkRequest request = new AfkRequest(clientEntity, false);
            clientEntity.send(request);
            return true;
        }
        return false;
    }

    private boolean requireConnection() {
        NetworkMode networkMode = networkSystem.getMode();
        if (networkMode != NetworkMode.CLIENT && networkMode != NetworkMode.DEDICATED_SERVER) {
            return true;
        }
        return false;
    }
}

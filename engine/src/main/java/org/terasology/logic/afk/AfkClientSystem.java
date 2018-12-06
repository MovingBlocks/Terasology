/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.logic.afk;

import org.terasology.assets.ResourceUrn;
import org.terasology.engine.subsystem.rpc.DiscordRPCSubSystem;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.game.Game;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;

@RegisterSystem(RegisterMode.CLIENT)
public class AfkClientSystem extends BaseComponentSystem {

    private static final ResourceUrn SCREEN_URL = new ResourceUrn("engine:afk");
    private static final ResourceUrn CONSOLE_SCREEN_URL = new ResourceUrn("engine:console");

    private static final String PERIODIC_ID = "AUTO-AFK-TIMEOUT";
    private static final long PERIODIC_PERIOD = 1000L;

    @In
    private Console console;

    @In
    private LocalPlayer localPlayer;

    @In
    private NetworkSystem networkSystem;

    @In
    private NUIManager nuiManager;

    @In
    private DelayManager delayManager;

    @In
    private Game game;

    private int seconds;

    @Override
    public void postBegin() {
        if (requireConnection()) {
            return;
        }
        delayManager.addPeriodicAction(localPlayer.getClientEntity(), PERIODIC_ID, 0, PERIODIC_PERIOD);
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
        EntityRef entity = localPlayer.getClientEntity();
        AfkComponent component = entity.getComponent(AfkComponent.class);
        component.afk = !component.afk;
        if (component.afk) {
            if (delayManager.hasPeriodicAction(entity, PERIODIC_ID)) {
                delayManager.cancelPeriodicAction(entity, PERIODIC_ID);
                seconds = 0;
            }
            nuiManager.pushScreen(SCREEN_URL);
            nuiManager.closeScreen(CONSOLE_SCREEN_URL);
            enableDiscord();
            console.addMessage("[AFK] You are AFK now!");
        } else {
            delayManager.addPeriodicAction(entity, PERIODIC_ID, 0, PERIODIC_PERIOD);
            nuiManager.closeScreen(SCREEN_URL);
            disableDiscord();
            console.addMessage("[AFK] You are no longer AFK!");
        }
        entity.addOrSaveComponent(component);
        AfkRequest request = new AfkRequest(entity, component.afk);
        entity.send(request);
    }

    @ReceiveEvent
    public void onAfk(AfkEvent event, EntityRef entity) {
        if (requireConnection()) {
            return;
        }
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
        delayManager.addPeriodicAction(localPlayer.getClientEntity(), PERIODIC_ID, 0, PERIODIC_PERIOD);
        disable();
        seconds = 0;
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onKeyDown(KeyDownEvent event, EntityRef entity) {
        if (requireConnection()) {
            return;
        }
        if (disable()) {
            delayManager.addPeriodicAction(localPlayer.getClientEntity(), PERIODIC_ID, 0, PERIODIC_PERIOD);
            event.consume();
        } else {
            seconds = 0;
        }
    }

    @ReceiveEvent
    public void onPeriodic(PeriodicActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(PERIODIC_ID)) {
            seconds += 1;
            if (seconds >= 5 * 60) {
                AfkComponent component = entity.getComponent(AfkComponent.class);
                if (!component.afk) {
                    component.afk = true;
                    entity.addOrSaveComponent(component);
                    nuiManager.pushScreen(SCREEN_URL);
                    AfkRequest request = new AfkRequest(entity, true);
                    entity.send(request);
                    enableDiscord();
                }
                delayManager.cancelPeriodicAction(entity, PERIODIC_ID);
                seconds = 0;
            }
        }
    }

    private String getGame() {
        NetworkMode networkMode = networkSystem.getMode();
        String mode = "Playing Online";
        if (networkMode == NetworkMode.DEDICATED_SERVER) {
            mode = "Hosting | " + game.getName();
        }
        return mode;
    }

    private void enableDiscord() {
        DiscordRPCSubSystem.tryToDiscover();
        DiscordRPCSubSystem.setState("Idle", true);
    }

    private void disableDiscord() {
        DiscordRPCSubSystem.tryToDiscover();
        DiscordRPCSubSystem.setState(getGame(), true);
    }

    private boolean disable() {
        EntityRef clientEntity = localPlayer.getClientEntity();
        AfkComponent component = clientEntity.getComponent(AfkComponent.class);
        if (component != null && component.afk) {
            component.afk = false;
            nuiManager.closeScreen(SCREEN_URL);
            clientEntity.addOrSaveComponent(component);
            AfkRequest request = new AfkRequest(clientEntity, false);
            clientEntity.send(request);
            disableDiscord();
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

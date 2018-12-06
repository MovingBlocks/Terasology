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
import org.terasology.engine.Time;
import org.terasology.engine.subsystem.rpc.DiscordRPCSubSystem;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.game.Game;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.delay.PeriodicActionComponent;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;

import java.util.Set;

@RegisterSystem(RegisterMode.CLIENT)
public class AfkClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

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
    private Game game;

    @In
    private Time time;

    private int seconds;
    private boolean firstFrame;

    @Override
    public void update(float delta) {
        if (!firstFrame) {
            if (requireConnection()) {
                return;
            }
            addPeriodicAction();
            firstFrame = true;
        }
        final long currentWorldTime = time.getGameTimeInMs();
        if (!hasPeriodicAction()) {
            return;
        }
        PeriodicActionComponent component = getPeriodicActionComponent();
        final long processedTime = component.getLowestWakeUp();
        if (processedTime > currentWorldTime) {
            return;
        }
        EntityRef entity = localPlayer.getClientEntity();
        Set<String> actionIds = component.getTriggeredActionsAndReschedule(currentWorldTime);
        if (component.isEmpty()) {
            entity.removeComponent(PeriodicActionComponent.class);
        } else {
            entity.saveComponent(component);
        }
        for (String actionId : actionIds) {
            entity.send(new PeriodicActionTriggeredEvent(actionId));
        }
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
            cancelPeriodicAction();
            seconds = 0;
            nuiManager.pushScreen(SCREEN_URL, AfkScreen.class).setAfkClientSystem(this);
            nuiManager.closeScreen(CONSOLE_SCREEN_URL);
            enableDiscord();
            console.addMessage("[AFK] You are AFK now!");
        } else {
            addPeriodicAction();
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
        addPeriodicAction();
        disable();
        seconds = 0;
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onKeyDown(KeyDownEvent event, EntityRef entity) {
        if (requireConnection()) {
            return;
        }
        if (disable()) {
            addPeriodicAction();
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
                    nuiManager.pushScreen(SCREEN_URL, AfkScreen.class).setAfkClientSystem(this);
                    AfkRequest request = new AfkRequest(entity, true);
                    entity.send(request);
                    enableDiscord();
                }
                cancelPeriodicAction();
                seconds = 0;
            }
        }
    }

    public void onAfkScreenClosed() {
        disable();
        addPeriodicAction();
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

    private void addPeriodicAction() {
        if (hasPeriodicAction()) {
            return;
        }
        EntityRef entity = localPlayer.getClientEntity();
        PeriodicActionComponent component = getPeriodicActionComponent();
        component.addScheduledActionId(PERIODIC_ID, time.getGameTimeInMs(), PERIODIC_PERIOD);
        entity.addOrSaveComponent(component);
    }

    private void cancelPeriodicAction() {
        if (hasPeriodicAction()) {
            return;
        }
        EntityRef entity = localPlayer.getClientEntity();
        PeriodicActionComponent component = getPeriodicActionComponent();
        component.removeScheduledActionId(PERIODIC_ID);
        if (component.isEmpty()) {
            entity.removeComponent(PeriodicActionComponent.class);
        } else {
            entity.saveComponent(component);
        }
    }

    private boolean hasPeriodicAction() {
        PeriodicActionComponent component = getPeriodicActionComponent();
        boolean isNotEmpty = !component.isEmpty();
        boolean containsTheAction = component.containsActionId(PERIODIC_ID);
        if (isNotEmpty && containsTheAction) {
            return true;
        }
        return false;
    }

    private PeriodicActionComponent getPeriodicActionComponent() {
        PeriodicActionComponent component = localPlayer.getClientEntity().getComponent(PeriodicActionComponent.class);
        if (component == null) {
            component = new PeriodicActionComponent();
        }
        return component;
    }

    private boolean requireConnection() {
        NetworkMode networkMode = networkSystem.getMode();
        if (networkMode != NetworkMode.CLIENT && networkMode != NetworkMode.DEDICATED_SERVER) {
            return true;
        }
        return false;
    }

}

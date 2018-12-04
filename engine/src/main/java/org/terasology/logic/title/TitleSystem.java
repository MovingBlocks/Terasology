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
package org.terasology.logic.title;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;

@Share(Title.class)
@RegisterSystem(RegisterMode.ALWAYS)
public class TitleSystem extends BaseComponentSystem implements Title {

    private static final Logger logger = LoggerFactory.getLogger(TitleSystem.class);

    private static final ResourceUrn UI_URL = new ResourceUrn("engine:title");

    private static final String DELAY_ACTION_ID = "TITLE";

    @In
    private Console console;

    @In
    private NUIManager nuiManager;

    @In
    private DelayManager delayManager;

    @In
    private LocalPlayer localPlayer;

    private TitleScreen titleScreen;
    private String lastTitle = "";
    private String lastSubtitle = "";
    private long lastStay;

    @Override
    public void initialise() {
        logger.info("Initialised the title system!");
    }

    @Override
    public void shutdown() {
        logger.info("Successfully! shut down the title system!");
    }

    @Command(
            value = "title",
            shortDescription = "Use the title feature",
            helpText = "<title:subtitle:stay:reset> <value>"
    )
    public void titleCommand(@CommandParam("type") String type, @CommandParam(value = "value", required = false) String value) {
        if (type.equalsIgnoreCase("reset")) {
            if (titleScreen != null) {
                hide();
                console.addMessage("Done! Reset the title screen.");
                return;
            }
        }
        if (value != null) {
            if (type.equalsIgnoreCase("title")) {
                show(value, lastSubtitle, lastStay);
                lastTitle = value;
                console.addMessage("Done! The current title value is " + value);
            } else if (type.equalsIgnoreCase("subtitle")) {
                show(lastTitle, value, lastStay);
                lastSubtitle = value;
                console.addMessage("Done! The current subtitle value is " + value);
            } else if (type.equalsIgnoreCase("stay")) {
                try {
                    long stay = Long.parseLong(value);
                    lastStay = stay;
                    show(lastTitle, lastSubtitle, stay);
                    console.addMessage("Done! The current stay value is " + stay);
                } catch (NumberFormatException ex) {
                    console.addMessage("I can't understand the stay value", CoreMessageType.ERROR);
                }
            }
        } else {
            console.addMessage("Failed! Can't find the value to use it.");
        }
    }

    @ReceiveEvent(netFilter = RegisterMode.CLIENT)
    public void onDelayTriggered(DelayedActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(DELAY_ACTION_ID)) {
            hide();
        }
    }

    /**
     * Show a title screen to the player.
     *
     * @param title will be shown in bigger font
     * @param subtitle will be shown in small font
     * @param stay how much do you wanna from it to stay for the player
     */
    public void show(String title, String subtitle, long stay) {
        build();
        titleScreen.setTitle(title);
        titleScreen.setSubtitle(subtitle);
        titleScreen.update();
        clear();
        delayManager.addDelayedAction(localPlayer.getClientEntity(), DELAY_ACTION_ID, stay);
    }

    /**
     * Reset the title screen
     */
    public void hide() {
        clear();
        build();
        titleScreen.setTitle("");
        titleScreen.setSubtitle("");
        titleScreen.update();
        nuiManager.removeOverlay(UI_URL);
        titleScreen = null;
    }

    private void clear() {
        EntityRef entity = localPlayer.getClientEntity();
        if (delayManager.hasDelayedAction(entity, DELAY_ACTION_ID)) {
            delayManager.cancelDelayedAction(entity, DELAY_ACTION_ID);
        }
    }

    private void build() {
        if (titleScreen == null) {
            titleScreen = nuiManager.addOverlay(UI_URL, TitleScreen.class);
        }
    }

}

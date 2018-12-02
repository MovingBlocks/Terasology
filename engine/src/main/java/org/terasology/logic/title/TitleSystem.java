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
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;

@RegisterSystem(RegisterMode.ALWAYS)
public class TitleSystem extends BaseComponentSystem implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TitleSystem.class);

    private static final ResourceUrn UI_URL = new ResourceUrn("engine:title");

    @In
    private Console console;

    @In
    private NUIManager nuiManager;

    private TitleScreen titleScreen;
    private Thread thread;
    private float currentStay;
    private boolean alive;

    @Override
    public void initialise() {
        thread = new Thread(this);
        thread.setName("TITLE-STAY");
        thread.start();
        alive = true;
        logger.info("Initialised the title system!");
    }

    @Override
    public void shutdown() {
        alive = false;
        thread.interrupt();
        logger.info("Successfully! shut down the title system!");
    }

    @Override
    public void run() {
        while (alive) {
            if (currentStay <= 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) { } // Ignore because this may happen because of #shutdown() which it doesn't matter
                continue;
            }
            currentStay -= 1f;
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) { } // Ignore because this may happen because of #shutdown() which it doesn't matter
            if (currentStay <= 0 && (titleScreen != null)) {
                currentStay = 0;
                hide();
                continue;
            }
        }
        currentStay = 0; // Make sure to make it 0 even if the title got hidden
    }

    @Command(shortDescription = "To test the title")
    public void title() {
        if (titleScreen != null && !titleScreen.getTitle().equals("")) {
            hide();
        } else {
            show("Welcome to Terasology", "Title System by @iHDeveloper", 5 * 1000f);
        }
    }

    public void show(String title, String subtitle, float stay) {
        build();
        currentStay = stay;
        titleScreen.setTitle(title);
        titleScreen.setSubtitle(subtitle);
        titleScreen.update();
    }

    public void hide() {
        build();
        titleScreen.setTitle("");
        titleScreen.setSubtitle("");
        titleScreen.update();
        nuiManager.removeOverlay(UI_URL);
        titleScreen = null;
        currentStay = 0;
    }

    private void build() {
        if (titleScreen == null) {
            titleScreen = nuiManager.addOverlay(UI_URL, TitleScreen.class);
        }
    }

}

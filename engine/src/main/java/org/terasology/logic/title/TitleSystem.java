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

import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;

@RegisterSystem(RegisterMode.ALWAYS)
public class TitleSystem extends BaseComponentSystem {

    private static final ResourceUrn UI_URL = new ResourceUrn("engine:title");

    private TitleScreen titleScreen;

    @In
    private Console console;

    @In
    private NUIManager nuiManager;

    @Command(shortDescription = "To test the title")
    public void title() {
        if (titleScreen != null && !titleScreen.getTitle().equals("")) {
            hide();
        } else {
            show("Welcome to Terasology", "Title System by @iHDeveloper");
        }
    }

    public void show(String title, String subtitle) {
        build();
        titleScreen.setTitle(title);
        titleScreen.setSubtitle(subtitle);
        titleScreen.update();
    }

    public void hide() {
        build();
        titleScreen.setTitle("");
        titleScreen.setSubtitle("");
        titleScreen.update();
    }

    private void build() {
        if (titleScreen == null) {
            titleScreen = nuiManager.pushScreen(UI_URL, TitleScreen.class);
        }
    }

}

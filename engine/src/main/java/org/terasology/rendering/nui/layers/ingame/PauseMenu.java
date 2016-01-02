/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.ingame;

import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;

/**
 */
public class PauseMenu extends CoreScreenLayer {

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "close", widget -> getManager().closeScreen(PauseMenu.this));
        WidgetUtil.trySubscribe(this, "settings", widget -> getManager().pushScreen("settingsMenuScreen"));
        WidgetUtil.trySubscribe(this, "mainMenu", widget -> CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu()));
        WidgetUtil.trySubscribe(this, "exit", widget -> CoreRegistry.get(GameEngine.class).shutdown());
    }
}

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
package org.terasology.engine.subsystem.lwjgl;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.input.InputSystem;
import org.terasology.input.lwjgl.LwjglKeyboardDevice;
import org.terasology.input.lwjgl.LwjglMouseDevice;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.NUIManager;

public class LwjglInput extends BaseLwjglSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(LwjglInput.class);
    private boolean mouseGrabbed;

    @Override
    public void preInitialise() {
        super.preInitialise();
    }

    @Override
    public void postInitialise(Config config) {
        initControls();
        updateInputConfig(config);
        Mouse.setGrabbed(false);
    }

    @Override
    public void preUpdate(GameState currentState, float delta) {
        NUIManager nuiManager = CoreRegistry.get(NUIManager.class);
        GameEngine engine = CoreRegistry.get(GameEngine.class);

        // TODO: this originally occurred before GameThread.processWaitingProcesses();
        boolean newGrabbed = engine.hasMouseFocus() && !(nuiManager.isReleasingMouse());
        if (newGrabbed != mouseGrabbed) {
            Mouse.setGrabbed(newGrabbed);
            mouseGrabbed = newGrabbed;
        }
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        currentState.handleInput(delta);
    }

    @Override
    public void shutdown(Config config) {
    }

    @Override
    public void dispose() {
        Mouse.destroy();
        Keyboard.destroy();
    }

    private void initControls() {
        try {
            Keyboard.create();
            Keyboard.enableRepeatEvents(true);
            Mouse.create();
            InputSystem inputSystem = CoreRegistry.putPermanently(InputSystem.class, new InputSystem());
            inputSystem.setMouseDevice(new LwjglMouseDevice());
            inputSystem.setKeyboardDevice(new LwjglKeyboardDevice());
        } catch (LWJGLException e) {
            logger.error("Could not initialize controls.", e);
            System.exit(1);
        }
    }

    private void updateInputConfig(Config config) {
        config.getInput().getBinds().updateForChangedMods();
        config.save();
    }

    @Override
    public void registerSystems(ComponentSystemManager componentSystemManager) {
    }

}

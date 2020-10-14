// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.lwjgl;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.config.Config;
import org.terasology.config.ControllerConfig;
import org.terasology.context.Context;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.input.InputSystem;
import org.terasology.input.lwjgl.JInputControllerDevice;
import org.terasology.input.lwjgl.LwjglKeyboardDevice;
import org.terasology.input.lwjgl.LwjglMouseDevice;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

public class LwjglInput extends BaseLwjglSubsystem {

    @In
    private ContextAwareClassFactory classFactory;
    @In
    private Config config;

    private Context context;


    @Override
    public String getName() {
        return "Input";
    }

    @Override
    public void postInitialise(Context rootContext) {
        this.context = rootContext;
        initControls();
        updateInputConfig();
        Mouse.setGrabbed(false);
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        currentState.handleInput(delta);
    }

    @Override
    public void shutdown() {
        Mouse.destroy();
        Keyboard.destroy();
    }

    private void initControls() {
        try {
            Keyboard.create();
            Keyboard.enableRepeatEvents(true);
            Mouse.create();
            InputSystem inputSystem = classFactory.createInjectableInstance(InputSystem.class);
            inputSystem.setMouseDevice(new LwjglMouseDevice(context));
            inputSystem.setKeyboardDevice(new LwjglKeyboardDevice());

            ControllerConfig controllerConfig = config.getInput().getControllers();
            JInputControllerDevice controllerDevice = new JInputControllerDevice(controllerConfig);
            inputSystem.setControllerDevice(controllerDevice);
        } catch (LWJGLException e) {
            throw new RuntimeException("Could not initialize controls.", e);
        }
    }

    private void updateInputConfig() {
        BindsManager bindsManager = context.get(BindsManager.class);
        bindsManager.updateConfigWithDefaultBinds();
        bindsManager.saveBindsConfig();
    }
}

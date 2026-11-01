// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.lwjgl.glfw.GLFW;
import org.terasology.context.Lifetime;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.ControllerConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.config.BindsManager;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.lwjgl.LwjglControllerDevice;
import org.terasology.engine.input.lwjgl.LwjglKeyboardDevice;
import org.terasology.engine.input.lwjgl.LwjglMouseDevice;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;

public class LwjglInput extends BaseLwjglSubsystem {

    private Context context;

    @Inject
    public LwjglInput() {
    }

    @Override
    public String getName() {
        return "Input";
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
    }

    @Override
    public void initialise(GameEngine engine, ServiceRegistry serviceRegistry) {
        InputSystem inputSystem = new InputSystem();
        serviceRegistry.with(InputSystem.class).lifetime(Lifetime.Singleton).use(() -> inputSystem);
    }

    @Override
    public void postInitialise(Context rootContext) {
        this.context = rootContext;
        initControls();
        updateInputConfig();
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        currentState.handleInput(delta);
    }

    private void initControls() {
        Config config = context.get(Config.class);
        InputSystem inputSystem = context.get(InputSystem.class);

        inputSystem.setMouseDevice(new LwjglMouseDevice(config.getRendering()));
        inputSystem.setKeyboardDevice(new LwjglKeyboardDevice());

        ControllerConfig controllerConfig = config.getInput().getControllers();
        LwjglControllerDevice controllerDevice = new LwjglControllerDevice(controllerConfig);
        inputSystem.setControllerDevice(controllerDevice);

        long window = GLFW.glfwGetCurrentContext();
        ((LwjglKeyboardDevice) inputSystem.getKeyboard()).registerToLwjglWindow(window);
        ((LwjglMouseDevice) inputSystem.getMouseDevice()).registerToLwjglWindow(window);
    }

    private void updateInputConfig() {
        BindsManager bindsManager = context.get(BindsManager.class);
        bindsManager.updateConfigWithDefaultBinds();
        bindsManager.saveBindsConfig();
    }
}

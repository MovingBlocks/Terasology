// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.lwjgl;

import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.config.ControllerConfig;
import org.terasology.context.Context;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.input.InputSystem;
import org.terasology.input.lwjgl.LwjglControllerDevice;
import org.terasology.input.lwjgl.LwjglKeyboardDevice;
import org.terasology.input.lwjgl.LwjglMouseDevice;

public class LwjglInput extends BaseLwjglSubsystem {

    private Context context;

    @Override
    public String getName() {
        return "Input";
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
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

        InputSystem inputSystem = new InputSystem();
        context.put(InputSystem.class, inputSystem);
        inputSystem.setMouseDevice(new LwjglMouseDevice(context));
        inputSystem.setKeyboardDevice(new LwjglKeyboardDevice(context));

        ControllerConfig controllerConfig = context.get(Config.class).getInput().getControllers();
        LwjglControllerDevice controllerDevice = new LwjglControllerDevice(controllerConfig);
        inputSystem.setControllerDevice(controllerDevice);

    }

    private void updateInputConfig() {
        BindsManager bindsManager = context.get(BindsManager.class);
        bindsManager.updateConfigWithDefaultBinds();
        bindsManager.saveBindsConfig();
    }
}

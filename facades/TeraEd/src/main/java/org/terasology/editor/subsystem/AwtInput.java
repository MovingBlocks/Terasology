// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.editor.subsystem;

import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.ControllerConfig;
import org.terasology.engine.context.Context;
import org.terasology.editor.input.AwtKeyboardDevice;
import org.terasology.editor.input.AwtMouseDevice;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.config.BindsManager;
import org.terasology.engine.core.subsystem.lwjgl.BaseLwjglSubsystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.lwjgl.LwjglControllerDevice;

public class AwtInput extends BaseLwjglSubsystem {

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
        Config config = context.get(Config.class);

        InputSystem inputSystem = new InputSystem();
        context.put(InputSystem.class, inputSystem);
        inputSystem.setMouseDevice(new AwtMouseDevice(config.getRendering()));
        inputSystem.setKeyboardDevice(new AwtKeyboardDevice());

        ControllerConfig controllerConfig = config.getInput().getControllers();
        LwjglControllerDevice controllerDevice = new LwjglControllerDevice(controllerConfig);
        inputSystem.setControllerDevice(controllerDevice);
    }

    private void updateInputConfig() {
        BindsManager bindsManager = context.get(BindsManager.class);
        bindsManager.updateConfigWithDefaultBinds();
        bindsManager.saveBindsConfig();
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.openvr;

import org.lwjgl.glfw.GLFW;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.lwjgl.LwjglKeyboardDevice;
import org.terasology.engine.input.lwjgl.LwjglMouseDevice;
import org.terasology.engine.rendering.openvrprovider.OpenVRProvider;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;

public class OpenVRInput implements EngineSubsystem {

    Config config;

    private Context context;

    private OpenVRProvider vrProvider;

    private OpenVRControllers controllerDevice;

    /**
     * Get the name of this subsystem.
     * @return the name of the subsystem, a string.
     */
    @Override
    public String getName() {
        return "OpenVRInput";
    }

    /**
     * Actions that need to be performed before initialization. In this case, the VR provider is retrieved (and this
     * possibly triggers an initialization if it hasn't been retrieved before).
     * @param rootContext The root context, that will survive the entire run of the engine
     */
    @Override
    public void preInitialise(Context rootContext) {
        context = rootContext;
        vrProvider = OpenVRProvider.getInstance();
    }

    /**
     *
     * @param assetTypeManager The asset type manager to register asset types to
     */
    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
    }

    /**
     * Set up listeners and input devices.
     * @param rootContext
     */
    @Override
    public void postInitialise(Context rootContext) {
        config = context.get(Config.class);
        if (!config.getRendering().isVrSupport()) {
            return;
        }
        this.context = rootContext;
        InputSystem inputSystem = context.get(InputSystem.class);
        if (inputSystem == null) {
            inputSystem = new InputSystem();
            inputSystem.setMouseDevice(new LwjglMouseDevice(config.getRendering()));
            inputSystem.setKeyboardDevice(new LwjglKeyboardDevice());
            context.put(InputSystem.class, inputSystem);

            long window = GLFW.glfwGetCurrentContext();
            ((LwjglKeyboardDevice) inputSystem.getKeyboard()).registerToLwjglWindow(window);
            ((LwjglMouseDevice) inputSystem.getMouseDevice()).registerToLwjglWindow(window);
        }

        controllerDevice = new OpenVRControllers();
        vrProvider.getState().addControllerListener(controllerDevice);
        inputSystem.setControllerDevice(controllerDevice);
    }

    /**
     * Tasks to perform after an update.
     * @param currentState The current state
     * @param delta The total time this frame/update cycle
     */
    @Override
    public void postUpdate(GameState currentState, float delta) {
        currentState.handleInput(delta);
    }

    /**
     * Clean up all objects in this class.
     */
    @Override
    public void shutdown() {
        vrProvider.getState().removeControllerListener(controllerDevice);
    }
}

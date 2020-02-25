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
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.config.ControllerConfig;
import org.terasology.context.Context;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.input.InputSystem;
import org.terasology.input.lwjgl.JInputControllerDevice;
import org.terasology.input.lwjgl.LwjglKeyboardDevice;
import org.terasology.input.lwjgl.LwjglMouseDevice;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.layers.mainMenu.inputSettings.ControllerSettingsScreen;

import java.util.Set;

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
            InputSystem inputSystem = new InputSystem();
            context.put(InputSystem.class, inputSystem);
            inputSystem.setMouseDevice(new LwjglMouseDevice(context));
            inputSystem.setKeyboardDevice(new LwjglKeyboardDevice());

            ControllerConfig controllerConfig = context.get(Config.class).getInput().getControllers();
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

    /**
     * Refreshes controllers list to discover newly plugged-in controllers
     * and invalidates the current {@link ControllerSettingsScreen} instance
     * from current {@link AssetManager} if the screen was already loaded and
     * saved in the AssetManager.
     * <p>
     * Note that: if this {@link LwjglInput} object does not have a {@link Context},
     * it returns without doing anything. {@link Context} can be initialized by calling
     * {@code postInitialise(Context rootContext)} ({@see postInitialise}) before calling
     * this method {@code refreshControllerList}.
     */
    public void refreshControllerList(){
        if (context == null)
            return;

        // create a new controller device handler.
        ControllerConfig controllerConfig = context.get(Config.class).getInput().getControllers();
        JInputControllerDevice controllerDevice = new JInputControllerDevice(controllerConfig);
        context.get(InputSystem.class).setControllerDevice(controllerDevice);

        // invalidate controller page from cache
        context.get(AssetManager.class).getLoadedAssets(UIElement.class).stream().
            filter(k -> k.getUrn().equals(ControllerSettingsScreen.ASSET_URI)).
            forEach(UIElement::dispose);
    }
}

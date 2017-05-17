
package org.terasology.engine.subsystem.config;

import org.terasology.config.BindsConfig;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.BindableButton;
import org.terasology.input.ControllerInput;
import org.terasology.input.Input;
import org.terasology.input.InputSystem;
import org.terasology.input.MouseInput;
import org.terasology.input.internal.AbstractBindableAxis;
import org.terasology.input.internal.BindableRealAxis;

import java.util.List;
import java.util.Map;

public interface BindsManager {

    BindsConfig getBindsConfig();

    /**
     * The default bindings. This reflects the current status from {@link #updateForAllModules(Context)}.
     * @return Returns the default bindings. Changes to this config are not reflected to the defaults.
     */
    BindsConfig getDefault();

    /**
     * Updates the bindings with their defaults from the entire environment.
     * @param context
     */
    void updateForAllModules(Context context);

    void applyBinds(InputSystem inputSystem, ModuleManager moduleManager);

    void saveBindsConfig();

    List<BindableButton> getButtonBinds();

    Map<MouseInput, BindableButton> getMouseButtonBinds();

    BindableButton getMouseWheelUpBind();

    BindableButton getMouseWheelDownBind();

    Map<ControllerInput, BindableButton> getControllerBinds();

    Map<Input, BindableRealAxis> getControllerAxisBinds();

    Map<Integer, BindableButton> getKeyBinds();

    List<AbstractBindableAxis> getAxisBinds();

    void linkBindButtonToKey(int key, SimpleUri bindId);
}

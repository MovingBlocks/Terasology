// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.subsystem.config;

import org.terasology.engine.config.BindsConfig;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.input.BindAxisEvent;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.BindableButton;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindAxis;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.engine.input.RegisterRealBindAxis;
import org.terasology.engine.input.internal.AbstractBindableAxis;
import org.terasology.engine.input.internal.BindableRealAxis;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.input.ControllerInput;
import org.terasology.input.Input;
import org.terasology.input.Keyboard.KeyId;
import org.terasology.input.MouseInput;

import java.util.List;
import java.util.Map;

/**
 * Manages all input bindings from the {@link ModuleEnvironment}.
 * The manager handles:
 * <ul>
 * <li>Subclasses of {@link BindButtonEvent}, annotated with {@link RegisterBindButton}.</li>
 * <li>Subclasses of {@link BindAxisEvent}, annotated with {@link RegisterBindAxis} or {@link RegisterRealBindAxis}.</li>
 * </ul>
 */
public interface BindsManager {

    /**
     * The actual binds config. This reflects the current status from {@link BindsManager#updateConfigWithDefaultBinds()}.
     * and all further modifications like modifications in the user input settings.
     * @return
     */
    BindsConfig getBindsConfig();

    /**
     * The default bindings. This reflects the current status from {@link BindsManager#updateConfigWithDefaultBinds()}.
     * @return Returns the default bindings. Changes to this config modify the actual instance
     * but become invalid the next time {@link #updateConfigWithDefaultBinds()} is called.
     */
    BindsConfig getDefaultBindsConfig();

    /**
     * Updates the bindings with their defaults from the current {@link ModuleEnvironment}.
     * After this call {@link #getDefaultBindsConfig()} contains all values for events with
     * {@link RegisterBindButton}, {@link RegisterBindAxis} and {@link RegisterRealBindAxis} which contain one ore more {@link DefaultBinding}.
     *
     * {@link #getBindsConfig()} will only be enhanced with missing values.
     *
     */
    void updateConfigWithDefaultBinds();

    /**
     * Registers all binds from the current {@link ModuleEnvironment}.
     * This contains all events with {@link RegisterBindButton}, {@link RegisterBindAxis} and {@link RegisterRealBindAxis}.
     * {@link #updateConfigWithDefaultBinds()} has to be called before to initialize the config.
     */
    void registerBinds();

    /**
     * Persists the current binds config.
     */
    void saveBindsConfig();

    /**
     * All button binds, registered with {@link #registerBinds()}.
     */
    List<BindableButton> getButtonBinds();

    /**
     * All binds for {@link MouseInput}, registered with {@link #registerBinds()}.
     */
    Map<MouseInput, BindableButton> getMouseButtonBinds();

    /**
     * The binding for the mouse wheel up movement, registered with {@link #registerBinds()}. This may be null.
     */
    BindableButton getMouseWheelUpBind();

    /**
     * The binding for the mouse wheel down movement, registered with {@link #registerBinds()}. This may be null.
     */
    BindableButton getMouseWheelDownBind();

    /**
     * All binds for {@link ControllerInput}, registered with {@link #registerBinds()}.
     */
    Map<ControllerInput, BindableButton> getControllerBinds();

    /**
     * All binds for controller axis input, registered with {@link #registerBinds()}.
     */
    Map<Input, BindableRealAxis> getControllerAxisBinds();

    /**
     * The key bindings. Values map to the {@link KeyId} constants.
     * Changes to the collection or the {@link BindableButton}s modify the system.
     */
    Map<Integer, BindableButton> getKeyBinds();

    /**
     * The axis bindings.
     * Changes to the collection or the {@link AbstractBindableAxis}s modify the system.
     */
    List<AbstractBindableAxis> getAxisBinds();

    /**
     * Binds a button directly to an input.
     * @param key one constant from the {@link KeyId}s.
     * @param bindId the uri for the binding, e.g. <code>engine:forwards</code>.
     */
    void linkBindButtonToKey(int key, SimpleUri bindId);
}

/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.engine.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.BindAxisEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.BindableAxis;
import org.terasology.input.BindableButton;
import org.terasology.input.Input;
import org.terasology.input.InputSystem;
import org.terasology.input.RegisterBindAxis;
import org.terasology.input.RegisterBindButton;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.players.LocalPlayerSystem;

import java.util.Locale;

/**
 * @author Immortius
 */
public class RegisterInputSystem extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(RegisterInputSystem.class);

    @Override
    public String getMessage() {
        return "Setting up Input Systems...";
    }

    @Override
    public boolean step() {
        ComponentSystemManager componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);

        LocalPlayerSystem localPlayerSystem = new LocalPlayerSystem();
        componentSystemManager.register(localPlayerSystem, "engine:localPlayerSystem");
        CoreRegistry.put(LocalPlayerSystem.class, localPlayerSystem);

        CameraTargetSystem cameraTargetSystem = new CameraTargetSystem();
        CoreRegistry.put(CameraTargetSystem.class, cameraTargetSystem);
        componentSystemManager.register(cameraTargetSystem, "engine:CameraTargetSystem");

        BindsConfig bindsConfig = CoreRegistry.get(Config.class).getInput().getBinds();
        InputSystem inputSystem = CoreRegistry.get(InputSystem.class);
        componentSystemManager.register(inputSystem, "engine:InputSystem");

        inputSystem.clearBinds();
        for (Module module : moduleManager.getActiveModules()) {
            if (module.isCodeModule()) {
                registerButtonBinds(inputSystem, module.getId(), module.getReflections().getTypesAnnotatedWith(RegisterBindButton.class), bindsConfig);
                registerAxisBinds(inputSystem, module.getId(), module.getReflections().getTypesAnnotatedWith(RegisterBindAxis.class));
            }
        }

        return true;
    }

    private void registerAxisBinds(InputSystem inputSystem, String packageName, Iterable<Class<?>> classes) {
        String prefix = packageName.toLowerCase(Locale.ENGLISH) + ":";
        for (Class registerBindClass : classes) {
            RegisterBindAxis info = (RegisterBindAxis) registerBindClass.getAnnotation(RegisterBindAxis.class);
            String id = prefix + info.id();
            if (BindAxisEvent.class.isAssignableFrom(registerBindClass)) {
                BindableButton positiveButton = inputSystem.getBindButton(new SimpleUri(info.positiveButton()));
                BindableButton negativeButton = inputSystem.getBindButton(new SimpleUri(info.negativeButton()));
                if (positiveButton == null) {
                    logger.warn("Failed to register axis \"{}\", missing positive button \"{}\"", id, info.positiveButton());
                    continue;
                }
                if (negativeButton == null) {
                    logger.warn("Failed to register axis \"{}\", missing negative button \"{}\"", id, info.negativeButton());
                    continue;
                }
                try {
                    BindableAxis bindAxis = inputSystem.registerBindAxis(id, (BindAxisEvent) registerBindClass.newInstance(), positiveButton, negativeButton);
                    bindAxis.setSendEventMode(info.eventMode());
                    logger.debug("Registered axis bind: {}", id);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register axis bind \"{}\"", id, e);
                }
            } else {
                logger.error("Failed to register axis bind \"{}\", does not extend BindAxisEvent", id);
            }
        }
    }

    private void registerButtonBinds(InputSystem inputSystem, String moduleId, Iterable<Class<?>> classes, BindsConfig config) {
        for (Class registerBindClass : classes) {
            RegisterBindButton info = (RegisterBindButton) registerBindClass.getAnnotation(RegisterBindButton.class);
            SimpleUri bindUri = new SimpleUri(moduleId, info.id());
            if (BindButtonEvent.class.isAssignableFrom(registerBindClass)) {
                try {
                    BindableButton bindButton = inputSystem.registerBindButton(bindUri, info.description(), (BindButtonEvent) registerBindClass.newInstance());
                    bindButton.setMode(info.mode());
                    bindButton.setRepeating(info.repeating());

                    for (Input input : config.getBinds(bindUri)) {
                        inputSystem.linkBindButtonToInput(input, bindUri);
                    }

                    logger.debug("Registered button bind: {}", bindUri);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register button bind \"{}\"", e);
                }
            } else {
                logger.error("Failed to register button bind \"{}\", does not extend BindButtonEvent", bindUri);
            }
        }
    }
}

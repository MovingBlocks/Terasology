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
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.GameManifest;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.naming.NameVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class RegisterMods extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(RegisterMods.class);

    private final Context context;
    private final GameManifest gameManifest;
    private Thread applyModulesThread;
    private ModuleEnvironment oldEnvironment;

    public RegisterMods(Context context, GameManifest gameManifest) {
        this.context = context;
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        if (applyModulesThread != null) {
            return "Scanning for Assets...";
        } else {
            return "Registering Mods...";
        }
    }

    @Override
    public boolean step() {
        if (applyModulesThread != null) {
            if (!applyModulesThread.isAlive()) {
                if (oldEnvironment != null) {
                    oldEnvironment.close();
                }
                return true;
            }
            return false;
        } else {
            ModuleManager moduleManager = context.get(ModuleManager.class);
            List<Name> moduleIds = gameManifest.getModules().stream().map(NameVersion::getName)
                    .collect(Collectors.toCollection(ArrayList::new));

            DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
            ResolutionResult result = resolver.resolve(moduleIds);
            if (result.isSuccess()) {
                oldEnvironment = moduleManager.getEnvironment();
                ModuleEnvironment env = moduleManager.loadEnvironment(result.getModules(), true);

                for (Module moduleInfo : env.getModulesOrderedByDependencies()) {
                    logger.info("Activating module: {}:{}", moduleInfo.getId(), moduleInfo.getVersion());
                }

                EnvironmentSwitchHandler environmentSwitchHandler = context.get(EnvironmentSwitchHandler.class);
                applyModulesThread = new Thread(() -> environmentSwitchHandler.handleSwitchToGameEnvironment(context));
                applyModulesThread.start();
                return false;
            } else {
                logger.warn("Missing at least one required module or dependency: {}", moduleIds);
                context.get(GameEngine.class).changeState(new StateMainMenu("Missing required module or dependency"));
                return true;
            }
        }

    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}

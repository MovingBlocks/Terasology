// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.GameManifest;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.naming.NameVersion;
import org.terasology.registry.In;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ExpectedCost(1)
public class RegisterMods extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(RegisterMods.class);

    @In
    private GameManifest gameManifest;
    @In
    private ModuleManager moduleManager;
    @In
    private EnvironmentSwitchHandler environmentSwitchHandler;
    @In
    private GameEngine gameEngine;
    @In
    private Context context;

    private Thread applyModulesThread;
    private ModuleEnvironment oldEnvironment;


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

                applyModulesThread = new Thread(() -> environmentSwitchHandler.handleSwitchToGameEnvironment(context));
                applyModulesThread.start();
                return false;
            } else {
                logger.warn("Missing at least one required module (or dependency) from the following list: {}",
                        moduleIds);
                gameEngine.changeState(new StateMainMenu("Missing required module or dependency"));
                return true;
            }
        }
    }
}

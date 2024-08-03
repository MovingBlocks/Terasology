// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.game.GameManifest;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.module.dependencyresolution.ResolutionResult;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.NameVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                    logger.atInfo().log("Activating module: {}:{}", moduleInfo.getId(), moduleInfo.getVersion());
                }

                EnvironmentSwitchHandler environmentSwitchHandler = context.get(EnvironmentSwitchHandler.class);
                applyModulesThread = new Thread(() -> environmentSwitchHandler.handleSwitchToGameEnvironment(context));
                applyModulesThread.start();
                return false;
            } else {
                logger.warn("Missing at least one required module (or dependency) from the following list: {}", moduleIds);
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

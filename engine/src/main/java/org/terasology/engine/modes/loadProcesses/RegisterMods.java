// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.bootstrap.EnvironmentSwitchHandler;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 */
public class RegisterMods extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(RegisterMods.class);

    private final Context context;
    private final GameManifest gameManifest;
    private Future<?> applyModulesThread;
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
            if (applyModulesThread.isDone()) {
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

                applyModulesThread = Executors.newSingleThreadExecutor(
                        new ThreadFactoryBuilder()
                                .setNameFormat(new TargetLengthBasedClassNameAbbreviator(36).abbreviate(getClass().getName()) + "-%d")
                                .setDaemon(true)
                                .build()).submit(() -> environmentSwitchHandler.handleSwitchToGameEnvironment(context));
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

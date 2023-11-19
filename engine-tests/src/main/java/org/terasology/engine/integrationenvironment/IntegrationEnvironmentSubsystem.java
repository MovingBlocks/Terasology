// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.testUtil.WithUnittestModule;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter;
import org.terasology.gestalt.module.ModuleRegistry;

import java.io.IOException;
import java.nio.file.Path;

final class IntegrationEnvironmentSubsystem implements EngineSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationEnvironmentSubsystem.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        ModuleManager moduleManager = rootContext.getValue(ModuleManager.class);
        WithUnittestModule.registerUnittestModule(moduleManager);
        registerCurrentDirectoryIfModule(moduleManager);
        configure(rootContext);
    }

    /**
     * Apply test environment default configuration.
     * <p>
     * You can override this by defining your own EngineSubsystem and passing it to
     * {@link IntegrationEnvironment#subsystem()}; it will run after this does.
     */
    static void configure(Context context) {
        Config config = context.getValue(Config.class);
        config.getRendering().setViewDistance(ViewDistance.LEGALLY_BLIND);

        SystemConfig sys = context.getValue(SystemConfig.class);
        sys.writeSaveGamesEnabled.set(false);
    }

    /**
     * In standalone module environments (i.e. Jenkins CI builds) the CWD is the module under test. When it uses MTE it very likely needs to
     * load itself as a module, but it won't be loadable from the typical path such as ./modules. This means that modules using MTE would
     * always fail CI tests due to failing to load themselves.
     * <p>
     * For these cases we try to load the CWD (via the installPath) as a module and put it in the global module registry.
     * <p>
     * This process is based on how ModuleManagerImpl uses ModulePathScanner to scan for available modules.
     */
    static void registerCurrentDirectoryIfModule(ModuleManager moduleManager) {
        Path installPath = PathManager.getInstance().getInstallPath();
        ModuleRegistry registry = moduleManager.getRegistry();
        ModuleMetadataJsonAdapter metadataReader = moduleManager.getModuleMetadataReader();
        moduleManager.getModuleFactory().getModuleMetadataLoaderMap()
                .put(TerasologyConstants.MODULE_INFO_FILENAME.toString(), metadataReader);

        try {
            Module module = moduleManager.getModuleFactory().createModule(installPath.toFile());
            if (module != null) {
                registry.add(module);
                logger.info("Added install path as module: {}", installPath);
            } else {
                logger.info("Install path does not appear to be a module: {}", installPath);
            }
        } catch (IOException e) {
            logger.warn("Could not read install path as module at {}", installPath);
        }
    }
}

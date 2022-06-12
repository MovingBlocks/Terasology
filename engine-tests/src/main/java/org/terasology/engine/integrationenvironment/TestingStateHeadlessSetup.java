// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.ModuleConfig;
import org.terasology.engine.config.WorldGenerationConfig;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.headless.mode.StateHeadlessSetup;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.gestalt.naming.Name;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public class TestingStateHeadlessSetup extends StateHeadlessSetup {

    static final Name MTE_MODULE_NAME = new Name("unittest");
    static final String WORLD_TITLE = "testworld";
    static final String DEFAULT_SEED = "seed";

    private static final Logger logger = LoggerFactory.getLogger(TestingStateHeadlessSetup.class);

    private final Collection<String> dependencies;
    private final SimpleUri worldGeneratorUri;

    {
        strictModuleRequirements = true;
    }

    public TestingStateHeadlessSetup(Collection<String> dependencies, String worldGeneratorUri, NetworkMode networkMode) {
        super(networkMode);
        this.dependencies = dependencies;
        this.worldGeneratorUri = new SimpleUri(worldGeneratorUri);
        checkArgument(this.worldGeneratorUri.isValid(), "Not a valid URI `%s`", worldGeneratorUri);
    }

    void configForTest(Config config, ModuleManager moduleManager) {
        Set<Name> dependencyNames = dependencies.stream().map(Name::new).collect(Collectors.toSet());

        if (dependencyNames.isEmpty()) {
            Path workingDirectory = Path.of(".").toAbsolutePath().normalize();
            var defaultModule = moduleManager.getModuleAt(workingDirectory);
            logger.info(
                    "No module dependencies given. Checked current directory `{}`, found `{}` to use as default.",
                    workingDirectory, defaultModule.orElse(null));
            defaultModule.ifPresent(m -> dependencyNames.add(m.getId()));
        }

        // Include the MTE module to provide world generators and suchlike.
        dependencyNames.add(MTE_MODULE_NAME);

        ModuleConfig moduleSelection = config.getDefaultModSelection();
        moduleSelection.clear();
        dependencyNames.forEach(moduleSelection::addModule);

        WorldGenerationConfig worldGenerationConfig = config.getWorldGeneration();
        worldGenerationConfig.setDefaultGenerator(worldGeneratorUri);
        worldGenerationConfig.setWorldTitle(WORLD_TITLE);
        worldGenerationConfig.setDefaultSeed(DEFAULT_SEED);
    }

    @Override
    public GameManifest createGameManifest() {
        GameManifest gameManifest = super.createGameManifest();

        float timeOffset = 0.25f + 0.025f;  // Time at dawn + little offset to spawn in a brighter env.
        gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD).setTime((long) (WorldTime.DAY_LENGTH * timeOffset));
        return gameManifest;
    }

    @Override
    public void init(GameEngine engine) {
        // We want to modify Config before super.init calls createGameManifest, but the child context
        // does not exist before we call super.init.
        var tEngine = (TerasologyEngine) engine;
        configForTest(tEngine.getFromEngineContext(Config.class), tEngine.getFromEngineContext(ModuleManager.class));
        super.init(engine);
    }
}

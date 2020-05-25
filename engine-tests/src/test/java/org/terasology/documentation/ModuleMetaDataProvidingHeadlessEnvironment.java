/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.documentation;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.terasology.HeadlessEnvironment;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.reflection.TypeRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Set;

/**
 * Environment with a ModuleManager, RemoteModuleRegistry, TypeRegistry, ComponentSystemManager and AssetManager.
 * Used to generate a partial headless ( = no graphics ) environment to access game module data including Events
 * and assets.
 */
public class ModuleMetaDataProvidingHeadlessEnvironment  extends HeadlessEnvironment {
    private static ModuleManager moduleManager;

    /**
     * Default master server address given game config default.
     */
    private static String masterServerAddress = "meta.terasology.org";

    /**
     * Environment with a ModuleManager, RemoteModuleRegistry, TypeRegistry, ComponentSystemManager and AssetManager.
     * Used to generate a partial headless environment to access game module data including Events
     * and assets. Uses default remote master server "meta.terasology.org".
     *
     * @param modules a set of module names that should be loaded (latest version)
     * @return a ModuleManager containing an InstallationManager configured to access a remote module registry at URL masterServerAddress
     * @throws Exception if a manager or registry cannot be loaded because engine metadata cannot be found or
     * ModuleInstallManager is unable to resolve the masterRemoteServerAddress endpoint
     */
    public ModuleMetaDataProvidingHeadlessEnvironment(Name... modules) throws IOException {
        super(modules);
        initialize();
    }

    private void initialize() throws IOException {
        final JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
        final FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
        PathManager.getInstance().useOverrideHomePath(vfs.getPath(""));

        moduleManager = context.get(ModuleManager.class);
        context.put(ModuleManager.class, moduleManager);

        ComponentSystemManager componentSystemManager = new ComponentSystemManager(context);
        context.put(ComponentSystemManager.class, componentSystemManager);
        LoadPrefabs prefabLoadStep = new LoadPrefabs(context);

        boolean complete = false;
        prefabLoadStep.begin();
        while (!complete) {
            complete = prefabLoadStep.step();
        }
        context.get(ComponentSystemManager.class).initialise();
        context.put(Console.class, new ConsoleImpl(context));

        this.setupAssetManager();
    }

    @Override
    protected void setupModuleManager(Set<Name> moduleNames) throws Exception {
        TypeRegistry typeRegistry = new TypeRegistry();
        context.put(TypeRegistry.class, typeRegistry);

        ModuleManager moduleManager = ModuleManagerFactory.create(masterServerAddress);
        ModuleRegistry registry = moduleManager.getRegistry();

        DependencyResolver resolver = new DependencyResolver(registry);
        ResolutionResult result = resolver.resolve(moduleNames);

        if (result.isSuccess()) {
            ModuleEnvironment modEnv = moduleManager.loadEnvironment(result.getModules(), true);
            typeRegistry.reload(modEnv);
            System.out.println("Loaded modules: " + modEnv.getModuleIdsOrderedByDependencies());
        } else {
            System.out.println("Could not resolve module dependencies for " + moduleNames);
        }

        context.put(ModuleManager.class, moduleManager);

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
    }
}
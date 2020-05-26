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

import org.terasology.HeadlessEnvironment;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.reflection.TypeRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Environment with a ModuleManager, RemoteModuleRegistry, TypeRegistry, ComponentSystemManager and AssetManager.
 * Used to generate a partial headless ( = no graphics ) environment to access game module data including Events
 * and assets.
 */
public class ModuleMetaDataProvidingHeadlessEnvironment  extends HeadlessEnvironment {
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
     * @throws ExecutionException if a manager or registry cannot be loaded because engine metadata cannot be found or
     * ModuleInstallManager is unable to resolve the masterRemoteServerAddress endpoint
     * @throws InterruptedException if a the RemoteRegistry connection is interrupted
     */
    public ModuleMetaDataProvidingHeadlessEnvironment(Name... modules) throws ExecutionException, InterruptedException {
        super(modules);
        initialize();
    }

    private void initialize() throws ExecutionException, InterruptedException {
        // Request Remote Modules List...
        ModuleManager moduleManager = context.get(ModuleManager.class);
        Future<Void> remoteModuleRegistryUpdater = Executors.newSingleThreadExecutor()
                .submit(moduleManager.getInstallManager().updateRemoteRegistry());
        remoteModuleRegistryUpdater.get(); // wait until remoteRegistry downloads
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
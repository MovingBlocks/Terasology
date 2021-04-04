// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter;
import org.terasology.gestalt.module.ModuleMetadataLoader;
import org.terasology.gestalt.module.ModulePathScanner;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.TableModuleRegistry;
import org.terasology.gestalt.module.dependencyresolution.DependencyInfo;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.module.dependencyresolution.ResolutionResult;
import org.terasology.gestalt.module.sandbox.APIScanner;
import org.terasology.gestalt.module.sandbox.ModuleSecurityManager;
import org.terasology.gestalt.module.sandbox.ModuleSecurityPolicy;
import org.terasology.gestalt.module.sandbox.PermissionProviderFactory;
import org.terasology.gestalt.module.sandbox.StandardPermissionProviderFactory;
import org.terasology.gestalt.module.sandbox.WarnOnlyProviderFactory;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;

import java.io.File;
import java.lang.reflect.ReflectPermission;
import java.nio.file.Path;
import java.security.Policy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleManager {
    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);

    private final StandardPermissionProviderFactory permissionProviderFactory = new StandardPermissionProviderFactory();
    private final PermissionProviderFactory wrappingPermissionProviderFactory =
            new WarnOnlyProviderFactory(permissionProviderFactory);

    private final ModuleRegistry registry;
    private ModuleEnvironment environment;
    private final ModuleMetadataJsonAdapter metadataReader;
    private final ModuleFactory moduleFactory;
    private final ModuleInstallManager installManager;
    private final Module engineModule;

    public ModuleManager(String masterServerAddress) {
        this(masterServerAddress, Collections.emptyList());
    }

    public ModuleManager(String masterServerAddress, List<Class<?>> classesOnClasspathsToAddToEngine) {
        PathManager pathManager = PathManager.getInstance();  // get early so if it needs to initialize, it does it now

        metadataReader = newMetadataReader();

        moduleFactory = new ModuleFactory();
        Map<String, ModuleMetadataLoader> mmlm = moduleFactory.getModuleMetadataLoaderMap();
        mmlm.put(TerasologyConstants.MODULE_INFO_FILENAME.toString(), metadataReader);
        mmlm.put("engine-module.txt", metadataReader);  // FIXME: this should be *only* for engine-module.
        // FIXME: …or maybe engine-module doesn't need a special filename anymore?

        registry = new TableModuleRegistry();

        engineModule = moduleFactory.createPackageModule("org.terasology.engine");
        Module nui = createSyntheticPackageModule("nui", "3.0.0", "org.terasology.nui");
        Module discordSubsystem = moduleFactory.createPackageModule("org.terasology.subsystem.discordrpc");

        createDependency(engineModule, nui);

        registry.add(nui);
        registry.add(engineModule);
        registry.add(discordSubsystem);

        loadModulesFromApplicationPath(pathManager);
        ensureModulesDependOnEngine();

        setupSandbox();
        loadEnvironment(Sets.newHashSet(engineModule, nui, discordSubsystem), true);
        installManager = new ModuleInstallManager(this, masterServerAddress);
    }

    private  void createDependency(Module parent, Module child) {
        DependencyInfo dependencyInfo = new DependencyInfo();
        dependencyInfo.setId(child.getId());
        dependencyInfo.setMinVersion(child.getVersion());
        dependencyInfo.setMaxVersion(child.getVersion().getNextPatchVersion());
        parent.getMetadata().getDependencies().add(dependencyInfo);
    }

    /**
     * I wondered why this is important, and found MovingBlocks/Terasology#1450.
     * It's not a worry that the engine module wouldn't be loaded without it.
     * It's about ordering: some things run in an order derived from the dependency
     * tree, and we want to make sure engine is at the root of it.
     */
    private void ensureModulesDependOnEngine() {
        DependencyInfo engineDep = new DependencyInfo();
        engineDep.setId(engineModule.getId());
        engineDep.setMinVersion(engineModule.getVersion());
        engineDep.setMaxVersion(engineModule.getVersion().getNextMinorVersion());

        HashSet<Name> engineModules = Sets.newHashSet(engineModule.getId());
        engineModules.addAll(engineModule.getMetadata().getDependencies().stream().map(DependencyInfo::getId).collect(Collectors.toList()));

        registry.stream()
                .filter(mod -> !engineModules.contains(mod.getId()))
                .forEach(mod -> mod.getMetadata().getDependencies().add(engineDep));
    }

    private void loadModulesFromApplicationPath(PathManager pathManager) {
        ModulePathScanner scanner = new ModulePathScanner(moduleFactory);
        List<File> paths = pathManager.getModulePaths().stream().map(Path::toFile).collect(Collectors.toList());
        scanner.scan(registry, paths);
    }

    public ModuleManager(Config config) {
        this(config, Collections.emptyList());
    }

    public ModuleManager(Config config, List<Class<?>> classesOnClasspathsToAddToEngine) {
        this(config.getNetwork().getMasterServer(), classesOnClasspathsToAddToEngine);
    }

    private ModuleMetadataJsonAdapter newMetadataReader() {
        final ModuleMetadataJsonAdapter metadataJsonAdapter = new ModuleMetadataJsonAdapter();
        for (ModuleExtension ext : StandardModuleExtension.values()) {
            metadataJsonAdapter.registerExtension(ext.getKey(), ext.getValueType());
        }
        for (ModuleExtension ext : ExtraDataModuleExtension.values()) {
            metadataJsonAdapter.registerExtension(ext.getKey(), ext.getValueType());
        }
        return metadataJsonAdapter;
    }

    private Module createSyntheticPackageModule(String id, String version, String packageName) {
        ModuleMetadata metadata = new ModuleMetadata(
                new Name(id),
                new Version(version));
        return moduleFactory.createPackageModule(
                metadata,
                packageName);
    }

    private void setupSandbox() {
        ExternalApiWhitelist.CLASSES.stream().forEach(clazz ->
                permissionProviderFactory.getBasePermissionSet().addAPIClass(clazz));
        ExternalApiWhitelist.PACKAGES.stream().forEach(packagee ->
                permissionProviderFactory.getBasePermissionSet().addAPIPackage(packagee));

        APIScanner apiScanner = new APIScanner(permissionProviderFactory);
        registry.stream().map(Module::getModuleManifest).forEach(apiScanner::scan);

        permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson", ReflectPermission.class);
        permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson.internal", ReflectPermission.class);

        Policy.setPolicy(new ModuleSecurityPolicy());
        System.setSecurityManager(new ModuleSecurityManager());
    }

    /**
     * the registry this ModuleManager uses internally
     *
     * @deprecated Use {@link #resolveAndLoadEnvironment} if you need module dependency resolution.
     */
    @Deprecated(/*since="4.4.0"*/)
    public ModuleRegistry getRegistry() {
        return registry;
    }

    public ModuleInstallManager getInstallManager() {
        return installManager;
    }

    public ModuleEnvironment getEnvironment() {
        return environment;
    }

    /** Create and register a new module for this package. */
    public Module registerPackageModule(String packageName) {
        Module module = moduleFactory.createPackageModule(packageName);
        registry.add(module);
        ensureModulesDependOnEngine();
        return module;
    }

    public void resolveAndLoadEnvironment(Name... modules) {
        resolveAndLoadEnvironment(Arrays.asList(modules));
    }

    public void resolveAndLoadEnvironment(Iterable<Name> modules) {
        DependencyResolver resolver = new DependencyResolver(registry);
        ResolutionResult result = resolver.resolve(modules);

        if (!result.isSuccess()) {
            // TODO: worth its own exception class?
            throw new RuntimeException("Failed to resolve dependencies for " + modules);
        }

        loadEnvironment(result.getModules(), true);
    }

    public ModuleEnvironment loadEnvironment(Set<Module> modules, boolean asPrimary) {
        Set<Module> finalModules = Sets.newLinkedHashSet(modules);
        finalModules.add(engineModule);
        ModuleEnvironment newEnvironment;
        boolean permissiveSecurityEnabled = Boolean.parseBoolean(System.getProperty(SystemConfig.PERMISSIVE_SECURITY_ENABLED_PROPERTY));
        if (permissiveSecurityEnabled) {
            newEnvironment = new ModuleEnvironment(finalModules, wrappingPermissionProviderFactory);
        } else {
            newEnvironment = new ModuleEnvironment(finalModules, permissionProviderFactory);
        }
        if (asPrimary) {
            environment = newEnvironment;
        }
        return newEnvironment;
    }

    public ModuleMetadataJsonAdapter getModuleMetadataReader() {
        // FIXME: is this used anywhere? (MTE?)
        return metadataReader;
    }

    public ModuleFactory getModuleFactory() {
        return moduleFactory;
    }

}

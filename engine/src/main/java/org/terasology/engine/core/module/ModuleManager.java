// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.gestalt.i18n.I18nMap;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter;
import org.terasology.gestalt.module.ModulePathScanner;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.TableModuleRegistry;
import org.terasology.gestalt.module.dependencyresolution.DependencyInfo;
import org.terasology.gestalt.module.resources.ClasspathFileSource;
import org.terasology.gestalt.module.sandbox.APIScanner;
import org.terasology.gestalt.module.sandbox.ModuleSecurityManager;
import org.terasology.gestalt.module.sandbox.ModuleSecurityPolicy;
import org.terasology.gestalt.module.sandbox.PermissionProviderFactory;
import org.terasology.gestalt.module.sandbox.StandardPermissionProviderFactory;
import org.terasology.gestalt.module.sandbox.WarnOnlyProviderFactory;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;

import java.lang.reflect.ReflectPermission;
import java.nio.file.Path;
import java.security.Policy;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleManager {
    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);
    private final ModuleFactory moduleFactory;
    private final StandardPermissionProviderFactory permissionProviderFactory = new StandardPermissionProviderFactory();
    private final PermissionProviderFactory wrappingPermissionProviderFactory =
            new WarnOnlyProviderFactory(permissionProviderFactory);

    private final ModuleRegistry registry;
    private ModuleEnvironment environment;
    private final ModuleMetadataJsonAdapter metadataReader;
    private final ModuleInstallManager installManager;

    public ModuleManager(String masterServerAddress) {
        this(masterServerAddress, Collections.emptyList());
    }

    public ModuleManager(String masterServerAddress, List<Class<?>> classesOnClasspathsToAddToEngine) {
        metadataReader = new ModuleMetadataJsonAdapter();
        for (ModuleExtension ext : StandardModuleExtension.values()) {
            metadataReader.registerExtension(ext.getKey(), ext.getValueType());
        }
        for (ModuleExtension ext : ExtraDataModuleExtension.values()) {
            metadataReader.registerExtension(ext.getKey(), ext.getValueType());
        }
        moduleFactory = new ModuleFactory();
        moduleFactory.getModuleMetadataLoaderMap().put(TerasologyConstants.MODULE_INFO_FILENAME.toString(),
                metadataReader);
        moduleFactory.getModuleMetadataLoaderMap().put("engine-module.txt", metadataReader);

        Module engineModule = moduleFactory.createPackageModule("org.terasology.engine");
        Module nuiModule = createSyntheticPackageModule(engineModule, "nui", "0.0.0", "org.terasology.nui");

        registry = new TableModuleRegistry();
        registry.add(engineModule);

        ModulePathScanner scanner = new ModulePathScanner(moduleFactory);
        scanner.scan(registry, PathManager.getInstance().getModulePaths().stream().map(Path::toFile).collect(Collectors.toSet()));

        DependencyInfo engineDep = new DependencyInfo();
        engineDep.setId(engineModule.getId());
        engineDep.setMinVersion(engineModule.getVersion());
        engineDep.setMaxVersion(engineModule.getVersion().getNextPatchVersion());

        registry.stream().filter(mod -> mod != engineModule).forEach(mod -> mod.getMetadata().getDependencies().add(engineDep));
        registry.add(nuiModule);

        setupSandbox();
        loadEnvironment(Sets.newHashSet(engineModule, nuiModule), true);
        installManager = new ModuleInstallManager(this, masterServerAddress);
    }

    public ModuleManager(Config config) {
        this(config, Collections.emptyList());
    }

    public ModuleManager(Config config, List<Class<?>> classesOnClasspathsToAddToEngine) {
        this(config.getNetwork().getMasterServer(), classesOnClasspathsToAddToEngine);
    }

    @NotNull
    private Module createSyntheticPackageModule(Module engineModule, String id, String version, String packageName) {
        ModuleMetadata metadata = new ModuleMetadata(
                new Name(id),
                new Version(version));
        metadata.setDisplayName(new I18nMap(id));
        Module module = moduleFactory.createPackageModule(
                metadata,
                packageName);
        DependencyInfo moduleDep = new DependencyInfo();
        moduleDep.setId(module.getId());
        moduleDep.setMinVersion(module.getVersion());
        moduleDep.setMaxVersion(module.getVersion().getNextPatchVersion());
        engineModule.getMetadata().getDependencies().add(moduleDep);
        return module;
    }

    private void setupSandbox() {
        ExternalApiWhitelist.CLASSES.stream().forEach(clazz ->
                permissionProviderFactory.getBasePermissionSet().addAPIClass(clazz));
        ExternalApiWhitelist.PACKAGES.stream().forEach(packagee ->
                permissionProviderFactory.getBasePermissionSet().addAPIPackage(packagee));

        APIScanner apiScanner = new APIScanner(permissionProviderFactory);
        registry.stream().map(Module::getModuleManifest).forEach(apiScanner::scan);

        permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson", ReflectPermission.class);
        permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson.internal",
                ReflectPermission.class);

        Policy.setPolicy(new ModuleSecurityPolicy());
        System.setSecurityManager(new ModuleSecurityManager());
    }

    public ModuleRegistry getRegistry() {
        return registry;
    }

    public ModuleInstallManager getInstallManager() {
        return installManager;
    }

    public ModuleEnvironment getEnvironment() {
        return environment;
    }

    public ModuleEnvironment loadEnvironment(Set<Module> modules, boolean asPrimary) {
        Set<Module> finalModules = Sets.newLinkedHashSet(modules);
        finalModules.addAll(registry.stream().filter((m) -> m.getResources() instanceof ClasspathFileSource).collect(Collectors.toList()));
        ModuleEnvironment newEnvironment;
        boolean permissiveSecurityEnabled =
                Boolean.parseBoolean(System.getProperty(SystemConfig.PERMISSIVE_SECURITY_ENABLED_PROPERTY));
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
        return metadataReader;
    }

    public ModuleFactory getModuleFactory() {
        return moduleFactory;
    }
}

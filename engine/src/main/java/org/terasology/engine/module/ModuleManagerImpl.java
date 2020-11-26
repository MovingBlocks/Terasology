// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.module;

import com.google.common.collect.Sets;
import org.reflections.Reflections;
import org.reflections.serializers.Serializer;
import org.reflections.serializers.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.Asset;
import org.terasology.config.Config;
import org.terasology.config.SystemConfig;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.module.ClasspathModule;
import org.terasology.module.DependencyInfo;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleLoader;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.ModulePathScanner;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.TableModuleRegistry;
import org.terasology.module.sandbox.APIScanner;
import org.terasology.module.sandbox.BytecodeInjector;
import org.terasology.module.sandbox.ModuleSecurityManager;
import org.terasology.module.sandbox.ModuleSecurityPolicy;
import org.terasology.module.sandbox.PermissionProviderFactory;
import org.terasology.module.sandbox.StandardPermissionProviderFactory;
import org.terasology.module.sandbox.WarnOnlyProviderFactory;
import org.terasology.naming.Name;
import org.terasology.nui.UIWidget;
import org.terasology.reflection.TypeRegistry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ReflectPermission;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleManagerImpl implements ModuleManager {
    private static final Logger logger = LoggerFactory.getLogger(ModuleManagerImpl.class);
    private StandardPermissionProviderFactory permissionProviderFactory = new StandardPermissionProviderFactory();
    private PermissionProviderFactory wrappingPermissionProviderFactory = new WarnOnlyProviderFactory(permissionProviderFactory);

    private ModuleRegistry registry;
    private ModuleEnvironment environment;
    private ModuleMetadataJsonAdapter metadataReader;
    private ModuleInstallManager installManager;

    public ModuleManagerImpl(String masterServerAddress) {
        this(masterServerAddress, Collections.emptyList());
    }

    public ModuleManagerImpl(String masterServerAddress, List<Class<?>> classesOnClasspathsToAddToEngine) {
        metadataReader = new ModuleMetadataJsonAdapter();
        for (ModuleExtension ext : StandardModuleExtension.values()) {
            metadataReader.registerExtension(ext.getKey(), ext.getValueType());
        }
        for (ModuleExtension ext : ExtraDataModuleExtension.values()) {
            metadataReader.registerExtension(ext.getKey(), ext.getValueType());
        }
        Module engineModule;
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/engine-module.txt"), TerasologyConstants.CHARSET)) {
            ModuleMetadata metadata = metadataReader.read(reader);
            List<Class<?>> additionalClassesList = new ArrayList<>(classesOnClasspathsToAddToEngine.size() + 2);
            additionalClassesList.add(Module.class); // provide access to gestalt-module.jar
            additionalClassesList.add(Asset.class); // provide access to gestalt-asset-core.jar
            additionalClassesList.add(UIWidget.class); // provide access to nui.jar
            additionalClassesList.add(TypeRegistry.class); // provide access to nui-reflect.jar
            additionalClassesList.add(KeyboardDevice.class); // provide access to nui-input.jar
            additionalClassesList.addAll(classesOnClasspathsToAddToEngine); // provide access to any facade-provided classes
            Class<?>[] additionalClassesArray = new Class[additionalClassesList.size()];
            additionalClassesArray = additionalClassesList.toArray(additionalClassesArray);
            engineModule = ClasspathModule.create(metadata, getClass(), additionalClassesArray);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read engine metadata", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to convert engine library location to path", e);
        }

        enrichReflectionsWithSubsystems(engineModule);

        registry = new TableModuleRegistry();
        registry.add(engineModule);

        loadModulesFromClassPath();

        ModulePathScanner scanner = new ModulePathScanner(new ModuleLoader(metadataReader));
        scanner.getModuleLoader().setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);
        scanner.scan(registry, PathManager.getInstance().getModulePaths());

        DependencyInfo engineDep = new DependencyInfo();
        engineDep.setId(engineModule.getId());
        engineDep.setMinVersion(engineModule.getVersion());
        engineDep.setMaxVersion(engineModule.getVersion().getNextPatchVersion());

        registry.stream().filter(mod -> mod != engineModule).forEach(mod -> mod.getMetadata().getDependencies().add(engineDep));

        setupSandbox();
        loadEnvironment(Sets.newHashSet(engineModule), true);
        installManager = new ModuleInstallManager(this, masterServerAddress);
    }

    public ModuleManagerImpl(Config config) {
        this(config, Collections.emptyList());
    }

    public ModuleManagerImpl(Config config, List<Class<?>> classesOnClasspathsToAddToEngine) {
        this(config.getNetwork().getMasterServer(), classesOnClasspathsToAddToEngine);
    }

    /**
     * Overrides modules in modules/ with those specified via -classpath in the JVM
     */
    private void loadModulesFromClassPath() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        ModuleLoader loader = new ModuleLoader(metadataReader);
        Enumeration<URL> moduleInfosInClassPath;
        loader.setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);

        // We're looking for jars on the classpath with a module.txt
        try {
            moduleInfosInClassPath = classLoader.getResources(TerasologyConstants.MODULE_INFO_FILENAME.toString());
        } catch (IOException e) {
            logger.warn("Failed to search for classpath modules: {}", e);
            return;
        }

        for (URL url : Collections.list(moduleInfosInClassPath)) {
            if (!url.getProtocol().equalsIgnoreCase("jar")) {
                continue;
            }

            try (Reader reader = new InputStreamReader(url.openStream(), TerasologyConstants.CHARSET)) {
                ModuleMetadata metaData = metadataReader.read(reader);
                String displayName = metaData.getDisplayName().toString();
                Name id = metaData.getId();

                // if the display name is empty or the id is null, this probably isn't a Terasology module
                if (null == id || displayName.equalsIgnoreCase("")) {
                    logger.warn("Found a module-like JAR on the class path with no id or display name. Skipping");
                    logger.warn("{}", url);
                    continue;
                }

                logger.info("Loading module {} from class path at {}", displayName, url.getFile());

                // the url contains a protocol, and points to the module.txt
                // we need to trim both of those away to get the module's path
                String targetFile = url.getFile()
                        .replace("file:", "")
                        .replace("!/" + TerasologyConstants.MODULE_INFO_FILENAME, "")
                        .replace("/" + TerasologyConstants.MODULE_INFO_FILENAME, "");

                // Windows specific check - Path doesn't like /C:/... style Strings indicating files
                if (targetFile.matches("/[a-zA-Z]:.*")) {
                    targetFile = targetFile.substring(1);
                }

                Path path = Paths.get(targetFile);

                Module module = loader.load(path);
                registry.add(module);
            } catch (IOException e) {
                logger.warn("Failed to load module.txt for classpath module {}", url);
            }
        }
    }

    private void setupSandbox() {
        ExternalApiWhitelist.CLASSES.stream().forEach(clazz ->
                permissionProviderFactory.getBasePermissionSet().addAPIClass(clazz));
        ExternalApiWhitelist.PACKAGES.stream().forEach(packagee ->
                permissionProviderFactory.getBasePermissionSet().addAPIPackage(packagee));

        APIScanner apiScanner = new APIScanner(permissionProviderFactory);
        registry.stream().filter(Module::isOnClasspath).forEach(apiScanner::scan);

        permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson", ReflectPermission.class);
        permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson.internal", ReflectPermission.class);

        Policy.setPolicy(new ModuleSecurityPolicy());
        System.setSecurityManager(new ModuleSecurityManager());
    }

    @Override
    public ModuleRegistry getRegistry() {
        return registry;
    }

    @Override
    public ModuleInstallManager getInstallManager() {
        return installManager;
    }

    @Override
    public ModuleEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public ModuleEnvironment loadEnvironment(Set<Module> modules, boolean asPrimary) {
        Set<Module> finalModules = Sets.newLinkedHashSet(modules);
        finalModules.addAll(registry.stream().filter(Module::isOnClasspath).collect(Collectors.toList()));
        ModuleEnvironment newEnvironment;
        boolean permissiveSecurityEnabled = Boolean.parseBoolean(System.getProperty(SystemConfig.PERMISSIVE_SECURITY_ENABLED_PROPERTY));
        if (permissiveSecurityEnabled) {
            newEnvironment = new ModuleEnvironment(finalModules, wrappingPermissionProviderFactory, Collections.<BytecodeInjector>emptyList());
        } else {
            newEnvironment = new ModuleEnvironment(finalModules, permissionProviderFactory, Collections.<BytecodeInjector>emptyList());
        }
        if (asPrimary) {
            environment = newEnvironment;
        }
        return newEnvironment;
    }

    @Override
    public ModuleMetadataJsonAdapter getModuleMetadataReader() {
        return metadataReader;
    }

    private void enrichReflectionsWithSubsystems(Module engineModule) {
        Serializer serializer = new XmlSerializer();
        try {
            Enumeration<URL> urls = ModuleManagerImpl.class.getClassLoader().getResources("reflections.cache");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url.getPath().contains("subsystem")) {
                    Reflections subsystemReflections = serializer.read(url.openStream());
                    engineModule.getReflectionsFragment().merge(subsystemReflections);
                }
            }
        } catch (IOException e) {
            logger.error("Cannot enrich engine's reflections with subsystems");
        }
    }
}

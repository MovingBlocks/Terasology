/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.engine.module;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.SystemConfig;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter;
import org.terasology.gestalt.module.ModulePathScanner;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.TableModuleRegistry;
import org.terasology.gestalt.module.dependencyresolution.DependencyInfo;
import org.terasology.gestalt.module.sandbox.APIScanner;
import org.terasology.gestalt.module.sandbox.ModuleSecurityManager;
import org.terasology.gestalt.module.sandbox.ModuleSecurityPolicy;
import org.terasology.gestalt.module.sandbox.PermissionProviderFactory;
import org.terasology.gestalt.module.sandbox.StandardPermissionProviderFactory;
import org.terasology.gestalt.module.sandbox.WarnOnlyProviderFactory;

import java.lang.reflect.ReflectPermission;
import java.security.Policy;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ModuleManagerImpl implements ModuleManager {
    private static final Logger logger = LoggerFactory.getLogger(ModuleManagerImpl.class);
    private final ModuleFactory moduleFactory;
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
        moduleFactory = new ModuleFactory();
        moduleFactory.getModuleMetadataLoaderMap().put(TerasologyConstants.MODULE_INFO_FILENAME.toString(), metadataReader);
        moduleFactory.getModuleMetadataLoaderMap().put("engine-module.txt", metadataReader);
        
//        Module engineModule;
//        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/engine-module.txt"), TerasologyConstants.CHARSET)) {
//            ModuleMetadata metadata = metadataReader.read(reader);
//            List<Class<?>> additionalClassesList = new ArrayList<>(classesOnClasspathsToAddToEngine.size() + 4);
//            additionalClassesList.add(Module.class); // provide access to gestalt-module.jar
//            additionalClassesList.add(Asset.class); // provide access to gestalt-asset-core.jar
//            additionalClassesList.add(UIWidget.class); // provide access to nui.jar
//            additionalClassesList.add(TypeRegistry.class); // provide access to nui-reflect.jar
//            additionalClassesList.addAll(classesOnClasspathsToAddToEngine); // provide access to any facade-provided classes
//            Class<?>[] additionalClassesArray = new Class[additionalClassesList.size()];
//            additionalClassesArray = additionalClassesList.toArray(additionalClassesArray);
//            engineModule = ClasspathModule.create(metadata, getClass(), additionalClassesArray);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to read engine metadata", e);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException("Failed to convert engine library location to path", e);
//        }

        Module engineModule = moduleFactory.createPackageModule("org.terasology");

        registry = new TableModuleRegistry();
        registry.add(engineModule);

        ModulePathScanner scanner = new ModulePathScanner(moduleFactory);
        scanner.scan(registry, PathManager.getInstance().getModulePathFiles());

        loadModulesFromClassPath(); // TODO: to remove - ClassPath modules depricated by gestalt

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
//        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//        ModuleLoader loader = new ModuleLoader(metadataReader);
//        Enumeration<URL> moduleInfosInClassPath;
//        loader.setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);
//
//        // We're looking for jars on the classpath with a module.txt
//        try {
//            moduleInfosInClassPath = classLoader.getResources(TerasologyConstants.MODULE_INFO_FILENAME.toString());
//        } catch (IOException e) {
//            logger.warn("Failed to search for classpath modules: {}", e);
//            return;
//        }
//
//        for (URL url : Collections.list(moduleInfosInClassPath)) {
//            if (!url.getProtocol().equalsIgnoreCase("jar")) {
//                continue;
//            }
//
//            try (Reader reader = new InputStreamReader(url.openStream(), TerasologyConstants.CHARSET)) {
//                ModuleMetadata metaData = metadataReader.read(reader);
//                String displayName = metaData.getDisplayName().toString();
//                Name id = metaData.getId();
//
//                // if the display name is empty or the id is null, this probably isn't a Terasology module
//                if (null == id || displayName.equalsIgnoreCase("")) {
//                    logger.warn("Found a module-like JAR on the class path with no id or display name. Skipping");
//                    logger.warn("{}", url);
//                    continue;
//                }
//
//                logger.info("Loading module {} from class path at {}", displayName, url.getFile());
//
//                // the url contains a protocol, and points to the module.txt
//                // we need to trim both of those away to get the module's path
//                String targetFile = url.getFile()
//                        .replace("file:", "")
//                        .replace("!/" + TerasologyConstants.MODULE_INFO_FILENAME, "")
//                        .replace("/" + TerasologyConstants.MODULE_INFO_FILENAME, "");
//
//                // Windows specific check - Path doesn't like /C:/... style Strings indicating files
//                if (targetFile.matches("/[a-zA-Z]:.*")) {
//                    targetFile = targetFile.substring(1);
//                }
//
//                Path path = Paths.get(targetFile);
//
//                Module module = loader.load(path);
//                registry.add(module);
//            } catch (IOException e) {
//                logger.warn("Failed to load module.txt for classpath module {}", url);
//            }
//        }
    }

    private void setupSandbox() {
        ExternalApiWhitelist.CLASSES.stream().forEach(clazz ->
                permissionProviderFactory.getBasePermissionSet().addAPIClass(clazz));
        ExternalApiWhitelist.PACKAGES.stream().forEach(packagee ->
                permissionProviderFactory.getBasePermissionSet().addAPIPackage(packagee));

        APIScanner apiScanner = new APIScanner(permissionProviderFactory);
//        registry.stream().filter(Module::isOnClasspath).forEach(apiScanner::scan); // TODO: to remove - classpath modules depricated by gestalt

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
//        finalModules.addAll(registry.stream().filter(Module::isOnClasspath).collect(Collectors.toList())); // TODO: to remove - classpath modules depricated by gestalt
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

    @Override
    public ModuleMetadataJsonAdapter getModuleMetadataReader() {
        return metadataReader;
    }

    @Override
    public ModuleFactory getModuleFactory() {
        return moduleFactory;
    }
}

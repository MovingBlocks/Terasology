/*
 * Copyright 2014 MovingBlocks
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
import org.terasology.assets.Asset;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.module.*;
import org.terasology.module.sandbox.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ReflectPermission;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Policy;
import java.util.*;
import java.util.stream.Collectors;

public class ModuleManagerImpl implements ModuleManager {

    private StandardPermissionProviderFactory permissionProviderFactory = new StandardPermissionProviderFactory();

    private ModuleRegistry registry;
    private ModuleEnvironment environment;
    private ModuleMetadataJsonAdapter metadataReader;
    private static Logger logger = LoggerFactory.getLogger(ModuleManagerImpl.class);

    public ModuleManagerImpl() {
        metadataReader = new ModuleMetadataJsonAdapter();
        for (ModuleExtension ext : StandardModuleExtension.values()) {
            metadataReader.registerExtension(ext.getKey(), ext.getValueType());
        }
        Module engineModule;
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/engine-module.txt"), TerasologyConstants.CHARSET)) {
            ModuleMetadata metadata = metadataReader.read(reader);
            engineModule = ClasspathModule.create(metadata, getClass(), Module.class, Asset.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read engine metadata", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to convert engine library location to path", e);
        }

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
    }

    /**
     * Overrides modules in modules/ with those specified via -classpath in the JVM
     */
    private void loadModulesFromClassPath() {
        try {
            // Only attempt this if we're using the standard URLClassLoader
            if (ClassLoader.getSystemClassLoader() instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                ModuleLoader loader = new ModuleLoader(metadataReader);
                loader.setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);

                // We're looking for jars on the classpath with a module.txt
                Enumeration<URL> moduleInfosInClassPath = urlClassLoader.findResources(TerasologyConstants.MODULE_INFO_FILENAME.toString());
                for (URL url : Collections.list(moduleInfosInClassPath)) {
                    if(!url.getProtocol().equalsIgnoreCase("jar")) {
                        continue;
                    }
                    Reader reader = new InputStreamReader(url.openStream());
                    String displayName = metadataReader.read(reader).getDisplayName().toString();
                    logger.info("Loading module {} from class path at {}", displayName, url.getFile());

                    // the url contains a protocol, and points to the module.txt
                    // we need to trim both of those away to get the module's path
                    Path path = Paths.get(url.getFile()
                            .replace("file:", "")
                            .replace("!/" + TerasologyConstants.MODULE_INFO_FILENAME, "")
                            .replace("/" + TerasologyConstants.MODULE_INFO_FILENAME, "")
                    );

                    Module module = loader.load(path);
                    registry.add(module);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read classpath modules", e);
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
    public ModuleEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public ModuleEnvironment loadEnvironment(Set<Module> modules, boolean asPrimary) {
        Set<Module> finalModules = Sets.newLinkedHashSet(modules);
        finalModules.addAll(registry.stream().filter(Module::isOnClasspath).collect(Collectors.toList()));
        ModuleEnvironment newEnvironment = new ModuleEnvironment(finalModules, permissionProviderFactory, Collections.<BytecodeInjector>emptyList());
        if (asPrimary) {
            environment = newEnvironment;
        }
        return newEnvironment;
    }

    @Override
    public ModuleMetadataJsonAdapter getModuleMetadataReader() {
        return metadataReader;
    }
}

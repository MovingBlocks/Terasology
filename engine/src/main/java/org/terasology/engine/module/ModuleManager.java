// Copyright 2021 The Terasology Foundation
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
import org.terasology.module.sandbox.ModuleSecurityManager;
import org.terasology.module.sandbox.ModuleSecurityPolicy;
import org.terasology.module.sandbox.PermissionProvider;
import org.terasology.module.sandbox.PermissionProviderFactory;
import org.terasology.module.sandbox.StandardPermissionProviderFactory;
import org.terasology.module.sandbox.WarnOnlyProviderFactory;
import org.terasology.nui.UIWidget;
import org.terasology.reflection.TypeRegistry;
import org.terasology.utilities.Jvm;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Verify.verifyNotNull;

public class ModuleManager {
    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);
    private final StandardPermissionProviderFactory permissionProviderFactory = new StandardPermissionProviderFactory();
    private final PermissionProviderFactory wrappingPermissionProviderFactory = new WarnOnlyProviderFactory(permissionProviderFactory);

    private final ModuleRegistry registry;
    private ModuleEnvironment environment;
    private final ModuleMetadataJsonAdapter metadataReader;
    private final ModuleInstallManager installManager;

    /** Set this environment variable to "true" to load all modules in the classpath by default. */
    final String LOAD_CLASSPATH_MODULES_ENV = "terasology_load_classpath_modules";

    public ModuleManager(String masterServerAddress) {
        this(masterServerAddress, Collections.emptyList());
    }

    public ModuleManager(String masterServerAddress, List<Class<?>> classesOnClasspathsToAddToEngine) {
        PathManager pathManager = PathManager.getInstance();  // get early so if it needs to initialize, it does it now

        metadataReader = newMetadataReader();

        Module engineModule = loadEngineModule(classesOnClasspathsToAddToEngine);

        registry = new TableModuleRegistry();
        registry.add(engineModule);

        if (doLoadModulesFromClasspath()) {
            loadModulesFromClassPath();
        } else {
            logger.debug("Not loading classpath modules. ({} = {})",
                    LOAD_CLASSPATH_MODULES_ENV, System.getenv(LOAD_CLASSPATH_MODULES_ENV));
        }

        loadModulesFromApplicationPath(pathManager);

        ensureModulesDependOnEngine(engineModule);

        setupSandbox();
        loadEnvironment(Sets.newHashSet(engineModule), true);
        installManager = new ModuleInstallManager(this, masterServerAddress);
    }

    /**
     * I wondered why this is important, and found MovingBlocks/Terasology#1450.
     * It's not a worry that the engine module wouldn't be loaded without it. 
     * It's about ordering: some things run in an order derived from the dependency 
     * tree, and we want to make sure engine is at the root of it.
     */   
    private void ensureModulesDependOnEngine(Module engineModule) {
        DependencyInfo engineDep = new DependencyInfo();
        engineDep.setId(engineModule.getId());
        engineDep.setMinVersion(engineModule.getVersion());
        engineDep.setMaxVersion(engineModule.getVersion().getNextPatchVersion());

        registry.stream().filter(mod -> mod != engineModule).forEach(mod -> mod.getMetadata().getDependencies().add(engineDep));
    }

    private void loadModulesFromApplicationPath(PathManager pathManager) {
        ModulePathScanner scanner = new ModulePathScanner(new ModuleLoader(metadataReader));
        scanner.getModuleLoader().setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);
        scanner.scan(registry, pathManager.getModulePaths());
    }

    private Module loadEngineModule(List<Class<?>> classesOnClasspathsToAddToEngine) {
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

        return engineModule;
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

    boolean doLoadModulesFromClasspath() {
        String env = System.getenv(LOAD_CLASSPATH_MODULES_ENV);
        return Boolean.parseBoolean(env);
    }

    /**
     * Overrides modules in modules/ with those specified via -classpath in the JVM
     */
    void loadModulesFromClassPath() {
        logger.debug("loadModulesFromClassPath with classpath:");
        Jvm.logClasspath(logger);

        ModuleLoader loader = new ClasspathSupportingModuleLoader(metadataReader, true, true);
        loader.setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);

        List<Path> classPaths = Arrays.stream(
                System.getProperty("java.class.path").split(System.getProperty("path.separator", ":"))
        ).map(Paths::get).collect(Collectors.toList());

        // I thought I'd make the ClasspathSupporting stuff in the shape of a ModuleLoader
        // so I could use it with the existing ModulePathScanner, but no. The inputs to that
        // are the _parent directories_ of what we have.
        for (Path path : classPaths) {
            attemptToLoadModule(loader, path);
        }
    }

    public void attemptToLoadModule(ModuleLoader loader, Path path) {
        // The conditions here mirror those of org.terasology.module.ModulePathScanner.loadModule

        Module module;
        try {
            module = loader.load(path);
        } catch (IOException e) {
            logger.error("Failed to load classpath module {}", path, e);
            return;
        }

        if (module == null) {
            return;
        }

        boolean isNew = registry.add(module);
        if (isNew) {
            logger.info("Discovered module: {}", module);
        } else {
            logger.warn("Discovered duplicate module: {}-{}, skipping {}",
                    module.getId(), module.getVersion(), path);
        }

    }

    public Module loadClasspathModule(Path path) throws IOException {
        ModuleLoader loader = new ClasspathSupportingModuleLoader(metadataReader, true, false);
        loader.setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);

        @SuppressWarnings("UnstableApiUsage") Module module = verifyNotNull(loader.load(path), "Failed to load module from %s", path);
        boolean isNew = registry.add(module);
        if (isNew) {
            logger.info("Discovered module: {}", module);
        } else {
            logger.warn("Discovered duplicate module: {}-{}, skipping {}",
                    module.getId(), module.getVersion(), path);
        }
        return module;
    }

    private void setupSandbox() {
        ExternalApiWhitelist.CLASSES.stream().forEach(clazz ->
                permissionProviderFactory.getBasePermissionSet().addAPIClass(clazz));
        ExternalApiWhitelist.PACKAGES.stream().forEach(packagee ->
                permissionProviderFactory.getBasePermissionSet().addAPIPackage(packagee));

        APIScanner apiScanner = new APIScannerTolerantOfAssetOnlyModules(permissionProviderFactory);
        apiScanner.scan(registry);

        permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson", ReflectPermission.class);
        permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson.internal", ReflectPermission.class);

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
        finalModules.addAll(registry.stream().filter(Module::isOnClasspath).collect(Collectors.toList()));
        ModuleEnvironment newEnvironment;
        boolean permissiveSecurityEnabled = Boolean.parseBoolean(System.getProperty(SystemConfig.PERMISSIVE_SECURITY_ENABLED_PROPERTY));
        if (permissiveSecurityEnabled) {
            newEnvironment = new ModuleEnvironment(finalModules, wrappingPermissionProviderFactory, Collections.emptyList());
        } else {
            newEnvironment = new ModuleEnvironment(finalModules, permissionProviderFactory, Collections.emptyList());
        }
        if (asPrimary) {
            environment = newEnvironment;
        }
        return newEnvironment;
    }

    public ModuleMetadataJsonAdapter getModuleMetadataReader() {
        return metadataReader;
    }

    private void enrichReflectionsWithSubsystems(Module engineModule) {
        Serializer serializer = new XmlSerializer();
        try {
            Enumeration<URL> urls = ModuleManager.class.getClassLoader().getResources("reflections.cache");
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

    public PermissionProvider getPermissionProvider(Module module) {
        return permissionProviderFactory.createPermissionProviderFor(module);
    }
}

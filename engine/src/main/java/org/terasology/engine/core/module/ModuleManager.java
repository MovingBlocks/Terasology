// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module;

import com.google.common.base.VerifyException;
import com.google.common.collect.Sets;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.utilities.Jvm;
import org.terasology.gestalt.di.DefaultBeanContext;
import org.terasology.gestalt.di.index.CompoundClassIndex;
import org.terasology.gestalt.di.index.UrlClassIndex;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleFactory;
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
import org.terasology.gestalt.module.sandbox.PermissionProvider;
import org.terasology.gestalt.module.sandbox.PermissionProviderFactory;
import org.terasology.gestalt.module.sandbox.PermissionSet;
import org.terasology.gestalt.module.sandbox.StandardPermissionProviderFactory;
import org.terasology.gestalt.module.sandbox.WarnOnlyProviderFactory;
import org.terasology.gestalt.naming.Name;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.Policy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Verify.verify;

public class ModuleManager {
    /** Set this property to "true" to allow modules on the classpath. */
    public static final String LOAD_CLASSPATH_MODULES_PROPERTY = "org.terasology.load_classpath_modules";

    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);
    private final StandardPermissionProviderFactory permissionProviderFactory = new StandardPermissionProviderFactory();
    private final PermissionProviderFactory wrappingPermissionProviderFactory =
            new WarnOnlyProviderFactory(permissionProviderFactory);

    private final ModuleRegistry registry = new TableModuleRegistry();
    private ModuleEnvironment environment;
    private final ModuleMetadataJsonAdapter metadataReader = newMetadataReader();
    private final ModuleFactory moduleFactory = newModuleFactory(metadataReader);
    private final ModuleInstallManager installManager;
    private final Module engineModule;

    public ModuleManager(String masterServerAddress) {
        this(masterServerAddress, Collections.emptyList());
    }

    public ModuleManager(String masterServerAddress, List<Class<?>> classesOnClasspathsToAddToEngine) {
        engineModule = loadAndConfigureEngineModule(moduleFactory, classesOnClasspathsToAddToEngine);
        registry.add(engineModule);

        if (isLoadingClasspathModules()) {
            loadModulesFromClassPath();
        }
        loadModulesFromApplicationPath(PathManager.getInstance());

        ensureModulesDependOnEngine();

        setupSandbox();
        loadEnvironment(Sets.newHashSet(engineModule), true);
        installManager = new ModuleInstallManager(this, masterServerAddress);
    }

    public ModuleManager(Config config) {
        this(config, Collections.emptyList());
    }

    public ModuleManager(Config config, List<Class<?>> classesOnClasspathsToAddToEngine) {
        this(config.getNetwork().getMasterServer(), classesOnClasspathsToAddToEngine);
    }

    public static boolean isLoadingClasspathModules() {
        return Boolean.getBoolean(LOAD_CLASSPATH_MODULES_PROPERTY);
    }

    /** Create a ModuleFactory configured for Terasology modules. */
    private static ModuleFactory newModuleFactory(ModuleMetadataJsonAdapter metadataReader) {
        final ModuleFactory moduleFactory;
        if (isLoadingClasspathModules()) {
            moduleFactory = new ClasspathCompromisingModuleFactory();
            Jvm.logClasspath(logger);
        } else {
            moduleFactory = new ModuleFactory();
        }
        moduleFactory.setDefaultLibsSubpath("build/libs");

        Map<String, ModuleMetadataLoader> mmlm = moduleFactory.getModuleMetadataLoaderMap();
        mmlm.put(TerasologyConstants.MODULE_INFO_FILENAME.toString(), metadataReader);
        return moduleFactory;
    }

    /**
     * Ensure all modules declare a dependency on the engine module.
     * <p>
     * This is to ensure that the set of modules is a graph with a single root.
     * We need this to ensure the engine is loaded <em>before</em> other modules
     * when things iterate over the module list in dependency order.
     * <p>
     * See <a href="https://github.com/MovingBlocks/Terasology/issues/1450">#1450</a>.
     */
    private void ensureModulesDependOnEngine() {
        DependencyInfo engineDep = new DependencyInfo();
        engineDep.setId(engineModule.getId());
        engineDep.setMinVersion(engineModule.getVersion());

        Set<Name> engineModules = Sets.newHashSet(engineModule.getId());
        engineModules.addAll(engineModule.getMetadata().getDependencies().stream().map(DependencyInfo::getId).collect(Collectors.toList()));

        registry.stream()
                .filter(mod -> !engineModules.contains(mod.getId()))
                .forEach(mod -> mod.getMetadata().getDependencies().add(engineDep));
    }

    private void loadModulesFromApplicationPath(PathManager pathManager) {
        ModulePathScanner scanner = new ModulePathScanner(moduleFactory);
        List<File> paths = pathManager.getModulePaths().stream()
                .map(Path::toFile)
                .collect(Collectors.toList());
        scanner.scan(registry, paths);
    }

    private void loadModulesFromClassPath() {
        ClasspathCompromisingModuleFactory ccModuleFactory = (ClasspathCompromisingModuleFactory) this.moduleFactory;
        for (String metadataName : ccModuleFactory.getModuleMetadataLoaderMap().keySet()) {
            Enumeration<URL> urls;
            try {
                urls = ClassLoader.getSystemResources(metadataName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                logger.debug("Probably a module in U:{}", url);
                Path path = ccModuleFactory.canonicalModuleLocation(metadataName, url);
                Module module;
                try {
                    module = ccModuleFactory.createModule(path.toFile());
                } catch (IOException e) {
                    logger.warn("Failed to create module from {}", path, e);
                    continue;
                }
                if (registry.add(module)) {
                    logger.info("Loaded {} from {}", module.getId(), path); //NOPMD
                } else {
                    logger.info("Module {} from {} was a duplicate; not registering this copy.", module.getId(), path); //NOPMD
                }
            }
        }
    }

    /**
     * Load and configure the engine module.
     * <p>
     * The engine module is the parts of the engine which are available to be called directly
     * from other modules. Unlike other modules, engine classes are on the classpath and not
     * restricted by the ModuleClassLoader.
     * <p>
     * This function is static so it can be tested without needing a ModuleManager instance.
     *
     * @param moduleFactory used to create the module
     * @param classesOnClasspathsToAddToEngine added to the module's reflections manifest
     */
    static Module loadAndConfigureEngineModule(ModuleFactory moduleFactory, List<Class<?>> classesOnClasspathsToAddToEngine) {
        // Start by creating a gestalt Module for the Java package `org.terasology.engine`.
        Module packageModule = moduleFactory.createPackageModule("org.terasology.engine");

        // We need to add reflections from our subsystems and other classes.
        CompoundClassIndex packageClassIndex = new CompoundClassIndex();
        packageClassIndex.add(packageModule.getClassIndex());

        Reflections packageReflections = new Reflections();
        ConfigurationBuilder config = reflectionsConfigurationFrom(packageReflections);

        Collection<File> classPaths = new HashSet<>(packageModule.getClasspaths());
        for (Class<?> aClass : classesOnClasspathsToAddToEngine) {
            URL url = ClasspathHelper.forClass(aClass);
            config.addUrls(url);  // include this in reflections scan
            classPaths.add(urlToFile(url));  // also include in Module.moduleClasspaths
            packageClassIndex.add(UrlClassIndex.byClassLoader(aClass.getClassLoader()));
            logger.debug("Adding path to engine module for class: {} {}", url, aClass);
        }

        if (!config.getUrls().isEmpty()) {
            Reflections reflectionsWithSubsystems = new Reflections(config);
            packageReflections.merge(reflectionsWithSubsystems);
        }

        // We need the class predicate to include classes in subsystems and whatnot. We can't change it in an
        // existing module, so make a new one based on the one from the factory.
        // TODO: expand the ModuleFactory interface to make this whole thing less awkward
        return new Module(
                packageModule.getMetadata(),
                packageModule.getResources(),
                classPaths,
                packageClassIndex,
                clazz ->
                        packageModule.getClassPredicate().test(clazz)
                                || config.getUrls().contains(ClasspathHelper.forClass(clazz))
        );
    }

    /** Create a new ConfigurationBuilder from an existing Reflections instance. */
    private static ConfigurationBuilder reflectionsConfigurationFrom(Reflections packageReflections) {
        ConfigurationBuilder config = new ConfigurationBuilder();
        Set<Scanner> scanners = packageReflections.getConfiguration().getScanners();
        config.setScanners(scanners.toArray(new Scanner[0]));
        return config;
    }

    private static ModuleMetadataJsonAdapter newMetadataReader() {
        final ModuleMetadataJsonAdapter metadataJsonAdapter = new ModuleMetadataJsonAdapter();
        for (ModuleExtension ext : StandardModuleExtension.values()) {
            metadataJsonAdapter.registerExtension(ext.getKey(), ext.getValueType());
        }
        for (ModuleExtension ext : ExtraDataModuleExtension.values()) {
            metadataJsonAdapter.registerExtension(ext.getKey(), ext.getValueType());
        }
        return metadataJsonAdapter;
    }

    private void setupSandbox() {
        PermissionSet permissionSet = permissionProviderFactory.getBasePermissionSet();
        ExternalApiWhitelist.CLASSES.forEach(permissionSet::addAPIClass);
        ExternalApiWhitelist.PACKAGES.forEach(permissionSet::addAPIPackage);

        APIScanner apiScanner = new APIScanner(permissionProviderFactory);
        for (Module module : registry) {
            apiScanner.scan(module.getClassIndex());
        }

        permissionSet.grantPermission("com.google.gson", ReflectPermission.class);
        permissionSet.grantPermission("com.google.gson.internal", ReflectPermission.class);

        //noinspection ConstantConditions - this reference is to help find this if this method gets separated from the reactor dependency
        if (reactor.core.scheduler.Scheduler.class != null) {  //lgtm [java/useless-null-check]
            // In theory, PropertyPermission has wildcard matching and "reactor.*" should be sufficient to grant read access to all
            // reactor configuration properties.
            permissionSet.grantPermission(new PropertyPermission("reactor.*", "read"));
            // In practice, the permission checks fail unless these are each named explicitly.
            permissionSet.grantPermission(new PropertyPermission("reactor.bufferSize.x", "read"));
            permissionSet.grantPermission(new PropertyPermission("reactor.bufferSize.small", "read"));
            permissionSet.grantPermission(new PropertyPermission("reactor.trace.operatorStacktrace", "read"));
            permissionSet.grantPermission(new PropertyPermission("reactor.schedulers.defaultPoolSize", "read"));
            permissionSet.grantPermission(new PropertyPermission("reactor.schedulers.defaultBoundedElasticSize", "read"));
            permissionSet.grantPermission(new PropertyPermission("reactor.schedulers.defaultBoundedElasticQueueSize", "read"));
        }

        if (Runtime.version().feature() < 18 || "allow".equals(System.getProperty("java.security.manager"))) {
            Policy.setPolicy(new ModuleSecurityPolicy());
            System.setSecurityManager(new ModuleSecurityManager());
        } else {
            logger.warn("SecurityManager is disabled starting with Java 18 - module sandbox functionality is limited!");
            logger.warn("To enable SecurityManager, use the \"-Djava.security.manager=allow\" JVM option.");
        }
    }

    /**
     * the registry this ModuleManager uses internally
     *
     * @deprecated Use {@link #resolveAndLoadEnvironment} if you need module dependency resolution.
     */
    @Deprecated(since = "4.4.0")
    public ModuleRegistry getRegistry() {
        return registry;
    }

    /**
     * Look up the module registered with this path.
     * <p>
     * This queries modules that have already been registered. It does <em>not</em> register
     * anything new for the path or make any addition to the current module search path.
     *
     * @return empty if no modules match the path
     * @throws VerifyException if more than one module matches
     */
    public Optional<Module> getModuleAt(Path path) {
        final var matchingModules = new HashSet<Module>();
        final var absolutePath = path.toAbsolutePath().normalize();
        for (Module module : registry) {
            for (Path modulePath : module.getResources().getRootPaths()) {
                if (modulePath.toAbsolutePath().normalize().equals(absolutePath)) {
                    matchingModules.add(module);
                }
            }
        }
        if (matchingModules.isEmpty()) {
            return Optional.empty();
        } else {
            verify(matchingModules.size() == 1,
                    "Path {} matched multiple modules: {}", path, matchingModules);
            return Optional.of(matchingModules.iterator().next());
        }
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

    /** Load and register a new module for this file. */
    @SuppressWarnings("UnusedReturnValue")
    public Module registerArchiveModule(Path path) throws IOException {
        Module module = moduleFactory.createArchiveModule(path.toFile());
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
            newEnvironment = new ModuleEnvironment(new DefaultBeanContext(), finalModules, wrappingPermissionProviderFactory);
        } else {
            newEnvironment = new ModuleEnvironment(new DefaultBeanContext(), finalModules, permissionProviderFactory);
        }
        if (asPrimary) {
            environment = newEnvironment;
        }
        return newEnvironment;
    }

    @Deprecated/*(since="4.4.0")*/
    public ModuleMetadataJsonAdapter getModuleMetadataReader() {
        // FIXME: This has one use in MTE. Give MTE a better interface to use and remove
        //     the accessor for this.
        return metadataReader;
    }

    public ModuleFactory getModuleFactory() {
        return moduleFactory;
    }

    public PermissionProvider getPermissionProvider(Module module) {
        // TODO: This has only one usage. Audit to see if it's worth keeping.
        return permissionProviderFactory.createPermissionProviderFor(
                module,
                x -> false  // modules (other than engine) never contain classpath classes
        );
    }

    /** Convert URL to File, with support for {@code jar} URLs. */
    private static File urlToFile(URL url) {
        URL fileUrl = url;
        if (url.getProtocol().equals("jar")) {
            try {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                fileUrl = connection.getJarFileURL();
                // despite the method name, openConnection doesn't open anything unless we
                // call connect(), so we needn't clean up anything here.
            } catch (IOException e) {
                throw new RuntimeException("Failed to get file from " + url);
            }
        }
        try {
            return new File(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to get file from " + url, e);
        }
    }
}

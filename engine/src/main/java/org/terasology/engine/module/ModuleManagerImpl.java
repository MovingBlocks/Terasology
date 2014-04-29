/*
 * Copyright 2013 MovingBlocks
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

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetSource;
import org.terasology.asset.sources.ArchiveSource;
import org.terasology.asset.sources.DirectorySource;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.FilesUtil;
import org.terasology.utilities.gson.VersionTypeAdapter;
import org.terasology.version.TerasologyVersion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This manager handles the available modules, which ones are active and access to their assets and code
 *
 * @author Immortius
 */
public class ModuleManagerImpl implements ModuleManager {

    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);
    private final ModuleSecurityManager moduleSecurityManager;

    private Map<String, Module> activeModules = Maps.newLinkedHashMap();

    private Table<String, Version, ExtensionModule> modules = HashBasedTable.create();
    private URLClassLoader activeModuleClassLoader;
    private URLClassLoader allModuleClassLoader;
    private Module engineModule;

    private Reflections allReflections;
    private Reflections engineReflections;
    private Reflections activeModuleReflections;

    private ClassLoader[] engineClassLoaders;

    private boolean cacheReflections;

    public ModuleManagerImpl(ModuleSecurityManager moduleSecurityManager, boolean cacheReflections) {
        this(moduleSecurityManager, cacheReflections, ModuleManagerImpl.class.getClassLoader());
    }

    private ModuleManagerImpl(ModuleSecurityManager moduleSecurityManager, boolean cacheReflections, ClassLoader... engineClassLoaders) {
        this.engineClassLoaders = Arrays.copyOf(engineClassLoaders, engineClassLoaders.length);
        this.moduleSecurityManager = moduleSecurityManager;
        this.cacheReflections = cacheReflections;

        String filename = "engine-reflections";

        if (cacheReflections) {
            engineReflections = tryLoadReflectionsFromCacheFile(filename);
        }

        if (engineReflections == null) {
            ConfigurationBuilder builder = new ConfigurationBuilder()
                    .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());

            for (ClassLoader loader : engineClassLoaders) {
                builder.addClassLoader(loader)
                        .addUrls(ClasspathHelper.forPackage("org.terasology", loader));
            }
            engineReflections = new Reflections(builder);

            if (cacheReflections) {
                saveReflectionsToCacheFile(engineReflections, filename);
            }
        }

        String modulePath = "/" + TerasologyConstants.ASSETS_SUBDIRECTORY + "/" + "module.txt";
        try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(modulePath), Charsets.UTF_8)) {
            engineModule = new EngineModule(engineReflections, new Gson().fromJson(reader, ModuleInfo.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load engine module info", e);
        }
        refresh();
    }

    private Reflections tryLoadReflectionsFromCacheFile(String filename) {
        String version = TerasologyVersion.getInstance().getGitCommit();
        Path root = PathManager.getInstance().getHomePath().resolve("cache");
        Path path = root.resolve(filename + version + ".xml");

        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            logger.info("Reading reflection content from file {}", filename);
            Stopwatch sw = Stopwatch.createStarted();
            try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
                engineReflections = new Reflections(new ConfigurationBuilder());
                engineReflections.collect(is);
                logger.info("Reflections read in {}ms.", sw.elapsed(TimeUnit.MILLISECONDS));
                return engineReflections;
            } catch (IOException e) {
                logger.warn("Failed to read from cache");
            }
        }

        return null;
    }

    private void saveReflectionsToCacheFile(Reflections reflections, String filename) {
        String version = TerasologyVersion.getInstance().getGitCommit();
        Path root = PathManager.getInstance().getHomePath().resolve("cache");
        Path path = root.resolve(filename + version + ".xml");

        try {
            Files.createDirectories(root);

            logger.info("Reading reflection content from file {}", path);
            Stopwatch sw = Stopwatch.createStarted();
            reflections.save(path.toString());
            logger.info("Reflections read in {}ms.", sw.elapsed(TimeUnit.MILLISECONDS));
        } catch (IOException e) {
            logger.warn("Could not create folder " + root, e);
        }
    }

    private Reflections getEngineReflections() {
        return engineReflections;
    }

    @Override
    public Reflections getActiveModuleReflections() {
        if (activeModuleReflections != null) {
            return activeModuleReflections;
        }
        return engineReflections;
    }

    @Override
    public void disableAllModules() {
        for (Module module : activeModules.values()) {
            if (module instanceof ExtensionModule) {
                CoreRegistry.get(AssetManager.class).removeAssetSource(module.getModuleSource());
            }
        }
        activeModules.clear();
        activeModules.put(engineModule.getId(), engineModule);
    }

    @Override
    public void enableModule(Module module) {
        Module oldModule = activeModules.put(module.getId(), module);
        if (!module.equals(oldModule)) {
            if (oldModule != null && oldModule instanceof ExtensionModule) {
                CoreRegistry.get(AssetManager.class).removeAssetSource(oldModule.getModuleSource());
            }
            if (module instanceof ExtensionModule) {
                CoreRegistry.get(AssetManager.class).addAssetSource(module.getModuleSource());
            }
        }
    }

    public void enableModuleAndDependencies(Module module) {
        for (DependencyInfo info : module.getModuleInfo().getDependencies()) {
            Module dependency = getLatestModuleVersion(info.getId());
            if (info.getMaxVersion().compareTo(dependency.getVersion()) <= 0) {
                enableModuleAndDependencies(dependency);
            }
        }
        enableModule(module);
    }

    @Override
    public void disableModule(Module module) {
        Module removedModule = activeModules.remove(module.getId());
        if (removedModule != null && removedModule instanceof ExtensionModule) {
            CoreRegistry.get(AssetManager.class).removeAssetSource(module.getModuleSource());
        }
    }

    /**
     * Provides the ability to reflect over the engine and all modules, not just active modules.  This should be used sparingly,
     * and classes retrieved from it should not be instantiated and used - this uses a different classloader than the
     * rest of the system.
     *
     * @return Reflections over the engine and all available modules
     */
    @Override
    public Reflections loadInactiveReflections() {
        if (allReflections == null) {
            String filename = "all-reflections";

            if (cacheReflections) {
                allReflections = tryLoadReflectionsFromCacheFile(filename);
            }

            if (allReflections == null) {
                List<URL> urls = Lists.newArrayList();
                for (ExtensionModule module : getExtensionModules()) {
                    if (module.isCodeModule()) {
                        urls.add(module.getModuleClasspathUrl());
                    }
                }

                ConfigurationBuilder builder = new ConfigurationBuilder()
                        .addUrls(urls)
                        .addClassLoader(allModuleClassLoader);
                for (ClassLoader engineLoader : engineClassLoaders) {
                    builder.addClassLoader(engineLoader)
                            .addUrls(ClasspathHelper.forPackage("org.terasology", engineLoader));
                }
                allReflections = new Reflections(builder);
                allReflections.merge(getEngineReflections());
                for (Module module : getModules()) {
                    if (module.isCodeModule()) {
                        allReflections.merge(module.getReflections());
                    }
                }

                if (cacheReflections) {
                    saveReflectionsToCacheFile(allReflections, filename);
                }
            }
        }
        return allReflections;
    }

    /**
     * Rescans for modules.  This should not be done while a game is running, as it drops the module classloader.
     */
    @Override
    public void refresh() {
        modules.clear();
        activeModules.clear();
        activeModules.put(engineModule.getId(), engineModule);
        Gson gson = new GsonBuilder().registerTypeAdapter(Version.class, new VersionTypeAdapter()).create();
        for (Path rootModulePath : PathManager.getInstance().getModulePaths()) {

            // Directories first (they should override zips)
            try {
                for (Path modulePath : Files.newDirectoryStream(rootModulePath, FilesUtil.DIRECTORY_FILTER)) {
                    processModDirectory(modulePath, gson);
                }
            } catch (IOException e) {
                logger.error("Failed to scan for directory modules", e);
            }

            // Zip files next
            try {
                for (Path modulePath : Files.newDirectoryStream(rootModulePath, new DirectoryStream.Filter<Path>() {
                    @Override
                    public boolean accept(Path entry) throws IOException {
                        return Files.isRegularFile(entry) && (entry.toString().endsWith(".jar") || entry.toString().endsWith(".zip"));
                    }
                })) {
                    processModArchive(modulePath, gson);
                }
            } catch (IOException e) {
                logger.error("Failed to scan for jar and zip modules", e);
            }
        }
        final List<URL> urls = Lists.newArrayList();
        for (ExtensionModule module : getExtensionModules()) {
            if (module.isCodeModule()) {
                urls.add(module.getModuleClasspathUrl());
            }
        }
        if (allModuleClassLoader != null) {
            try {
                allModuleClassLoader.close();
            } catch (IOException e) {
                logger.error("Failed to cloase allModuleClassLoader", e);
            }
        }

        allModuleClassLoader = AccessController.doPrivileged(new PrivilegedAction<ModuleClassLoader>() {
            @Override
            public ModuleClassLoader run() {
                return new ModuleClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader(), moduleSecurityManager);
            }
        });

        for (ExtensionModule module : getExtensionModules()) {
            module.setInactiveClassLoader(allModuleClassLoader);
        }

        if (activeModuleClassLoader != null) {
            try {
                activeModuleClassLoader.close();
            } catch (IOException e) {
                logger.error("Failed to close activeModuleClassLoader", e);
            }
        }
        allReflections = null;
    }

    private void processModArchive(Path modulePath, Gson gson) {
        try (ZipFile zipFile = new ZipFile(modulePath.toFile())) {
            ZipEntry modInfoEntry = zipFile.getEntry("module.txt");
            if (modInfoEntry != null) {
                try {
                    ModuleInfo moduleInfo = gson.fromJson(new InputStreamReader(zipFile.getInputStream(modInfoEntry), Charsets.UTF_8), ModuleInfo.class);
                    AssetSource source = new ArchiveSource(moduleInfo.getId(), modulePath.toFile(),
                            TerasologyConstants.ASSETS_SUBDIRECTORY, TerasologyConstants.OVERRIDES_SUBDIRECTORY, TerasologyConstants.DELTAS_SUBDIRECTORY);
                    processModuleInfo(moduleInfo, modulePath, source);
                } catch (FileNotFoundException | JsonIOException e) {
                    logger.warn("Failed to load module manifest for module at {}", modulePath, e);
                }
            }
        } catch (IOException e) {
            logger.error("Invalid module file: {}", modulePath, e);
        }
    }

    private void processModDirectory(Path modulePath, Gson gson) throws IOException {
        Path modInfoFile = modulePath.resolve("module.txt");
        if (Files.isRegularFile(modInfoFile)) {
            try (Reader reader = Files.newBufferedReader(modInfoFile, TerasologyConstants.CHARSET)) {
                ModuleInfo moduleInfo = gson.fromJson(reader, ModuleInfo.class);
                Path assetLocation = modulePath.resolve(TerasologyConstants.ASSETS_SUBDIRECTORY);
                Path overridesLocation = modulePath.resolve(TerasologyConstants.OVERRIDES_SUBDIRECTORY);
                Path deltasLocation = modulePath.resolve(TerasologyConstants.DELTAS_SUBDIRECTORY);
                AssetSource source = new DirectorySource(moduleInfo.getId(), assetLocation, overridesLocation, deltasLocation);
                processModuleInfo(moduleInfo, modulePath, source);
            } catch (FileNotFoundException | JsonIOException e) {
                logger.warn("Failed to load module manifest for module at {}", modulePath, e);
            }
        }
    }

    private void processModuleInfo(ModuleInfo moduleInfo, Path modulePath, AssetSource source) {
        String moduleId = UriUtil.normalise(moduleInfo.getId());
        Version version = Version.create(moduleInfo.getVersion());
        if (version != null) {
            if (!modules.contains(moduleId, version)) {
                ExtensionModule module = new ExtensionModule(modulePath, moduleInfo, version, source);
                modules.put(moduleId, version, module);
                logger.info("Discovered module: {}:{} (hasCode = {})", moduleInfo.getDisplayName(), moduleInfo.getVersion(), module.isCodeModule());
            } else {
                logger.info("Discovered duplicate module: {}:{}, skipping", moduleInfo.getDisplayName(), moduleInfo.getVersion());
            }
        } else {
            logger.error("Found module '" + moduleInfo.getId() + "' with invalid version '" + moduleInfo.getVersion() + "', skipping");
        }
    }

    @Override
    public void applyActiveModules() {
        List<ExtensionModule> activeCodeMods = getActiveExtensionCodeModules();
        final List<URL> urls = Lists.newArrayList();
        for (ExtensionModule module : activeCodeMods) {
            urls.add(module.getModuleClasspathUrl());
        }
        if (activeModuleClassLoader != null) {
            try {
                activeModuleClassLoader.close();
            } catch (IOException e) {
                logger.error("Failed to close activeModuleClassLoader", e);
            }
        }
        activeModuleClassLoader = AccessController.doPrivileged(new PrivilegedAction<ModuleClassLoader>() {
            @Override
            public ModuleClassLoader run() {
                return new ModuleClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader(), moduleSecurityManager);
            }
        });
        for (Module module : activeModules.values()) {
            if (module instanceof ExtensionModule) {
                ((ExtensionModule) module).setActiveClassLoader(activeModuleClassLoader);
            }
        }
        // We don't submit any urls as we don't want to scan (going to merge in from previous scans)
        activeModuleReflections = new Reflections(new ConfigurationBuilder().addClassLoader(getClass().getClassLoader()).addClassLoader(activeModuleClassLoader));
        activeModuleReflections.merge(getEngineReflections());
        for (Module module : activeCodeMods) {
            activeModuleReflections.merge(module.getReflections());
        }
    }

    @Override
    public List<String> getModuleIds() {
        return ImmutableList.<String>builder().add(TerasologyConstants.ENGINE_MODULE).addAll(modules.rowKeySet()).build();
    }

    @Override
    public List<Module> getModules() {
        return ImmutableList.<Module>builder().add(engineModule).addAll(modules.values()).build();
    }

    @Override
    public List<Module> getCodeModules() {
        List<Module> result = Lists.newArrayList();
        result.add(engineModule);
        for (Module module : modules.values()) {
            if (module.isCodeModule()) {
                result.add(module);
            }
        }
        return result;
    }

    private Collection<ExtensionModule> getExtensionModules() {
        return modules.values();
    }

    @Override
    public Module getActiveModule(String id) {
        String normalisedName = UriUtil.normalise(id);
        Module module = activeModules.get(normalisedName);
        if (null == module) {
            logger.error("Reference to missing active module id: {}", id);
        }
        return module;
    }

    @Override
    public Iterable<Module> getActiveModules() {
        return ImmutableSet.copyOf(activeModules.values());
    }

    @Override
    public Module getLatestModuleVersion(String id) {
        if (TerasologyConstants.ENGINE_MODULE.equals(id)) {
            return engineModule;
        }
        Module result = null;
        for (Module module : modules.row(UriUtil.normalise(id)).values()) {
            if (result == null || module.getVersion().compareTo(result.getVersion()) > 0) {
                result = module;
            }
        }
        return result;
    }

    @Override
    public Module getLatestModuleVersion(String id, Version minVersion, Version maxVersion) {
        if (TerasologyConstants.ENGINE_MODULE.equals(id)) {
            return engineModule;
        }
        Module latestInBounds = null;
        for (Module module : modules.row(UriUtil.normalise(id)).values()) {
            if (module.getVersion().compareTo(minVersion) >= 0 && module.getVersion().compareTo(maxVersion) < 0
                    && (latestInBounds == null || latestInBounds.getVersion().compareTo(module.getVersion()) > 0)) {
                latestInBounds = module;
            }
        }
        return latestInBounds;
    }

    @Override
    public Iterable<Module> getActiveCodeModules() {
        List<Module> result = Lists.newArrayList();
        for (Module module : activeModules.values()) {
            if (module.isCodeModule()) {
                result.add(module);
            }
        }
        return result;
    }

    @Override
    public List<Module> getActiveModulesOrderedByDependency() {
        List<Module> result = Lists.newArrayList();
        result.add(getActiveModule("engine"));
        for (Module module : getActiveModules()) {
            addModuleAfterDependencies(module, result);
        }
        return result;
    }

    private void addModuleAfterDependencies(Module module, List<Module> result) {
        if (!result.contains(module)) {
            for (DependencyInfo dependency : module.getModuleInfo().getDependencies()) {
                Module dependencyModule = getActiveModule(dependency.getId());
                if (dependencyModule != null) {
                    addModuleAfterDependencies(dependencyModule, result);
                } else {
                    throw new IllegalStateException("Active modules missing dependency '" + dependency.getId() + "'");
                }
            }
            result.add(module);
        }
    }

    private List<ExtensionModule> getActiveExtensionCodeModules() {
        List<ExtensionModule> result = Lists.newArrayListWithCapacity(modules.size() + 1);
        for (Module module : activeModules.values()) {
            if (module.isCodeModule() && module instanceof ExtensionModule) {
                result.add((ExtensionModule) module);
            }
        }
        return result;
    }

    @Override
    public boolean isEnabled(Module module) {
        return activeModules.containsKey(module.getId());
    }

    @Override
    public Iterable<Module> getAllDependencies(Module module) {
        Set<Module> dependencies = Sets.newHashSet();
        addDependenciesRecursive(module, dependencies);
        dependencies.add(engineModule);
        return dependencies;
    }

    private void addDependenciesRecursive(Module module, Set<Module> dependencies) {
        for (DependencyInfo dependencyInfo : module.getModuleInfo().getDependencies()) {
            Module dependency = getLatestModuleVersion(dependencyInfo.getId());
            if (dependency != null) {
                dependencies.add(module);
                addDependenciesRecursive(dependency, dependencies);
            }
        }
    }

    @Override
    public Set<String> getDependencyNamesOf(Module context) {
        Set<String> dependencies = Sets.newHashSet();
        addDependencyNamesRecursive(context, dependencies);
        dependencies.add(engineModule.getId());
        return dependencies;
    }

    private void addDependencyNamesRecursive(Module module, Set<String> dependencies) {
        for (DependencyInfo dependencyInfo : module.getModuleInfo().getDependencies()) {
            Module dependency = getLatestModuleVersion(dependencyInfo.getId());
            if (dependency != null) {
                dependencies.add(module.getId());
                addDependencyNamesRecursive(dependency, dependencies);
            }
        }
    }

    @Override
    public Module getModule(String moduleId, Version version) {
        if (UriUtil.normalise(moduleId).equals(TerasologyConstants.ENGINE_MODULE)) {
            return engineModule;
        }
        ExtensionModule extensionModule = modules.get(moduleId, version);
        if (null == extensionModule) {
            logger.error("Reference to missing module id: {}", moduleId);
        }
        return extensionModule;
    }

    @Override
    public <T> ListMultimap<String, Class<? extends T>> findAllSubclassesOf(Class<? extends T> type) {
        ListMultimap<String, Class<? extends T>> result = ArrayListMultimap.create();
        List<Class<? extends T>> validSuperTypes = Lists.newArrayList();
        validSuperTypes.add(type);

        for (Module module : getActiveModulesOrderedByDependency()) {
            if (!module.isCodeModule()) {
                continue;
            }
            List<Class<? extends T>> moduleTypes = Lists.newArrayList();
            for (Class<? extends T> superType : validSuperTypes) {
                for (Class<? extends T> subtype : module.getReflections().getSubTypesOf(superType)) {
                    moduleTypes.add(subtype);
                }
            }
            result.putAll(module.getId(), moduleTypes);
            for (Class<? extends T> moduleType : moduleTypes) {
                if (!Modifier.isFinal(moduleType.getModifiers())) {
                    validSuperTypes.add(moduleType);
                }
            }
        }

        return result;
    }
}

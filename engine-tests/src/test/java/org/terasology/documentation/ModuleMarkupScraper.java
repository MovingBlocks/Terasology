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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.codehaus.plexus.util.StringUtils;
import org.terasology.HeadlessEnvironment;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.ExtraDataModuleExtension;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.RemoteModuleExtension;
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.entitySystem.event.Event;
import org.terasology.i18n.I18nMap;
import org.terasology.module.*;
import org.terasology.naming.Name;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.terasology.engine.module.StandardModuleExtension.IS_ASSET;
import static org.terasology.engine.module.StandardModuleExtension.IS_WORLD;

public final class ModuleMarkupScraper {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_GITHUB_MODULE_URL = "https://github.com/Terasology/";
    private static final List INTERNAL_MODULES = Arrays.asList("Core", "engine", "CoreSampleGameplay", "BuilderSampleGameplay", "BiomesAPI");
    private static final Comparator<? super Module> moduleInfoComparator = Comparator.comparing(o -> o.getMetadata()
            .getDisplayName().toString());

    protected static Context context;

    private static ModuleManager moduleManager;

    private static HeadlessEnvironment env;

    private ModuleMarkupScraper() throws IOException {
        super();
    }

    /**
     * @param args (ignored)
     * @throws Exception if the module environment cannot be loaded
     */
    public static void main(String[] args) throws Exception {
        env = new HEnv(new Name("engine"));
        context = env.getContext();
        moduleManager = context.get(ModuleManager.class);

        // Request Remote Modules List...
        Future<Void> remoteModuleRegistryUpdater = Executors.newSingleThreadExecutor()
                .submit(moduleManager.getInstallManager().updateRemoteRegistry());
        remoteModuleRegistryUpdater.get(); // wait until remoteRegistry downloads

        List<Module> allModules = Lists.newArrayList();

        List<Module> gameModules = Lists.newArrayList();
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module latestVersion = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (!latestVersion.isOnClasspath()) {
                gameModules.add(latestVersion);
                System.out.println("Found Game Module: " + latestVersion.getId());
            }
        }

        List<Module> remoteModules = Lists.newArrayList();
        Iterable<Module> remoteModuleRegistry = moduleManager.getInstallManager().getRemoteRegistry();
        Set<Name> filtered = ImmutableSet.of(TerasologyConstants.ENGINE_MODULE, new Name("engine-test"));
        for (Module remote : remoteModuleRegistry) {
            remoteModules.add(remote);
            System.out.println("Found Remote Module: " + remote.getId());
        }
        allModules.addAll(gameModules);
        allModules.addAll(remoteModules);
        allModules.sort(moduleInfoComparator);

        List<Module> allSortedModules;
        allSortedModules = new ArrayList<>(allModules);

        StringBuilder wiki = new StringBuilder();

        StringBuilder moduleMapListing = new StringBuilder();

        List<Module> sortedGameModules = new ArrayList<>(gameModules);
        sortedGameModules.sort(moduleInfoComparator);
        List<Module> sortedRemoteModules = new ArrayList<>(remoteModules);
        sortedRemoteModules.sort(moduleInfoComparator);
        moduleMapListing.append("| ");
        for (Module gModule : sortedGameModules) {
            // | [AdditionalFruits](#AdditionalFruits)
            moduleMapListing.append("[").append(gModule.getId()).append("](#").append(gModule.getId()).append(") ");
        }
        moduleMapListing.append(" | ");
        for (Module rModule : sortedRemoteModules) {
            moduleMapListing.append("[").append(rModule.getId()).append("](#").append(rModule.getId()).append(") ");
        }
        moduleMapListing.append(" | ");

        StringBuilder moduleMap = new StringBuilder();
        moduleMap.append(System.getProperty("line.separator"));
        moduleMap.append("**Extensions:**");
        moduleMap.append(System.getProperty("line.separator"));
        moduleMap.append("| Installed | Remote |");
        moduleMap.append(System.getProperty("line.separator"));
        moduleMap.append("| -------- | ---------- |");
        moduleMap.append(System.getProperty("line.separator"));
        moduleMap.append(moduleMapListing);

        wiki.append(moduleMap);

        wiki.append(ScanModules(allSortedModules));
        System.out.println(wiki);

        String fileName = "Module_Wiki.md";
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));
        writer.write(wiki.toString());
        writer.flush();
        writer.close();
        System.out.println("Module Wiki generation file is ready!");

        env.close();
    }

    private static String ScanModules(List<Module> allSortedModules) {
        if (allSortedModules == null) {
            throw new IllegalArgumentException("ModuleMarkupScraper:ScanModules() - Module must be valid.");
        }

        if (allSortedModules.isEmpty()) {
            throw new IllegalStateException("ModuleMarkupScraper:ScanModules() - No modules found? Somethings very wrong.");
        }

        StringBuilder out = new StringBuilder();
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Module module : allSortedModules) {
            Name moduleId = module.getId();
            if (module.isCodeModule()) {
                out.append("## Module: ").append(moduleId);

                String moduleDescription = getModuleDescription(module);
                out.append(moduleDescription);

                Set<Module> modules = new HashSet<>();
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    modules = result.getModules();
                } else {
                    modules.add(module);
                }

                out.append(ExportEvents(moduleId, modules));
            }
            out.append(System.getProperty("line.separator"));
        }
        return out.toString();
    }

    private static String ExportEvents(Name moduleId, Set<Module> modules) {
        if (modules == null) {
            throw new IllegalArgumentException("ModuleMarkupScraper:ExportEvents() - Module must be valid.");
        }

        StringBuilder events = new StringBuilder();
        try (ModuleEnvironment environment2 = moduleManager.loadEnvironment(modules, false)) {

            for (Class<? extends Event> type : environment2.getSubtypesOf(Event.class)) {
                Name mod = environment2.getModuleProviding(type);

                if (!mod.toString().equals(moduleId.toString()))
                    continue;

                events.append("  ").append(type.getName());
                events.append(System.getProperty("line.separator"));

                // Are there any other classes derived from this class?
                for (Class<? extends Object> eventType : environment2.getSubtypesOf(type)) {
                    events.append("    ").append(eventType.getName());
                    events.append(System.getProperty("line.separator"));
                }
            }
        }
        StringBuilder out = new StringBuilder();
        if (events.length() > 0) {
            out.append("**Module Events:** ");
            out.append(System.getProperty("line.separator"));
            out.append(events);
            out.append(System.getProperty("line.separator"));
        } else {
            out.append("**Module Events:** None");
            out.append(System.getProperty("line.separator"));
        }
        return out.toString();
    }

    private static String getModuleDescription(final Module module) {
        if (module == null) {
            throw new IllegalArgumentException("ModuleMarkupScraper:getModuleDescription() - Module must be valid.");
        }

        if (module.getMetadata() == null) {
            throw new IllegalStateException("ModuleMarkupScraper:getModuleDescription() - ModuleMetadata must be valid.");
        }

        final ModuleMetadata metadata = module.getMetadata();

        StringBuilder details = new StringBuilder();
        details.append(System.getProperty("line.separator"));
        details.append("# ").append(metadata.getId());
        details.append(System.getProperty("line.separator"));
        details.append("- **Display Name:** ").append(metadata.getDisplayName());
        details.append(System.getProperty("line.separator"));
        details.append("- **Version:** ").append(metadata.getVersion());
        details.append(System.getProperty("line.separator"));

        I18nMap description = metadata.getDescription();
        if(description != null && !description.value().isEmpty()) {
            details.append("- **Description:** ").append(description);
            details.append(System.getProperty("line.separator"));
        }

        String permissions = String.join(", ", metadata.getRequiredPermissions());
        if(permissions != null && !permissions.isEmpty()) {
            details.append("- **Permissions:** ").append(permissions);
            details.append(System.getProperty("line.separator"));
        }

        details.append("- **Github:** ").append(getOriginModuleUrl(module));
        details.append(System.getProperty("line.separator"));

//        details.append("- **Online Version:** ").append(getOnlineVersion(moduleManager, module.getMetadata().getDependencyInfo(module.getId())));
//        details.append(System.getProperty("line.separator"));

        String author = ExtraDataModuleExtension.getAuthor(module);
        if(!author.isEmpty()) {
            details.append("- **Author:** ").append(author);
            details.append(System.getProperty("line.separator"));
        }

        String categories = getModuleTags(module);
        if(categories != null && !categories.isEmpty()) {
            details.append("- **Categories:** ").append(categories);
            details.append(System.getProperty("line.separator"));
        }

        details.append(getModuleExtensions(module));

        details.append(getModuleDependencies(metadata));

        details.append(System.getProperty("line.separator"));
        return details.toString();
    }

    private static String getModuleDependencies(ModuleMetadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("ModuleMarkupScraper:getModuleDependencies() - ModuleMetadata must be valid.");
        }

        StringBuilder out = new StringBuilder();
        final List<DependencyInfo> dependencies = metadata.getDependencies();
        StringBuilder dependenciesNames = new StringBuilder();
        if (dependencies != null && !dependencies.isEmpty()) {
            for (DependencyInfo dependency : dependencies) {

                if (dependenciesNames.length() > 0) {
                    dependenciesNames.append(", ");
                }

                dependenciesNames
                        .append(System.getProperty("line.separator"))
                        .append("|  _").append(dependency.getId()).append("_ |")
                        .append(dependency.getMinVersion() == null ? "" : "  _").append(dependency.getMinVersion()).append("_ |")
                        .append(dependency.getMaxVersion() == null ? "" : "  _").append(dependency.getMaxVersion()).append("_ |")
                        .append(dependency.getMaxVersion() == null ? "" : "  _").append(!dependency.isOptional()).append("_ |");
            }

            out.append(System.getProperty("line.separator"));
            out.append("**Dependencies:** ");
            out.append(System.getProperty("line.separator"));
            out.append("| Module | MinVersion | MaxVersion | Required |");
            out.append(System.getProperty("line.separator"));
            out.append("| -------- | ---------- | ---------- | ---------- |");
            out.append(dependenciesNames);
        } else {
            out.append(System.getProperty("line.separator"));
            out.append("**Dependencies:** _None_");
        }
        return out.toString();
    }

    private static String getModuleExtensions(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("ModuleMarkupScraper:getModuleExtensions() - Module must be valid.");
        }
        ModuleMetadata metadata = module.getMetadata();
        StringBuilder extensions = new StringBuilder();
        URL downloadUri = RemoteModuleExtension.getDownloadUrl(metadata);
        if (downloadUri != null) {
            extensions.append("|  Download Uri: | _").append(downloadUri).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Date lastUpdate = RemoteModuleExtension.getLastUpdated(metadata);
        if (lastUpdate != null) {
            extensions.append("|  Last Update: | _").append(lastUpdate).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Long size = RemoteModuleExtension.getArtifactSize(metadata);
        if (size != null) {
            extensions.append("|  Artifact Size: | _").append(size).append(" bytes").append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
//        String author = ExtraDataModuleExtension.getAuthor(module);
//        if (author != null && StringUtils.isNotEmpty(author)) {
//            extensions.append("|  Author: | _").append(author).append("_ |");
//            extensions.append(System.getProperty("line.separator"));
//        }
        String origin = ExtraDataModuleExtension.getOrigin(module);
        if (origin != null && StringUtils.isNotEmpty(origin)) {
            extensions.append("|  Origin: | _").append(origin).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean serverSideOnly = StandardModuleExtension.isServerSideOnly(module);
        if (serverSideOnly != null) {
            extensions.append("|  serverSideOnly: | _").append(serverSideOnly).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean gameplayModule = StandardModuleExtension.isGameplayModule(module);
        if (gameplayModule != null) {
            extensions.append("|  gameplayModule: | _").append(gameplayModule).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean isAsset = (Boolean) metadata.getExtension(IS_ASSET.getKey(), Boolean.class);
        if (isAsset != null) {
            extensions.append("|  IS_ASSET: | _").append(isAsset).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean isWorld = (Boolean) metadata.getExtension(IS_WORLD.getKey(), Boolean.class);
        if (isAsset != null) {
            extensions.append("|  IS_WORLD: | _").append(isWorld).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean isLibrary = (Boolean) metadata.getExtension(StandardModuleExtension.IS_LIBRARY.getKey(), Boolean.class);
        if (isAsset != null) {
            extensions.append("|  IS_LIBRARY: | _").append(isLibrary).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean isSpecial = (Boolean) metadata.getExtension(StandardModuleExtension.IS_SPECIAL.getKey(), Boolean.class);
        if (isAsset != null) {
            extensions.append("|  IS_SPECIAL: | _").append(isSpecial).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean isAug = (Boolean) metadata.getExtension(StandardModuleExtension.IS_AUGMENTATION.getKey(), Boolean.class);
        if (isAsset != null) {
            extensions.append("|  IS_AUGMENTATION: | _").append(isAug).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        SimpleUri defaultWorldGenerator = StandardModuleExtension.getDefaultWorldGenerator(module);
        if (defaultWorldGenerator != null && defaultWorldGenerator.isValid()) {
            extensions.append("|  Default World Generator: | _").append(defaultWorldGenerator).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }

        StringBuilder out = new StringBuilder();
        out.append(System.getProperty("line.separator"));
        if (extensions.length() > 0) {
            out.append(System.getProperty("line.separator"));
            out.append("**Extensions:**");
            out.append(System.getProperty("line.separator"));
            out.append("| Extension | Value |");
            out.append(System.getProperty("line.separator"));
            out.append("| -------- | ---------- |");
            out.append(System.getProperty("line.separator"));
            out.append(extensions);
        } else {
            out.append(System.getProperty("line.separator"));
            out.append("**Extensions:** _None_");
        }
        return out.toString();
    }

    private static String getOriginModuleUrl(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("ModuleMarkupScraper:getOriginModuleUrl() - Module must be valid.");
        }
        final String origin = ExtraDataModuleExtension.getOrigin(module);
        if (StringUtils.isBlank(origin) && !INTERNAL_MODULES.contains(module.getId().toString())) {
            return DEFAULT_GITHUB_MODULE_URL + module.getId();
        }
        return origin;
    }

//    private static String getOnlineVersion(final ModuleManager moduleManager, final DependencyInfo dependencyInfo) {
//        if (moduleManager == null) {
//            throw new IllegalArgumentException("ModuleMarkupScraper:getOnlineVersion() - ModuleManager must be valid.");
//        }
//        if (dependencyInfo == null) {
//            throw new IllegalArgumentException("ModuleMarkupScraper:getOnlineVersion() - DependencyInfo must be valid.");
//        }
//        return moduleManager.getInstallManager().getRemoteRegistry().stream()
//                .filter(module -> module.getId().equals(dependencyInfo.getId()))
//                .findFirst()
//                .map(Module::getVersion)
//                .map(String::valueOf)
//                .orElse("");
//    }

    private static String getModuleTags(final Module module) {
        if (module == null) {
            throw new IllegalArgumentException("ModuleMarkupScraper:getModuleTags() - Module must be valid.");
        }
        return StandardModuleExtension.booleanPropertySet().stream()
                .filter(ext -> ext.isProvidedBy(module))
                .map(StandardModuleExtension::getKey)
                .collect(Collectors.joining(", "));
    }
}


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

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
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
import org.terasology.module.DependencyInfo;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.terasology.engine.module.StandardModuleExtension.IS_ASSET;
import static org.terasology.engine.module.StandardModuleExtension.IS_WORLD;

public final class ModuleMarkupScraper {

    private static final String DEFAULT_GITHUB_MODULE_URL = "https://github.com/Terasology/";
    private static final List INTERNAL_MODULES = Arrays.asList("Core", "engine", "CoreSampleGameplay", "BuilderSampleGameplay", "BiomesAPI");
    private static final Comparator<? super Module> MODULE_INFO_COMPARATOR  = Comparator.comparing(o -> o.getMetadata()
            .getDisplayName().toString());

    /**
     * @param args (ignored)
     * @throws Exception if the module environment cannot be loaded
     */
    public static void main(String[] args) throws Exception {
         HeadlessEnvironment env = new ModuleMetaDataProvidingHeadlessEnvironment(new Name("engine"));
         Context context = env.getContext();
         ModuleManager moduleManager = context.get(ModuleManager.class);

        // Request Remote Modules List...
        Future<Void> remoteModuleRegistryUpdater = Executors.newSingleThreadExecutor()
                .submit(moduleManager.getInstallManager().updateRemoteRegistry());
        remoteModuleRegistryUpdater.get(); // wait until remoteRegistry downloads

        List<Module> sortedGameModules = getListOfGameModules(moduleManager);
        List<Module> sortedRemoteModules = getListofRemoteModules(moduleManager);

        String wiki = generateWiki(moduleManager, sortedGameModules, sortedRemoteModules);

        String fileName = "Module_Wiki.md";
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));
        writer.write(wiki);
        writer.flush();
        writer.close();
        System.out.println("Module Wiki generation file is ready!");

        env.close();
    }

    private static String generateWiki(ModuleManager moduleManager, List<Module> sortedGameModules, List<Module> sortedRemoteModules) {
        StringBuilder wiki = new StringBuilder();

        List<Module> allModules = Lists.newArrayList();
        allModules.addAll(sortedGameModules);
        allModules.addAll(sortedRemoteModules);
        allModules.sort(MODULE_INFO_COMPARATOR);

        wiki.append(generateModuleTableOfContents(sortedGameModules, sortedRemoteModules));

        wiki.append(scanModules(moduleManager, allModules));

        System.out.println(wiki);

        return wiki.toString();
    }

    private static String generateModuleTableOfContents(List<Module> sortedGameModules, List<Module> sortedRemoteModules) {
        StringBuilder moduleMapListing = new StringBuilder();

        moduleMapListing.append("| ");
        for (Module gModule : sortedGameModules) {
            // Add anchor tags, for example [AdditionalFruits](#AdditionalFruits)
            moduleMapListing.append("[").append(gModule.getId()).append("](#").append(gModule.getId()).append(") ");
        }
        moduleMapListing.append(" | ");
        for (Module rModule : sortedRemoteModules) {
            moduleMapListing.append("[").append(rModule.getId()).append("](#").append(rModule.getId()).append(") ");
        }
        moduleMapListing.append(" | ");

        String moduleMap = System.getProperty("line.separator") +
                "**Extensions:**" +
                System.getProperty("line.separator") +
                "| Installed | Remote |" +
                System.getProperty("line.separator") +
                "| -------- | ---------- |" +
                System.getProperty("line.separator") +
                moduleMapListing;

        return moduleMap;
    }

    private static List<Module> getListofRemoteModules(ModuleManager moduleManager) {
        List<Module> remoteModules = Lists.newArrayList();
        Iterable<Module> remoteModuleRegistry = moduleManager.getInstallManager().getRemoteRegistry();
        Set<Name> filtered = ImmutableSet.of(TerasologyConstants.ENGINE_MODULE, new Name("engine-test"));
        for (Module remote : remoteModuleRegistry) {
            remoteModules.add(remote);
            System.out.println("Found Remote Module: " + remote.getId());
        }
        remoteModules.sort(MODULE_INFO_COMPARATOR);
        return remoteModules;
    }

    private static List<Module> getListOfGameModules(ModuleManager moduleManager) {
        List<Module> gameModules = Lists.newArrayList();
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module latestVersion = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (!latestVersion.isOnClasspath()) {
                gameModules.add(latestVersion);
                System.out.println("Found Game Module: " + latestVersion.getId());
            }
        }
        gameModules.sort(MODULE_INFO_COMPARATOR);
        return gameModules;
    }

    private static String scanModules(ModuleManager moduleManager, List<Module> allSortedModules) {
        Preconditions.checkNotNull(moduleManager);
        Preconditions.checkNotNull(allSortedModules);

        StringBuilder out = new StringBuilder();
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Module module : allSortedModules) {
            Name moduleId = module.getId();
            if (module.isCodeModule()) {
                String moduleDescription = getModuleDescription(module);
                out.append(moduleDescription);

                Set<Module> modules = new HashSet<>();
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    modules = result.getModules();
                } else {
                    modules.add(module);
                }

                out.append(exportEvents(moduleManager, moduleId, modules));
            }
            out.append(System.getProperty("line.separator"));
        }
        return out.toString();
    }

    private static String exportEvents(ModuleManager moduleManager, Name moduleId, Set<Module> modules) {
        Preconditions.checkNotNull(modules);

        StringBuilder events = new StringBuilder();
        try (ModuleEnvironment environment2 = moduleManager.loadEnvironment(modules, false)) {

            for (Class<? extends Event> type : environment2.getSubtypesOf(Event.class)) {
                Name mod = environment2.getModuleProviding(type);

                if (!mod.toString().equals(moduleId.toString()))
                    continue;

                events.append("  ").append(type.getName());
                events.append(System.getProperty("line.separator"));

                // Are there any other classes derived from this class?
                for (Class<?> eventType : environment2.getSubtypesOf(type)) {
                    events.append("    ").append(eventType.getName());
                    events.append("    " + eventType.getName());
                    events.append(System.getProperty("line.separator"));
                }
            }
        }

        StringBuilder out = new StringBuilder();
        out.append(System.getProperty("line.separator"));
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
        Preconditions.checkNotNull(module);
        Preconditions.checkNotNull(module.getMetadata());

        final ModuleMetadata metadata = module.getMetadata();

        StringBuilder details = new StringBuilder();
        details.append(System.getProperty("line.separator"));
        details.append("# ").append(metadata.getId()); // Used as anchor tag with in Module Table.
        details.append(System.getProperty("line.separator"));
        details.append("- **Display Name:** ").append(metadata.getDisplayName());
        details.append(System.getProperty("line.separator"));
        details.append("- **Version:** ").append(metadata.getVersion());
        details.append(System.getProperty("line.separator"));

        I18nMap description = metadata.getDescription();
        if (description != null && !description.value().isEmpty()) {
            details.append("- **Description:** ").append(description);
            details.append(System.getProperty("line.separator"));
        }

        String permissions = String.join(", ", metadata.getRequiredPermissions());
        if (!permissions.isEmpty()) {
            details.append("- **Permissions:** ").append(permissions);
            details.append(System.getProperty("line.separator"));
        }

        details.append("- **Github:** ").append(getOriginModuleUrl(module));
        details.append(System.getProperty("line.separator"));

        String author = ExtraDataModuleExtension.getAuthor(module);
        if (!author.isEmpty()) {
            details.append("- **Author:** ").append(author);
            details.append(System.getProperty("line.separator"));
        }

        String categories = getModuleTags(module);
        if (categories != null && !categories.isEmpty()) {
            details.append("- **Categories:** ").append(categories);
            details.append(System.getProperty("line.separator"));
        }

        details.append(getModuleExtensions(module));

        details.append(getModuleDependencies(metadata));

        details.append(System.getProperty("line.separator"));
        return details.toString();
    }

    private static String getModuleDependencies(ModuleMetadata metadata) {
        Preconditions.checkNotNull(metadata);

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
        Preconditions.checkNotNull(module);

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
        String origin = ExtraDataModuleExtension.getOrigin(module);
        if (StringUtils.isNotEmpty(origin)) {
            extensions.append("|  Origin: | _").append(origin).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean serverSideOnly = StandardModuleExtension.isServerSideOnly(module);
        extensions.append("|  serverSideOnly: | _").append(serverSideOnly).append("_ |");
        extensions.append(System.getProperty("line.separator"));
        Boolean gameplayModule = StandardModuleExtension.isGameplayModule(module);
        extensions.append("|  gameplayModule: | _").append(gameplayModule).append("_ |");
        extensions.append(System.getProperty("line.separator"));
        Boolean isAsset = metadata.getExtension(IS_ASSET.getKey(), Boolean.class);
        if (isAsset != null) {
            extensions.append("|  IS_ASSET: | _").append(isAsset).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean isWorld = metadata.getExtension(IS_WORLD.getKey(), Boolean.class);
        if (isAsset != null) {
            extensions.append("|  IS_WORLD: | _").append(isWorld).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean isLibrary = metadata.getExtension(StandardModuleExtension.IS_LIBRARY.getKey(), Boolean.class);
        if (isAsset != null) {
            extensions.append("|  IS_LIBRARY: | _").append(isLibrary).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean isSpecial = metadata.getExtension(StandardModuleExtension.IS_SPECIAL.getKey(), Boolean.class);
        if (isAsset != null) {
            extensions.append("|  IS_SPECIAL: | _").append(isSpecial).append("_ |");
            extensions.append(System.getProperty("line.separator"));
        }
        Boolean isAug = metadata.getExtension(StandardModuleExtension.IS_AUGMENTATION.getKey(), Boolean.class);
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
        Preconditions.checkNotNull(module);
        final String origin = ExtraDataModuleExtension.getOrigin(module);
        if (StringUtils.isBlank(origin) && !INTERNAL_MODULES.contains(module.getId().toString())) {
            try {
                return new URL(DEFAULT_GITHUB_MODULE_URL + module.getId()).toString();
            }catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return origin;
    }

    private static String getModuleTags(final Module module) {
        Preconditions.checkNotNull(module);
        return StandardModuleExtension.booleanPropertySet().stream()
                .filter(ext -> ext.isProvidedBy(module))
                .map(StandardModuleExtension::getKey)
                .collect(Collectors.joining(", "));
    }
}


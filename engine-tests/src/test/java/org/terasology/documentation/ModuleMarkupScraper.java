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
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.ExtraDataModuleExtension;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.entitySystem.event.Event;
import org.terasology.module.*;
import org.terasology.naming.Name;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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

        StringBuilder out = new StringBuilder();

        List<Module> allModules = Lists.newArrayList();
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module latestVersion = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (!latestVersion.isOnClasspath()) {
                allModules.add(latestVersion);
                System.out.println("Found Game Module: " + latestVersion.getId());
            }
        }

        Iterable<Module> remoteModuleRegistry = moduleManager.getInstallManager().getRemoteRegistry();
        Set<Name> filtered = ImmutableSet.of(TerasologyConstants.ENGINE_MODULE, new Name("engine-test"));
        for (Module remote : remoteModuleRegistry) {
            allModules.add(remote);
            System.out.println("Found Remote Module: " + remote.getId());
        }

        allModules.sort(moduleInfoComparator);

        List<Module> allSortedModules;
        allSortedModules = new ArrayList<>(allModules);

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Module module : allSortedModules) {
            Name moduleId = module.getId();
            if (module.isCodeModule()) {
                out.append("Examining Module: " + moduleId);
                out.append(System.getProperty("line.separator"));

                String moduleDescription = getModuleDescription(moduleManager, module);
                out.append("Module Description: " + moduleDescription);

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

        System.out.println(out);
        env.close();
    }

    private static String ExportEvents(Name moduleId, Set<Module> modules) {
        StringBuilder events = new StringBuilder();
        try (ModuleEnvironment environment2 = moduleManager.loadEnvironment(modules, false)) {

            for (Class<? extends Event> type : environment2.getSubtypesOf(Event.class)) {
                Name mod = environment2.getModuleProviding(type);

                if (!mod.toString().equals(moduleId.toString()))
                    continue;

                events.append("  " + type.getName()); //  + " | From Module: " + mod
                events.append(System.getProperty("line.separator"));

                // Are there any other classes derived from this class?
                for (Class<? extends Object> eventType : environment2.getSubtypesOf(type)) {
                    events.append("    " + eventType.getName()); //  + " | Event Module: " + environment2.getModuleProviding(eventType)
                    events.append(System.getProperty("line.separator"));
                }
            }
        }
        StringBuilder out = new StringBuilder();
        if (events.length() > 0) {
            out.append("Module Events: ");
            out.append(System.getProperty("line.separator"));
            out.append(events);
            out.append(System.getProperty("line.separator"));
        } else {
            out.append("Module Events: None");
            out.append(System.getProperty("line.separator"));
        }
        return out.toString();
    }

    private static String getModuleDescription(final ModuleManager moduleManager, final Module module) {
        if (module == null || module.getMetadata() == null) {
            return "";
        }
        final ModuleMetadata metadata = module.getMetadata();

        StringBuilder details = new StringBuilder();
        details.append(System.getProperty("line.separator"));
        details.append("ID: " + metadata.getId());
        details.append(System.getProperty("line.separator"));
        details.append("Display Name: " + metadata.getDisplayName());
        details.append(System.getProperty("line.separator"));
        details.append("Version: " + metadata.getVersion());
        details.append(System.getProperty("line.separator"));
        details.append("Description: " + metadata.getDescription());
        details.append(System.getProperty("line.separator"));
        details.append("Permissions: " + String.join(", ", metadata.getRequiredPermissions()));
        details.append(System.getProperty("line.separator"));
        details.append("Github: " + getOriginModuleUrl(module));
        details.append(System.getProperty("line.separator"));
        details.append("Author: " + ExtraDataModuleExtension.getAuthor(module));
        details.append(System.getProperty("line.separator"));
        details.append("Categories: " + getModuleTags(module));
        details.append(System.getProperty("line.separator"));
        details.append("Dependencies: ");

        final List<DependencyInfo> dependencies = metadata.getDependencies();
        StringBuilder dependenciesNames = new StringBuilder();
        if (dependencies != null && !dependencies.isEmpty()) {
            for (DependencyInfo dependency : dependencies) {
                if (dependenciesNames.length() > 0) {
                    dependenciesNames
                            .append(", ")
                            .append(dependency.getId().toString());
                } else {
                    dependenciesNames.append(dependency.getId().toString());
                }
            }
        } else {
            dependenciesNames.append("None");
        }
        details.append(dependenciesNames);
        details.append(System.getProperty("line.separator"));
        return details.toString();
    }

    private static String getOriginModuleUrl(Module module) {
        final String origin = ExtraDataModuleExtension.getOrigin(module);
        if (StringUtils.isBlank(origin) && !INTERNAL_MODULES.contains(module.getId().toString())) {
            return DEFAULT_GITHUB_MODULE_URL + module.getId();
        }
        return origin;
    }

    private static String getOnlineVersion(final ModuleManager moduleManager, final DependencyInfo dependencyInfo) {
        return moduleManager.getInstallManager().getRemoteRegistry().stream()
                .filter(module -> module.getId().equals(dependencyInfo.getId()))
                .findFirst()
                .map(Module::getVersion)
                .map(String::valueOf)
                .orElse("");
    }

    private static String getModuleTags(final Module module) {
        return StandardModuleExtension.booleanPropertySet().stream()
                .filter(ext -> ext.isProvidedBy(module))
                .map(StandardModuleExtension::getKey)
                .collect(Collectors.joining(", "));
    }
}


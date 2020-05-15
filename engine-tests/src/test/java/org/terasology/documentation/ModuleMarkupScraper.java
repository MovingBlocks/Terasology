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

import org.codehaus.plexus.util.StringUtils;
import org.terasology.HeadlessEnvironment;
import org.terasology.context.Context;
import org.terasology.engine.module.ExtraDataModuleExtension;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.entitySystem.event.Event;
import org.terasology.module.*;
import org.terasology.naming.Name;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModuleMarkupScraper {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_GITHUB_MODULE_URL = "https://github.com/Terasology/";
    private static final List INTERNAL_MODULES = Arrays.asList("Core", "engine", "CoreSampleGameplay", "BuilderSampleGameplay", "BiomesAPI");

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

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (module.isCodeModule()) {
                System.out.println("Examining Module: " + moduleId);

                String moduleDescription = getModuleDescription(moduleManager, module);

                System.out.println("  Module Description: " + moduleDescription);

                Set<Module> modules = new HashSet<>();
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    modules = result.getModules();
                } else {
                    modules.add(module);
                }

                try (ModuleEnvironment environment2 = moduleManager.loadEnvironment(modules, false)) {
                    for (Class<? extends Event> type : environment2.getSubtypesOf(Event.class)) {
                        Name mod = environment2.getModuleProviding(type);

                        if (!mod.toString().equals(moduleId.toString()))
                            continue;

                        System.out.println("  Event: " + type.getSimpleName() + " | From Module: " + mod);

                        // Are there any other classes derived from this class?
                        for (Class<? extends Object> eventType : environment2.getSubtypesOf(type)) {
                            System.out.println("    " + eventType.getName() + " | Event Module: " + environment2.getModuleProviding(eventType));
                        }
                    }
                }
            }
        }
        env.close();
    }

    private static String getModuleDescription(final ModuleManager moduleManager, final Module module) {
        if (module == null || module.getMetadata() == null) {
            return "";
        }
        final ModuleMetadata metadata = module.getMetadata();

        StringBuilder dependenciesNames = new StringBuilder();
        dependenciesNames.append(System.getProperty("line.separator"));
        dependenciesNames.append("ID: " + metadata.getId());
        dependenciesNames.append(System.getProperty("line.separator"));
        dependenciesNames.append("Display Name: " + metadata.getDisplayName());
        dependenciesNames.append(System.getProperty("line.separator"));
        dependenciesNames.append("Version: " + metadata.getVersion());
        dependenciesNames.append(System.getProperty("line.separator"));
        dependenciesNames.append("Description: " + metadata.getDescription());
        dependenciesNames.append(System.getProperty("line.separator"));
        dependenciesNames.append("Permissions: " + String.join(", ", metadata.getRequiredPermissions()));
        dependenciesNames.append(System.getProperty("line.separator"));
        dependenciesNames.append("Github: " + getOriginModuleUrl(module));
        dependenciesNames.append(System.getProperty("line.separator"));
        dependenciesNames.append("Author: " + ExtraDataModuleExtension.getAuthor(module));
        dependenciesNames.append(System.getProperty("line.separator"));
        dependenciesNames.append("Categories: " + getModuleTags(module));
        dependenciesNames.append(System.getProperty("line.separator"));

        final List<DependencyInfo> dependencies = metadata.getDependencies();
        if (dependencies != null && !dependencies.isEmpty()) {
            dependenciesNames.append("Dependencies: ");
            for (DependencyInfo dependency : dependencies) {
                dependenciesNames
                        .append("   ")
                        .append(dependency.getId().toString())
                        .append('\n');
            }
        } else {
            dependenciesNames.append("Dependencies: None");
        }
        dependenciesNames.append(System.getProperty("line.separator"));
        return dependenciesNames.toString();
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


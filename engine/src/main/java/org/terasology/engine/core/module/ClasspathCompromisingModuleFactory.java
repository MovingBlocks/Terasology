// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.reflections.util.ClasspathHelper;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadata;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class ClasspathCompromisingModuleFactory extends ModuleFactory {
    @Override
    public Module createDirectoryModule(ModuleMetadata metadata, File directory) {
        Module module = super.createDirectoryModule(metadata, directory);
        return new Module(
                module.getMetadata(), module.getResources(),
                module.getClasspaths(), module.getModuleManifest(),
                new ClassesInModule(module));
    }

    @Override
    public Module createArchiveModule(ModuleMetadata metadata, File archive) throws IOException {
        Module module = super.createArchiveModule(metadata, archive);
        return module;
    }

    static class ClassesInModule implements Predicate<Class<?>> {

        private final Set<URL> classpaths;
        private final ClassLoader[] classLoaders;

        public ClassesInModule(Module module) {
            classpaths = module.getClasspaths().stream().map(f -> {
                try {
                    return f.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toSet());
            classLoaders = module.getModuleManifest().getConfiguration().getClassLoaders();
        }

        @Override
        public boolean test(Class<?> aClass) {
            return classpaths.contains(ClasspathHelper.forClass(aClass, classLoaders));
        }
    }
}

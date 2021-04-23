// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.reflections.util.ClasspathHelper;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadata;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Creates modules that can own classes that were loaded without a ModuleClassLoader.
 * <p>
 * When {@link ModuleEnvironment#getModuleProviding(Class)} checks modules built using the default
 * ModuleFactory, it will only acknowledge the class as belonging to that module if it was loaded
 * using the module's ModuleClassLoader.
 * <p>
 * This factory will recognize classes as belonging to the module as long as that class's source
 * location is within the module's directory or archive.
 * <p>
 * ⚠ Usually <em>checking the classloader is sufficient</em> and thus you
 * should <em>not</em> find the need to use this in production code. It's useful in cases where
 * the module <em>cannot</em> be loaded using a ModuleClassLoader (e.g. a test runner) and it's
 * acceptable to run without the protections ModuleClassLoader provides.
 */
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
        return new Module(
                module.getMetadata(), module.getResources(),
                module.getClasspaths(), module.getModuleManifest(),
                new ClassesInModule(module));
    }

    static class ClassesInModule implements Predicate<Class<?>> {

        private final Set<URL> classpaths;
        private final ClassLoader[] classLoaders;
        private final String name;

        ClassesInModule(Module module) {
            classpaths = module.getClasspaths().stream().map(f -> {
                try {
                    URL url = f.toURI().toURL();
                    if (f.getName().endsWith(".jar")) {
                        // Code from jars has a `jar:` URL.
                        return new URL("jar", null, url.toString() + "!/");
                    }
                    return url;
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(ImmutableSet.toImmutableSet());
            classLoaders = module.getModuleManifest().getConfiguration().getClassLoaders();
            name = module.getId().toString();
        }

        @Override
        public boolean test(Class<?> aClass) {
            URL classUrl = ClasspathHelper.forClass(aClass, classLoaders);
            return classpaths.contains(classUrl);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", name)
                    .toString();
        }
    }
}

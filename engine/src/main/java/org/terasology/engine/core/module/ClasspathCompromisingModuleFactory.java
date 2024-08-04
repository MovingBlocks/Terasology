// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadata;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;

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
    private static final Logger logger = LoggerFactory.getLogger(ClasspathCompromisingModuleFactory.class);

    @Override
    public Module createDirectoryModule(ModuleMetadata metadata, File directory) {
        Module module = super.createDirectoryModule(metadata, directory);
        return new Module(
                module.getMetadata(), module.getResources(),
                module.getClasspaths(), module.getClassIndex(),
                new ClassesInModule(module));
    }

    @Override
    public Module createArchiveModule(ModuleMetadata metadata, File archive) throws IOException {
        Module module = super.createArchiveModule(metadata, archive);
        return new Module(
                module.getMetadata(), module.getResources(),
                module.getClasspaths(), module.getClassIndex(),
                new ClassesInModule(module));
    }

    /**
     * Find the location of the module containing this URL.
     * <p>
     * Accounts for loading modules from development workspaces that may have their build directories
     * on the classpath, as is the case when running tests.
     *
     * @see #setDefaultCodeSubpath
     * @see #setDefaultLibsSubpath
     *
     * @param metadataName the expected name of the metadata file, as it would appear in {@link #getModuleMetadataLoaderMap()}
     * @param metadataUrl a URL of a metadata file, such as might be returned from {@link ClassLoader#getSystemResource}
     * @return the module's base directory, or a jar file if it doesn't look like a local build
     */
    Path canonicalModuleLocation(String metadataName, URL metadataUrl) {
        checkArgument(getModuleMetadataLoaderMap().containsKey(metadataName),
                "metadataName `%s` is not in loader map", metadataName);
        if (metadataUrl.getProtocol().equals("jar")) {
            return modulePathFromMetadataJarUrl(metadataUrl);
        } else {
            return modulePathFromMetadataFileUrl(metadataName, metadataUrl);
        }
    }

    private Path modulePathFromMetadataFileUrl(String metadataName, URL url) {
        Path path = fromUrl(url);
        // We are considering the location of a resource file, so compare it to the code path.
        // Include the metadata name in case it has path components of its own.
        Path relativePathFromModuleRoot = Paths.get(getDefaultCodeSubpath(), metadataName);
        return findModuleRoot(relativePathFromModuleRoot, path).orElse(path);
    }

    private Path modulePathFromMetadataJarUrl(URL jarUrl) {
        checkArgument(jarUrl.getProtocol().equals("jar"), "Not a jar URL: %s", jarUrl);
        URL fileUrl;
        try {
            JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
            fileUrl = connection.getJarFileURL();
            // despite the method name, openConnection doesn't open anything unless we
            // call connect(), so we needn't clean up anything here.
        } catch (IOException e) {
            throw new RuntimeException("Failed to get file from " + jarUrl, e);
        }
        Path path = fromUrl(fileUrl);
        // We are considering the location of a jar file, so compare it to the libs path.
        Path relativePathFromModuleRoot = Paths.get(getDefaultLibsSubpath());
        // Assume jars would be directly in the libs path (not in a subdirectory).
        Path jarDirectory = path.getParent();
        return findModuleRoot(relativePathFromModuleRoot, jarDirectory).orElse(path);
    }

    /**
     * Find the root of a module build directory.
     * <p>
     * If {@code path} matches a known build directory sub-path, return the base directory.
     * <p>
     * Example:
     * <ul>
     *   <li>findModuleRoot("foo/bar.txt", "/some/base/foo/bar.txt") == "/some/base"
     *   <li>findModuleRoot("foo/bar.txt", "/some/base/x/foo/bar.txt") == "/some/base/x"
     *   <li>findModuleRoot("foo/bar.txt", "/some/base/foo/x/bar.txt") == <i>Empty</i>
     *   <li>findModuleRoot("foo/bar.txt", "/some/base/foo/baz.txt") == <i>Empty</i>
     * </ul>
     */
    private static Optional<Path> findModuleRoot(Path relativePathFromModuleRoot, Path path) {
        if (path.endsWith(relativePathFromModuleRoot)) {
            int relativeDepth = relativePathFromModuleRoot.getNameCount();
            Path parentPath = path.subpath(0, path.getNameCount() - relativeDepth);
            if (path.getRoot() != null) {  // TODO: test case
                parentPath = path.getRoot().resolve(parentPath);
            }
            return Optional.of(parentPath);
        } else {
            logger.warn("does not seem to be in a build directory {}", path);
            return Optional.empty();
        }
    }

    /**
     * Convert a URL to a Path without checked exceptions.
     * <p>
     * {@link URISyntaxException} is a rare edge case, not worth losing the ability to use this in a mapping function
     * or the noise of try/catch blocks around every usage.
     */
    private static Path fromUrl(URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (RuntimeException | URISyntaxException e) {
            throw new RuntimeException("Failed getting URL " + url, e);
        }
    }


    static class ClassesInModule implements Predicate<Class<?>> {

        private final Set<URL> classpaths;
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
            name = module.getId().toString();
        }

        @Override
        public boolean test(Class<?> aClass) {
            URL classUrl = ClasspathHelper.forClass(aClass);
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

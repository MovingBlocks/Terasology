// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;
import org.terasology.unittest.ExampleClass;
import org.terasology.unittest.ExampleInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.Truth8.assertThat;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClasspathCompromisingModuleFactoryTest {
    static final Class<?> SOME_CLASS_OUTSIDE_THE_MODULE = ClasspathCompromisingModuleFactory.class;
    static final String METADATA_NAME = "module.json";

    ClasspathCompromisingModuleFactory factory;

    @BeforeEach
    public void newFactory() {
        factory = new ClasspathCompromisingModuleFactory();
        factory.setDefaultLibsSubpath("build/libs");
    }

    @Test
    public void directoryModuleContainsClass() {
        // This test assumes that the unittest module is under the current working directory (`engine-test/`)
        File engineTestDirectory = new File(System.getProperty("user.dir", "."));
        ModuleMetadata metadata = new ModuleMetadata(new Name("unittest"), new Version("1.0.0"));
        Module module = factory.createDirectoryModule(metadata, engineTestDirectory);

        // and that ExampleClass is inside that directory
        assertTrue(module.getClassPredicate().test(ExampleClass.class));
        // and that this other class (in engine, not engine-test) is outside that directory.
        assertFalse(module.getClassPredicate().test(SOME_CLASS_OUTSIDE_THE_MODULE));

        // These assumptions could break if things get moved around enough.
    }

    @Test
    @Disabled("TODO: need a jar module containing classes")
    public void archiveModuleContainsClass() throws IOException {
        Module module = factory.createArchiveModule(new File("FIXME.jar"));

        String someClassInTheModule = module.getClassIndex().getTypesAnnotatedWith(API.class.getName()).iterator().next();

        try {
            assertTrue(module.getClassPredicate().test(Class.forName(someClassInTheModule)));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        assertFalse(module.getClassPredicate().test(SOME_CLASS_OUTSIDE_THE_MODULE));
    }

    /**
     * A module's code is present at more than one location in a development build: the compiled
     * classes directory and the jar packed from it. Gradle puts the classes directory on the
     * classpath for the module under test but resolves its module <em>dependencies</em> as jars,
     * so a class may well be served from the jar while the module is loaded from its directory.
     * Both have to count as belonging to the module.
     */
    @Test
    public void directoryModuleContainsClassLoadedFromJar(@TempDir Path moduleDirectory) throws Exception {
        // Example:
        //   - m/build/classes/org/t/Foo.class
        //   - m/build/libs/foo.jar
        // load m as directory module while foo.jar is on classpath
        String className = ExampleClass.class.getName();
        // The interface comes along so the isolated class loader below can resolve it without
        // delegating to a parent (which would hand back the original class, not the jar's copy).
        List<String> classResources = Stream.of(ExampleClass.class, ExampleInterface.class)
                .map(c -> c.getName().replace('.', '/') + ".class")
                .collect(Collectors.toList());

        Path classesDirectory = moduleDirectory.resolve(Paths.get("build", "classes"));
        for (String classResource : classResources) {
            Files.createDirectories(classesDirectory.resolve(classResource).getParent());
            try (InputStream in = requireNonNull(getClass().getClassLoader().getResourceAsStream(classResource))) {
                Files.copy(in, classesDirectory.resolve(classResource));
            }
        }

        Path jar = moduleDirectory.resolve(Paths.get("build", "libs", "example.jar"));
        Files.createDirectories(jar.getParent());
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            for (String classResource : classResources) {
                out.putNextEntry(new JarEntry(classResource));
                Files.copy(classesDirectory.resolve(classResource), out);
                out.closeEntry();
            }
        }

        ModuleMetadata metadata = new ModuleMetadata(new Name("example"), new Version("1.0.0"));
        Module module = factory.createDirectoryModule(metadata, moduleDirectory.toFile());

        // Load the class from the jar so that its code source is the jar, not the classes
        // directory - the situation a module dependency is in when resolved as an artifact.
        try (URLClassLoader jarLoader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, null)) {
            Class<?> fromJar = jarLoader.loadClass(className);
            assertEquals(jar.toUri().toURL(), fromJar.getProtectionDomain().getCodeSource().getLocation());

            assertTrue(module.getClassPredicate().test(fromJar));
        }

        assertFalse(module.getClassPredicate().test(SOME_CLASS_OUTSIDE_THE_MODULE));
    }

    @Test
    public void canGetPathFromJarResource() throws MalformedURLException {
        // A jar file on the classpath but not in a local build directory.
        URL jarUrl = new URL("jar:file:/example/Terasology/cachedModules/CoreAssets-2.3.0-SNAPSHOT.jar!/module.json");
        Path expectedPath = Paths.get("/example/Terasology/cachedModules/CoreAssets-2.3.0-SNAPSHOT.jar");

        assertThat(factory.canonicalModuleLocation(METADATA_NAME, jarUrl)).isEqualTo(expectedPath);
    }

    @Test
    public void canGetPathFromLocalJarBuild() throws MalformedURLException {
        // A jar file on the classpath that is a build directory in a local development workspace
        URL jarUrl = new URL("jar:file:/example/Terasology/modules/CoreAssets/build/libs/CoreAssets-2.3.0-SNAPSHOT.jar!/module.json");
        Path expectedPath = Paths.get("/example/Terasology/modules/CoreAssets");

        assertThat(factory.canonicalModuleLocation(METADATA_NAME, jarUrl)).isEqualTo(expectedPath);
    }

    @Test
    public void canGetPathFromFilesystemResource() throws MalformedURLException {
        // A directory on the classpath that is a build directory in a local development workspace
        URL fileUrl = new URL("file:/example/Terasology/modules/Health/build/classes/module.json");
        Path expectedPath = Paths.get("/example/Terasology/modules/Health");

        assertThat(factory.canonicalModuleLocation(METADATA_NAME, fileUrl)).isEqualTo(expectedPath);
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;
import org.terasology.unittest.ExampleClass;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.truth.Truth8.assertThat;
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

    @Test
    @Disabled("TODO: need a jar module alongside a classes directory")
    public void directoryModuleContainsClassLoadedFromJar() {
        // Example:
        //   - m/build/classes/org/t/Foo.class
        //   - m/build/libs/foo.jar
        // load m as directory module while foo.jar is on classpath
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

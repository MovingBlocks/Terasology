// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;
import org.terasology.unittest.ExampleClass;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClasspathCompromisingModuleFactoryTest {
    final static Class<?> someClassOutsideTheModule = ClasspathCompromisingModuleFactory.class;

    @Test
    public void directoryModuleContainsClass() {
        ModuleFactory factory = new ClasspathCompromisingModuleFactory();

        // This test assumes that the unittest module is under the current working directory (`engine-test/`)
        File engineTestDirectory = new File(System.getProperty("user.dir", "."));
        ModuleMetadata metadata = new ModuleMetadata(new Name("unittest"), new Version("1.0.0"));
        Module module = factory.createDirectoryModule(metadata, engineTestDirectory);

        // and that ExampleClass is inside that directory
        assertTrue(module.getClassPredicate().test(ExampleClass.class));
        // and that this other class (in engine, not engine-test) is outside that directory.
        assertFalse(module.getClassPredicate().test(someClassOutsideTheModule));

        // These assumptions could break if things get moved around enough.
    }

    @Test
    @Disabled("TODO: need a jar module containing classes")
    public void archiveModuleContainsClass() throws IOException {
        ModuleFactory factory = new ClasspathCompromisingModuleFactory();

        Module module = factory.createArchiveModule(new File("FIXME.jar"));

        Class<?> someClassInTheModule = module.getModuleManifest().getTypesAnnotatedWith(API.class).iterator().next();

        assertTrue(module.getClassPredicate().test(someClassInTheModule));
        assertFalse(module.getClassPredicate().test(someClassOutsideTheModule));
    }

    @Test
    @Disabled("TODO: need a jar module alongside a classes directory")
    public void directoryModuleContainsClassLoadedFromJar() {
        // Example:
        //   - m/build/classes/org/t/Foo.class
        //   - m/build/libs/foo.jar
        // load m as directory module while foo.jar is on classpath
    }
}

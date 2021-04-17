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

        File engineTestDirectory = new File(System.getProperty("user.dir", "."));

        ModuleMetadata metadata = new ModuleMetadata(new Name("unittest"), new Version("1.0.0"));

        Module module = factory.createDirectoryModule(metadata, engineTestDirectory);

        assertTrue(module.getClassPredicate().test(ExampleClass.class));
        assertFalse(module.getClassPredicate().test(someClassOutsideTheModule));
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
}

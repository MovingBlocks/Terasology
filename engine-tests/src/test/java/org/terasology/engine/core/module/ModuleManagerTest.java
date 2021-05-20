// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.reflections.Reflections;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.logic.permission.PermissionSetComponent;
import org.terasology.engine.world.block.structure.AttachSupportRequiredComponent;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.resources.EmptyFileSource;
import org.terasology.gestalt.module.sandbox.PermissionProvider;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;
import org.terasology.unittest.stubs.StubSubsystem;

import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.terasology.engine.core.TerasologyConstants.ENGINE_MODULE;

public class ModuleManagerTest {

    ModuleManager manager;
    ModuleEnvironment environment;
    Module engineModule;

    @BeforeEach
    public void provideEngineModule() {
        manager = new ModuleManager("");
        environment = manager.getEnvironment();
        engineModule = environment.get(ENGINE_MODULE);

        // assert we did not load extra modules that may have been laying around.
        assertThat(manager.getRegistry().getModuleIds()).containsExactly(ENGINE_MODULE);
        assertThat(environment.getModuleIdsOrderedByDependencies()).containsExactly(ENGINE_MODULE);
    }

    private Module getEmptyTestModule() {
        return new Module(
                new ModuleMetadata(new Name("EmptyTestModule"), new Version("0.0.1")),
                new EmptyFileSource(),
                Collections.emptyList(),
                new Reflections(),
                (clazz) -> false
        );
    }

    @Test
    public void nonApiClassesAreNotPermitted() {
        Class<?> disallowedClass = PermissionSetComponent.class;
        PermissionProvider permissionProvider = manager.getPermissionProvider(getEmptyTestModule());
        assertFalse(permissionProvider.isPermitted(disallowedClass));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Component.class,
            AttachSupportRequiredComponent.class,
            PermissionSetComponent.class
    })
    public void nonApiClassesHaveAModuleWhichProvidesThem(Class<?> clazz) {
        // These classes should be recognized as belonging to the engine module, even if access
        // to them is not permitted from other modules.
        assertThat(environment.getModuleProviding(clazz)).isEqualTo(ENGINE_MODULE);
    }

    @Test
    public void engineModuleContainsSubsystems() {
        Class<StubSubsystem> subsystem = StubSubsystem.class;
        manager = new ModuleManager("", Collections.singletonList(subsystem));
        environment = manager.getEnvironment();
        engineModule = environment.get(ENGINE_MODULE);

        assertThat(engineModule.getModuleManifest().getSubTypesOf(EngineSubsystem.class)).contains(subsystem);

        assertThat(environment.getSubtypesOf(EngineSubsystem.class)).contains(subsystem);

        assertThat(environment.getModuleProviding(subsystem)).isEqualTo(ENGINE_MODULE);
    }
}

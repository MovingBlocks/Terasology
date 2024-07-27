// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.terasology.engine.config.flexible.AutoConfig;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.PathManagerProvider;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.logic.permission.PermissionSetComponent;
import org.terasology.engine.world.block.structure.AttachSupportRequiredComponent;
import org.terasology.gestalt.di.index.CompoundClassIndex;
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
import static org.mockito.Mockito.when;
import static org.terasology.engine.core.TerasologyConstants.ENGINE_MODULE;

@ExtendWith(PathManagerProvider.class)
@ExtendWith(MockitoExtension.class)
public class ModuleManagerTest {

    ModuleManager manager;
    ModuleEnvironment environment;
    Module engineModule;

    /** Configure a ModuleManager with only the <i>engine module.</i> */
    @BeforeEach
    public void provideEngineModule(PathManager pathManager) {
        when(pathManager.getModulePaths()).thenReturn(Collections.emptyList());
        manager = new ModuleManager("");
        environment = manager.getEnvironment();
        engineModule = environment.get(ENGINE_MODULE);

        // assert we did not load extra modules that may have been laying around.
        assertThat(manager.getRegistry().getModuleIds()).containsExactly(ENGINE_MODULE);
        assertThat(environment.getModuleIdsOrderedByDependencies()).containsExactly(ENGINE_MODULE);
    }

    /** A bare minimum Module not associated with any real code or assets. */
    private Module getEmptyTestModule() {
        return new Module(
                new ModuleMetadata(new Name("EmptyTestModule"), new Version("0.0.1")),
                new EmptyFileSource(),
                Collections.emptyList(),
                new CompoundClassIndex(),
                (clazz) -> false
        );
    }

    /**
     * Non-API classes are not permitted.
     * <p>
     * Tests against the {@link PermissionProvider} that ModuleManager gives a module.
     * <p>
     * A test failure here may indicate that modules have unrestricted access to engine classes they
     * should not.
     * <p>
     * ⚠ This test <em>passing</em> should <strong>not</strong> be taken to mean the protections
     * of the sandbox are intact. The PermissionProvider provides only one piece of the logic and
     * is not always the deciding factor.
     *
     * @param disallowedClass A class in {@code org.terasology.engine} not annotated with @API
     *     nor in any package annotated with @API.
     */
    @ParameterizedTest
    @ValueSource(classes = {
            PathManager.class,
            PermissionSetComponent.class
    })
    public void nonApiClassesAreNotPermitted(Class<?> disallowedClass) {
        PermissionProvider permissionProvider = manager.getPermissionProvider(getEmptyTestModule());
        assertFalse(permissionProvider.isPermitted(disallowedClass));
    }

    /**
     * Non-API classes have a module which provides them.
     * <p>
     * There are some classes that are not marked as being part of a public API, but we still expect the
     * {@link ModuleEnvironment} to recognize which module provides them.
     * <p>
     * Test failures of this type can show up in the application as a failure to get a URI for a class,
     * or find a class from a URI, or get a subtype of a class. These are often NullPointerExceptions.
     *
     * @param engineClass A class in {@code org.terasology.engine} not annotated with @API
     *      nor in any package annotated with @API.
     */
    @ParameterizedTest
    @ValueSource(classes = {
            AttachSupportRequiredComponent.class,
            PermissionSetComponent.class
    })
    public void nonApiClassesHaveAModuleWhichProvidesThem(Class<?> engineClass) {
        assertThat(environment.getModuleProviding(engineClass)).isEqualTo(ENGINE_MODULE);
    }

    /**
     * Classes from subsystems are contained in the <i>engine module.</i>
     * <p>
     * Test failures of this type appear as failures to find classes from the subsystem, such as
     * its {@link AutoConfig} class.
     * <p>
     * (This policy may change in the future to better reflect the fact that the set of subsystems
     * is not always the same, and it would be useful to know which classes are provided by which
     * subsystem.)
     * */
    @Test
    public void engineModuleContainsSubsystems() {
        Class<StubSubsystem> subsystem = StubSubsystem.class;
        manager = new ModuleManager("", Collections.singletonList(subsystem));
        environment = manager.getEnvironment();
        engineModule = environment.get(ENGINE_MODULE);

        assertThat(engineModule.getClassIndex().getSubtypesOf(EngineSubsystem.class.getName())).contains(subsystem.getName());

        assertThat(environment.getSubtypesOf(EngineSubsystem.class)).contains(subsystem);

        assertThat(environment.getModuleProviding(subsystem)).isEqualTo(ENGINE_MODULE);
    }
}

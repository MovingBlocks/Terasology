
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.subsystem.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.config.BindsConfig;
import org.terasology.engine.config.facade.BindsConfiguration;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.config.BindsSubsystem.BindsConfigAdapter;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.BindableButton;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindAxis;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.TableModuleRegistry;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.input.Keyboard.Key;
import org.terasology.input.Keyboard.KeyId;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BindsSubsystemTest {

    private static final String TEST_MODULE = "TestModule";

    private BindsSubsystem bindsSubsystem;
    private BindsConfiguration bindsConfiguration;
    private Context context;
    private List<Class<?>> registerBindButtonClasses;
    private List<Class<?>> registerRealBindAxisClasses;

    @BeforeEach
    public void setUp() {
        bindsSubsystem = new BindsSubsystem();
        bindsConfiguration = new BindsConfigAdapter(new BindsConfig());
        context = new ContextImpl();
        context.put(BindsConfiguration.class, bindsConfiguration);
        bindsSubsystem.preInitialise(context);
        setUpMockModuleEnvironment();
    }

    private void setUpMockModuleEnvironment() {
        ModuleManager moduleManager = mock(ModuleManager.class);
        ModuleRegistry moduleRegistry = new TableModuleRegistry();

        Module module = mock(Module.class);
        when(module.getId()).thenReturn(new Name(TEST_MODULE));
        when(module.getVersion()).thenReturn(new Version(0, 0, 1, true));
        when(module.getMetadata()).thenReturn(new ModuleMetadata());
        moduleRegistry.add(module);
        when(moduleManager.getRegistry()).thenReturn(moduleRegistry);
        ModuleEnvironment environment = mock(ModuleEnvironment.class);
        when(moduleManager.loadEnvironment(any(), anyBoolean())).thenReturn(environment);
        when(moduleManager.getEnvironment()).thenReturn(environment);
        registerBindButtonClasses = new ArrayList<>();
        when(environment.getTypesAnnotatedWith(eq(RegisterBindButton.class))).thenReturn(registerBindButtonClasses);
        when(environment.getTypesAnnotatedWith(eq(RegisterBindButton.class), any())).thenReturn(registerBindButtonClasses);
        registerRealBindAxisClasses = new ArrayList<>();
        when(environment.getTypesAnnotatedWith(eq(RegisterBindAxis.class))).thenReturn(registerRealBindAxisClasses);
        when(environment.getTypesAnnotatedWith(eq(RegisterBindAxis.class), any())).thenReturn(registerRealBindAxisClasses);
        when(environment.getModuleProviding(any())).thenReturn(new Name(TEST_MODULE));
        context.put(ModuleManager.class, moduleManager);
    }

    @Test
    public void testSelfRegisterOnContext() {
        assertEquals(context.get(BindsManager.class), bindsSubsystem);
    }

    @Test
    public void testUpdateBinds() {
        registerBindButtonClasses.add(TestEventButton.class);

        bindsSubsystem.updateConfigWithDefaultBinds();

        List<Input> defaultBinds = bindsSubsystem.getDefaultBindsConfig().getBinds(new SimpleUri(TEST_MODULE, "testEvent"));
        assertEquals(defaultBinds.size(), 1);
        assertEquals(defaultBinds.get(0).getType(), InputType.KEY);
        assertEquals(defaultBinds.get(0).getId(), KeyId.T);
        assertEquals(defaultBinds.get(0).getName(), Key.T.getName());
        assertEquals(defaultBinds.get(0).getDisplayName(), Key.T.getDisplayName());

        List<Input> binds = bindsSubsystem.getBindsConfig().getBinds(new SimpleUri(TEST_MODULE, "testEvent"));
        assertEquals(binds.size(), 1);
        assertEquals(binds.get(0).getType(), InputType.KEY);
        assertEquals(binds.get(0).getId(), KeyId.T);
        assertEquals(binds.get(0).getName(), Key.T.getName());
        assertEquals(binds.get(0).getDisplayName(), Key.T.getDisplayName());
    }

    @Test
    public void test() {

        ModuleEnvironment environment = mock(ModuleEnvironment.class);
        when(environment.getTypesAnnotatedWith(eq(RegisterBindButton.class), any())).thenReturn(registerBindButtonClasses);
        registerBindButtonClasses.add(TestEventButton.class);
    }

    @Test
    public void testRegisterBinds() {
        registerBindButtonClasses.add(TestEventButton.class);

        bindsSubsystem.updateConfigWithDefaultBinds();
        bindsSubsystem.registerBinds();

        BindableButton button = bindsSubsystem.getKeyBinds().get(KeyId.T);

        assertNotNull(button);
        assertEquals(button.getId(), new SimpleUri(TEST_MODULE, "testEvent"));
        assertEquals(button.getDisplayName(), "${engine-tests:menu#theTestEvent}");
    }

    //test classes, registered during tests

    @RegisterBindButton(id = "testEvent", description = "${engine-tests:menu#theTestEvent}", repeating = false, category = "tests")
    @DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.T)
    public static class TestEventButton extends BindButtonEvent {
    }
}

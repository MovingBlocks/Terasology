// Copyright 2020 The Terasology Foundation
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
import org.terasology.engine.input.BindableButton;
import org.terasology.nui.input.Input;
import org.terasology.nui.input.InputType;
import org.terasology.nui.input.Keyboard.Key;
import org.terasology.nui.input.Keyboard.KeyId;
import org.terasology.testUtil.ModuleManagerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for binds subsystem.
 * <p>
 * Using {@link org.terasology.unittest.binds.TestEventButton} for test purpose.
 * </p>
 */
public class BindsSubsystemTest {

    private static final String TEST_MODULE = "unittest";

    private BindsSubsystem bindsSubsystem;
    private BindsConfiguration bindsConfiguration;
    private Context context;

    @BeforeEach
    public void setUp() throws Exception {
        bindsSubsystem = new BindsSubsystem();
        bindsConfiguration = new BindsConfigAdapter(new BindsConfig());
        context = new ContextImpl();
        context.put(BindsConfiguration.class, bindsConfiguration);
        bindsSubsystem.preInitialise(context);
        ModuleManager moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class, moduleManager);
    }

    @Test
    public void testSelfRegisterOnContext() {
        assertEquals(context.get(BindsManager.class), bindsSubsystem);
    }

    @Test
    public void testUpdateBinds() {

        bindsSubsystem.updateConfigWithDefaultBinds();

        List<Input> defaultBinds = bindsSubsystem.getDefaultBindsConfig().getBinds(new SimpleUri(TEST_MODULE,
                "testEvent"));
        assertEquals(defaultBinds.size(), 1);
        assertEquals(defaultBinds.get(0).getType(), InputType.KEY);
        assertEquals(defaultBinds.get(0).getId(), KeyId.UNLABELED);
        assertEquals(defaultBinds.get(0).getName(), Key.UNLABELED.getName());
        assertEquals(defaultBinds.get(0).getDisplayName(), Key.UNLABELED.getDisplayName());

        List<Input> binds = bindsSubsystem.getBindsConfig().getBinds(new SimpleUri(TEST_MODULE, "testEvent"));
        assertEquals(binds.size(), 1);
        assertEquals(binds.get(0).getType(), InputType.KEY);
        assertEquals(binds.get(0).getId(), KeyId.UNLABELED);
        assertEquals(binds.get(0).getName(), Key.UNLABELED.getName());
        assertEquals(binds.get(0).getDisplayName(), Key.UNLABELED.getDisplayName());
    }

    @Test
    public void testRegisterBinds() {
        bindsSubsystem.updateConfigWithDefaultBinds();
        bindsSubsystem.registerBinds();

        BindableButton button = bindsSubsystem.getKeyBinds().get(KeyId.UNLABELED);

        assertNotNull(button);
        assertEquals(button.getId(), new SimpleUri(TEST_MODULE, "testEvent"));
        assertEquals(button.getDisplayName(), "${engine-tests:menu#theTestEvent}");
    }
}

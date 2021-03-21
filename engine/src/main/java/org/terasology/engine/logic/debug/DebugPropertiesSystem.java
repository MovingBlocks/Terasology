// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.debug;

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.nui.layouts.PropertyLayout;
import org.terasology.nui.properties.OneOfProviderFactory;
import org.terasology.nui.properties.PropertyProvider;
import org.terasology.reflection.reflect.ReflectFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 *         <br><br>
 *         Debug property editor. Usage:
 *         <br><br>
 *         context.get(DebugPropertiesSystem.class).addProperty("Model 1", model);
 *         <br><br>
 *         Ingame press F1 to see the property editor. Only annotated fields will show up.
 */
@API
@RegisterSystem(RegisterMode.CLIENT)
@Share(DebugPropertiesSystem.class)
public class DebugPropertiesSystem extends BaseComponentSystem {
    @In
    private NUIManager nuiManager;

    private PropertyLayout properties;

    @In
    private ReflectFactory reflectFactory;
    @In
    private OneOfProviderFactory providerFactory;

    @Override
    public void initialise() {
        DebugProperties debugProperties = (DebugProperties) nuiManager.getHUD().addHUDElement("engine:DebugProperties");
        debugProperties.setVisible(false);
        properties = debugProperties.getPropertyLayout();
    }

    public void addProperty(final String group, final Object o) {
        PropertyProvider propertyProvider = new PropertyProvider(reflectFactory, providerFactory);
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            properties.addProperties(group, propertyProvider.createProperties(o));
            return null;
        });
    }
}

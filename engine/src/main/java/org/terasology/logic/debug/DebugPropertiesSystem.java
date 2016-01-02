/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.debug;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.module.sandbox.API;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layouts.PropertyLayout;
import org.terasology.rendering.nui.properties.PropertyProvider;

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

    @Override
    public void initialise() {
        DebugProperties debugProperties = (DebugProperties) nuiManager.getHUD().addHUDElement("engine:DebugProperties");
        debugProperties.setVisible(false);
        properties = debugProperties.getPropertyLayout();
    }

    public void addProperty(final String group, final Object o) {
        PropertyProvider propertyProvider = new PropertyProvider();
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            properties.addProperties(group, propertyProvider.createProperties(o));
            return null;
        });
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.debug;

import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.nui.layouts.ColumnLayout;
import org.terasology.nui.layouts.PropertyLayout;

/**
 * Simple NUI HUD widget, that comes up when pressing F1 ingame. Shows a property layout panel with all registered
 * Objects.
 *
 * See DebugPropertySystem#addProperty
 *
 */
public class DebugProperties extends CoreHudWidget {
    private ColumnLayout container;
    private PropertyLayout propertyLayout;

    @Override
    public void initialise() {
        container = find("container", ColumnLayout.class);
        propertyLayout = find("properties", PropertyLayout.class);
    }

    public PropertyLayout getPropertyLayout() {
        return propertyLayout;
    }
}

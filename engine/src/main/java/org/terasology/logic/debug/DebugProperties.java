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

import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.PropertyLayout;

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
    protected void initialise() {
        container = find("container", ColumnLayout.class);
        propertyLayout = find("properties", PropertyLayout.class);
    }

    public PropertyLayout getPropertyLayout() {
        return propertyLayout;
    }
}

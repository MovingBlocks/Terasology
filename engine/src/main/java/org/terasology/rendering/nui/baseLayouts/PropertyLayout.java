/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.baseLayouts;

import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.Border;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.baseLayouts.miglayout.MigLayout;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UILabel;
import org.terasology.rendering.nui.properties.Property;
import org.terasology.rendering.nui.properties.PropertyProvider;
import org.terasology.utilities.collection.NullIterator;

import java.util.Iterator;

/**
 * Created by synopia on 03.01.14.
 */
public class PropertyLayout extends MigLayout {
    private PropertyProvider<?> propertyProvider;

    public PropertyLayout() {
    }

    public PropertyLayout(String id) {
        super(id);
    }

    public void addPropertyProvider(String label, final PropertyProvider<?> propertyProvider) {
        this.propertyProvider = propertyProvider;
        final UIButton expand = new UIButton("", "+");
        final UILabel headline = new UILabel(label);
        final MigLayout layout = new MigLayout();

        expand.subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                if( "-".equals(button.getText())) {
                    layout.clear();
                    button.setText("+");
                } else {
                    for (Property<?, ?> property : propertyProvider.getProperties()) {
                        layout.addWidget(property.getLabel(), new CCHint("newline"));
                        layout.addWidget(property.getEditor(), new CCHint());
                    }
                    button.setText("-");
                }
            }
        });
        addWidget(expand, new CCHint("newline, grow"));
        addWidget(headline, new CCHint());
        addWidget(layout, new CCHint("newline, spanx 2, grow"));
    }
}

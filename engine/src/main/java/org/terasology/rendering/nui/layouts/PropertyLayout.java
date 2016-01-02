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
package org.terasology.rendering.nui.layouts;

import com.google.common.collect.Lists;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.layouts.miglayout.MigLayout;
import org.terasology.rendering.nui.properties.Property;
import org.terasology.rendering.nui.properties.PropertyOrdering;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PropertyLayout extends MigLayout {

    private Comparator<? super Property<?, ?>> propertyComparator = PropertyOrdering.byLabel();

    public PropertyLayout() {
    }

    public PropertyLayout(String id) {
        super(id);
    }

    public void setOrdering(Comparator<? super Property<?, ?>> comparator) {
        this.propertyComparator = comparator;
    }

    /**
     * Adds a provider for properties to this layout. All properties appears in a list that may be collapsed/expanded.
     * Initially the list is expanded.
     */
    public void addProperties(String groupLabel, final Collection<Property<?, ?>> properties) {
        if (properties.size() > 0) {
            final UIButton expand = new UIButton("", "-");
            expand.setTooltip("Click to collapse");
            final UILabel headline = new UILabel(groupLabel);
            final MigLayout layout = new MigLayout();
            layout.setColConstraints("[min][fill]");
            layout.setRowConstraints("[min]");

            expand.subscribe(widget -> {
                UIButton button = (UIButton) widget;
                if ("-".equals(button.getText())) {
                    layout.clear();
                    invalidate();
                    button.setText("+");
                    button.setTooltip("Click to expand");
                } else {
                    expand(properties, layout);
                    button.setText("-");
                    button.setTooltip("Click to collapse");
                }
            });
            addWidget(expand, new CCHint("newline, w 45!, h 22!"));
            addWidget(headline, new CCHint());
            addWidget(layout, new CCHint("newline, spanx 2"));

            expand(properties, layout);
        }
    }

    private void expand(Collection<Property<?, ?>> properties, MigLayout layout) {
        List<Property<?, ?>> props = Lists.newArrayList(properties);
        Collections.sort(props, propertyComparator);
        for (Property<?, ?> property : props) {
            UILabel label = property.getLabel();
            UIWidget editor = property.getEditor();
            editor.setTooltip(property.getDescription());

            layout.addWidget(label, new CCHint("newline"));
            layout.addWidget(editor, new CCHint());
        }
        invalidate();
    }
}

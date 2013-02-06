/*
* Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.terasology.editor.ui;

import org.terasology.editor.properties.IPropertyProvider;
import org.terasology.editor.properties.Property;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PropertyPanel extends JPanel {

    private IPropertyProvider activePropertyProvider = null;

    private TitledBorder border = null;
    private String title = "";

    public PropertyPanel() {
        border = new TitledBorder("");
        setBorder(border);
    }

    public PropertyPanel(String title) {
        this();
        this.title = title;
        border.setTitle(title);
    }

    public PropertyPanel(IPropertyProvider provider) {
        this();
        setActivePropertyProvider(provider);
    }

    public void setActivePropertyProvider(IPropertyProvider provider) {
        activePropertyProvider = provider;
        onActivePropertyProviderChanged();
    }

    public void onActivePropertyProviderChanged() {
        removeAll();

        if (activePropertyProvider != null) {
            List<Property> properties = new ArrayList<Property>();
            activePropertyProvider.addPropertiesToList(properties);

            setLayout(new GridLayout(properties.size() >= 16 ? properties.size() : 16, 1));

            Iterator<Property> it = properties.iterator();
            while (it.hasNext()) {
                Property property = it.next();

                if (property.getValueType() == Float.class) {
                    add(new PropertySlider(property));
                    revalidate();
                }
            }
        }

        repaint();
    }

    public void setTitle(String title) {
        this.title = title;
        border.setTitle(title);
        revalidate();
    }
}

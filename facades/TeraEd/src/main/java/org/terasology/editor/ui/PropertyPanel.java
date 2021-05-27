// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.editor.ui;

import org.terasology.editor.properties.FloatProperty;
import org.terasology.editor.properties.Property;
import org.terasology.editor.properties.PropertyProvider;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class PropertyPanel extends JPanel {

    private static final long serialVersionUID = 6844552770055484579L;

    private PropertyProvider activePropertyProvider;

    private TitledBorder border;
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

    public PropertyPanel(PropertyProvider provider) {
        this();
        setActivePropertyProvider(provider);
    }

    public void setActivePropertyProvider(PropertyProvider provider) {
        activePropertyProvider = provider;
        onActivePropertyProviderChanged();
    }

    public void onActivePropertyProviderChanged() {
        removeAll();

        if (activePropertyProvider != null) {
            List<Property<?>> properties = activePropertyProvider.getProperties();

            setLayout(new GridLayout(properties.size() >= 16 ? properties.size() : 16, 1));

            Iterator<Property<?>> it = properties.iterator();
            while (it.hasNext()) {
                Property property = it.next();

                if (property instanceof FloatProperty) {
                    add(new PropertySlider((FloatProperty) property));
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

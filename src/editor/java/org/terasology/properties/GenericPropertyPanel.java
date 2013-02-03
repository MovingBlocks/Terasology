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
package org.terasology.properties;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GenericPropertyPanel extends JPanel {

    private PropertyProvider activePropertyProvider = null;
    private FlowLayout flowLayout;

    public GenericPropertyPanel() {
        flowLayout = new FlowLayout();
        setLayout(flowLayout);
    }

    public GenericPropertyPanel(PropertyProvider provider) {
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
            List<Property> properties = new ArrayList<Property>();
            activePropertyProvider.addPropertiesToList(properties);

            Iterator<Property> it = properties.iterator();
            while (it.hasNext()) {
                Property property = it.next();

                if (property.getValueType() == Float.class) {
                    add(new EditorPropertySlider(property));
                }
            }
        }

        revalidate();
    }
}

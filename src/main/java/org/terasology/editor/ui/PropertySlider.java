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

import org.terasology.editor.properties.Property;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PropertySlider extends JPanel implements ChangeListener {

    private JSlider slider;
    private Property activeProperty = null;

    private BorderLayout borderLayout;
    private TitledBorder titledBorder;
    private JLabel label;

    public PropertySlider() {
        titledBorder = new TitledBorder("");
        setBorder(titledBorder);

        borderLayout = new BorderLayout();
        setLayout(borderLayout);

        label = new JLabel("");

        slider = new JSlider();
        slider.setMinimum(0);
        slider.setMaximum(100);
        slider.setMinorTickSpacing(1);
        slider.setMajorTickSpacing(10);
        slider.addChangeListener(this);

        add(slider, BorderLayout.CENTER);
        add(label, BorderLayout.EAST);
    }

    public PropertySlider(Property property) {
        this();
        setActiveProperty(property);
    }

    public void setActiveProperty(Property provider) {
        activeProperty = provider;
        onActivePropertyChanged();
    }

    public void onActivePropertyChanged() {
        if (activeProperty != null) {
            titledBorder.setTitle(activeProperty.getTitle());
            if (activeProperty.getValueType() == Float.class) {
                setValue((Float) activeProperty.getValue(), activeProperty.getMinValue(), activeProperty.getMaxValue());
            }
        }
    }

    public void setValue(float value, float minValue, float maxValue) {
        int sliderValue = (int) (((value - minValue) / (maxValue - minValue)) * 100.0f);
        slider.setValue(sliderValue);
    }
    @Override
    public void stateChanged(ChangeEvent e) {
        if (activeProperty.getValueType() == Float.class) {
            float range = Math.abs(activeProperty.getMaxValue() - activeProperty.getMinValue());
            float val = (slider.getValue() / 100.0f) * range + activeProperty.getMinValue();
            activeProperty.setValue(val);
            label.setText(activeProperty.toString());
        }
    }
}

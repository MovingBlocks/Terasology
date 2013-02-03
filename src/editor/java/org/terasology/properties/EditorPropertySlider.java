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
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class EditorPropertySlider extends JPanel implements ChangeListener {

    private JSlider slider;
    private Property activeProperty = null;
    private float minValue = 0.0f;
    private float maxValue = 1.0f;
    private TitledBorder titledBorder;

    public EditorPropertySlider() {
        titledBorder = new TitledBorder("");
        setBorder(titledBorder);

        slider = new JSlider();
        slider.setMinimum(0);
        slider.setMaximum(100);
        slider.setMinorTickSpacing(1);
        slider.setMajorTickSpacing(10);
        slider.addChangeListener(this);

        add(slider);
    }

    public EditorPropertySlider(Property property) {
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
                setValue((Float) activeProperty.getValue());
            }
        }
    }

    public void setValue(float value) {
        int sliderValue = (int) (((value - minValue) / maxValue) * 100.0f);
        slider.setValue(sliderValue);
    }

    public void setMinValue(float min) {
        minValue = min;
    }

    public void setMaxValue(float max) {
        maxValue = max;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        float range = Math.abs(maxValue - minValue);

        if (activeProperty.getValueType() == Float.class) {
            float val = (slider.getValue() / 100.0f) * range + minValue;
            activeProperty.setValue(val);
        }
    }
}

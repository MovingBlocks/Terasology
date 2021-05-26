// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.editor.ui;

import org.terasology.editor.properties.FloatProperty;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class PropertySlider extends JPanel implements ChangeListener {

    private static final long serialVersionUID = 3157887601371629996L;

    private JSlider slider;
    private FloatProperty activeProperty;

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

    public PropertySlider(FloatProperty property) {
        this();
        setActiveProperty(property);
    }

    public void setActiveProperty(FloatProperty provider) {
        activeProperty = provider;
        onActivePropertyChanged();
    }

    public void onActivePropertyChanged() {
        if (activeProperty != null) {
            titledBorder.setTitle(activeProperty.getTitle());
            if (activeProperty.getValueType() == Float.class) {
                setValue(activeProperty.getValue(), activeProperty.getMinValue(), activeProperty.getMaxValue());
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

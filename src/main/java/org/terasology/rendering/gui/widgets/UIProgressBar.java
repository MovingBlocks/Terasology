/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.gui.widgets;

import org.newdawn.slick.Color;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.events.ChangedListener;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;

/**
 * A simple graphical progressBar
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIProgressBar extends UIDisplayContainer {

    //events
    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();

    //label position
    public static enum ELabelPosition {
        ABOVE, MIDDLE, UNDER
    }

    private ELabelPosition labelPosition = ELabelPosition.UNDER;
    private final float labelSpacing = 8f;

    //value
    private int value = 0;
    private int minValue = 0;
    private int maxValue = 100;

    //child elements
    private UILabel label;
    private UIProgressLine progressLine;
    private int range;

    public UIProgressBar() {
        setBackgroundImage("engine:gui_menu", new Vector2f(0f, 175f), new Vector2f(256f, 15f));

        label = new UILabel();
        label.setHorizontalAlign(EHorizontalAlign.CENTER);
        label.setColor(Color.white);
        label.setCrop(false);
        label.setVisible(true);

        progressLine = new UIProgressLine();
        progressLine.setPosition(new Vector2f(4f, 3f));
        progressLine.updateProgress(minValue, maxValue - minValue);
        progressLine.setVisible(true);

        progressLine.addDisplayElement(label);
        addDisplayElement(progressLine);

        setTextPosition(labelPosition);
        calcRange();
    }

    @Override
    public void setSize(String width, String height) {
        super.setSize(width, height);
        progressLine.setSize(new Vector2f(getSize().x - 8, getSize().y - 6));
    }

    @Override
    public void setSize(Vector2f size) {
        super.setSize(size);
        progressLine.setSize(new Vector2f(getSize().x - 8, getSize().y - 6));
    }

    private void calcRange() {
        range = maxValue - minValue;
    }

    /**
     * Get the value which the progressbar currently has.
     *
     * @return Returns the value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Set the value which the slider should have.
     *
     * @param value The value. The range of the value should be greater or equal than the minimum value and lower or equal than the maximum value.
     */
    public void setValue(int value) {
        this.value = value;

        progressLine.updateProgress(value, range);

        notifyChangedListeners();
    }

    /**
     * Get the the minimum value which will be allowed.
     *
     * @return Returns the minimum value.
     */
    public int getMin() {
        return minValue;
    }

    /**
     * Set the minimum value which will be allowed.
     *
     * @param min The minimum value. A minimum value greater than the maximum value results in unspecified behavior.
     */
    public void setMin(int min) {
        this.minValue = min;
        calcRange();
    }

    /**
     * Get the the maximum value which will be allowed.
     *
     * @return Returns the maximum value.
     */
    public int getMax() {
        return maxValue;
    }

    /**
     * Set the maximum value which will be allowed.
     *
     * @param max The maximum value. A minimum value greater than the maximum value results in unspecified behavior.
     */
    public void setMax(int max) {
        this.maxValue = max;
        calcRange();
    }

    /**
     * Get the text position of the progress bar text.
     *
     * @return Returns the text position.
     */
    public ELabelPosition getTextPosition() {
        return labelPosition;
    }

    /**
     * Set the text position of the progress bar text.
     *
     * @param position The position of the text, which can be above, under or in the middle of the progress bar.
     */
    public void setTextPosition(ELabelPosition position) {
        this.labelPosition = position;

        if (position == ELabelPosition.ABOVE) {
            label.setVerticalAlign(EVerticalAlign.TOP);
            label.setPosition(new Vector2f(0f, -label.getSize().y - labelSpacing));
        } else if (position == ELabelPosition.MIDDLE) {
            label.setVerticalAlign(EVerticalAlign.CENTER);
            label.setPosition(new Vector2f(0f, 0f));
        } else if (position == ELabelPosition.UNDER) {
            label.setVerticalAlign(EVerticalAlign.BOTTOM);
            label.setPosition(new Vector2f(0f, label.getSize().y + labelSpacing));
        }
    }

    /**
     * Get the font.
     *
     * @return Returns the font.
     */
    public Font getFont() {
        return label.getFont();
    }

    /**
     * Set the font.
     *
     * @param font The font to set.
     */
    public void setFont(Font font) {
        label.setFont(font);
    }

    /**
     * Get the text color.
     *
     * @return Returns the text color.
     */
    public Color getTextColor() {
        return label.getColor();
    }

    /**
     * Set the text color.
     *
     * @param color The color to set.
     */
    public void setTextColor(Color color) {
        label.setColor(color);
    }

    /**
     * Get the text of the progress bar.
     *
     * @return Returns the text.
     */
    public String getText() {
        return label.getText();
    }

    /**
     * Set the text of the progress bar.
     *
     * @param text The text.
     */
    public void setText(String text) {
        label.setText(text);

        layout();
    }
    
    /*
       Event listeners
    */

    private void notifyChangedListeners() {
        for (ChangedListener listener : changedListeners) {
            listener.changed(this);
        }
    }

    public void addChangedListener(ChangedListener listener) {
        changedListeners.add(listener);
    }

    public void removeChangedListener(ChangedListener listener) {
        changedListeners.remove(listener);
    }

    /**
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     */
    private static class UIProgressLine extends UIDisplayContainer {

        public UIProgressLine() {
            setCropContainer(true);
            setBackgroundImage("engine:gui_menu", new Vector2f(0f, 190f), new Vector2f(248f, 9f));
        }

        public void updateProgress(int value, int range) {
            setCropMargin(new Vector4f(0f, -(getSize().x - getSize().x * ((float) value / (float) range)), 0f, 0f));
        }
    }
}


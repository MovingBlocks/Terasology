/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.components;

import java.util.ArrayList;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

/**
 * A simple Slider.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UISlider extends UIDisplayContainer {
    
    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
    private final UIText label;
    private final UIGraphicsElement slider;
    
    private int currentValue;
    private int minValue;
    private int maxValue;
    private int range;

    /**
     * Creates a slider.
     * @param size The size of the slider.
     * @param min The minimum value the slider can have.
     * @param max The maximum value the slider can have.
     */
    public UISlider(Vector2f size, int min, int max) {
        setSize(size);
        this.minValue = min;
        this.maxValue = max;
        currentValue = Integer.MAX_VALUE;

        setBackgroundImage("engine:gui_menu", new Vector2f(0f, 0f), new Vector2f(256f, 30f));
        
        addMouseMoveListener(new MouseMoveListener() {    
            @Override
            public void leave(UIDisplayElement element) {
                setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }
            
            @Override
            public void hover(UIDisplayElement element) {

            }
            
            @Override
            public void enter(UIDisplayElement element) {
                AudioManager.play(new AssetUri(AssetType.SOUND, "engine:click"), 1.0f);
                setBackgroundImage(new Vector2f(0f, 30f), null);
            }

            @Override
            public void move(UIDisplayElement element) {
                if (isFocused()) {
                    changeSlider(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()).x);
                }
            }
        });
        
        addMouseButtonListener(new MouseButtonListener() {            
            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
                setBackgroundImage(new Vector2f(0f, 0f), null);
            }
            
            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                if (intersect)
                    changeSlider(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()).x);
            }
            
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }
        });
        
        slider = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));
        slider.setParent(this);
        slider.setVisible(true);
        slider.setPosition(new Vector2f(0, 0));
        slider.setTextureOrigin(new Vector2f(0f, 60f));
        slider.setTextureSize(new Vector2f(256f, 30f));
        slider.setSize(new Vector2f(16f, getSize().y));
        slider.addMouseButtonListener(new MouseButtonListener() {                                    
            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
                setFocus(null);
            }
            
            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                if (!isFocused() && intersect) {
                    setFocus(UISlider.this);
                }
            }
            
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
                
            }
        });
        
        label = new UIText("");
        label.setVisible(true);
        
        addDisplayElement(slider);
        addDisplayElement(label);
        
        calcRange();
    }
    
    @Override
    public void layout() {
        super.layout();
        
        if (label != null) {
            label.setPosition(new Vector2f(getSize().x / 2 - label.getTextWidth() / 2, getSize().y / 2 - label.getTextHeight() / 2));
        }
    }
    
    /**
     * Changes the slider position based on the value.
     * @param value The value the slider should have.
     */
    private void changeSlider(int value)
    {
        if (value < minValue) {
            value = minValue;
        }
        else if (value > maxValue) {
            value = maxValue;
        }
        
        slider.getPosition().set(valueToPos(value), 0);
        
        if (value != currentValue)
        {
            currentValue = value;
            
            notifyChangedListeners();
        }
    }

    /**
     * Changes the slider position based on the mouse position.
     * @param pos The position of the mouse in x direction.
     */
    private void changeSlider(float pos) {
        float sliderPos = pos - getPosition().x - slider.getSize().x / 2;
        if (sliderPos < 0)
        {
            sliderPos = 0;
        }
        else if (sliderPos > (getSize().x - slider.getSize().x))
        {
            sliderPos = getSize().x - slider.getSize().x;
        }
        
        slider.getPosition().set(sliderPos, 0);
        
        int newValue = posToValue(sliderPos);
        if (newValue != currentValue)
        {
            currentValue = newValue;
            
            notifyChangedListeners();
        }
    }

    /**
     * Calculate slider value from position.
     * @param pos The position of the slider.
     * @return Returns the value at the given position.
     */
    private int posToValue(float pos) {
        int value = Math.round(pos / ((getSize().x - slider.getSize().x) / (float)range));
        
        value += minValue;
        
        if (value < minValue) {
            value = minValue;
        }
        else if (value > maxValue) {
            value = maxValue;
        }
        
        return value;
    }
    
    /**
     * Calculate slider position from value.
     * @param value The value of the slider.
     * @return Returns the position at the given value.
     */
    private float valueToPos(int value) {
        if (minValue < 0)
        {
            value += -minValue;
        }
        else
        {
            value -= minValue;
        }
        
        float pos = value * ((getSize().x - slider.getSize().x) / (float)range);
        if (pos < 0)
        {
            pos = 0;
        }
        else if (pos > getSize().x)
        {
            pos = getSize().x;
        }
        
        return pos;
    }
    
    private void calcRange() {
        range = maxValue - minValue;
    }
    
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
    
    public int getValue() {
        return currentValue;
    }

    public void setValue(int value) {
        changeSlider(value);
    }    
    
    public int getMax() {
        return maxValue;
    }

    public void setMax(int max) {
        this.maxValue = max;
        calcRange();
    }

    public int getMin() {
        return minValue;
    }

    public void setMin(int min) {
        this.minValue = min;
        calcRange();
    }
    
    public void setText(String text) {
        label.setText(text);
        
        layout();
    }
    
    public String getText() {
        return label.getText();
    }
}

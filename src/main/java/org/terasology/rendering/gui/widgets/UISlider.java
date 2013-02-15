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
package org.terasology.rendering.gui.widgets;

import java.util.ArrayList;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

/**
 * A simple Slider.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UISlider extends UIDisplayContainer {
    
    //events
    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
    
    //value
    private int currentValue;
    private int minValue;
    private int maxValue;
    private int range;
    
    //child elements
    private final UILabel label;
    private final UIImage slider;

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
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
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
                setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
                setFocus(null);
            }
            
            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                if (intersect) {
                    changeSlider(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()).x);
                    if (!isFocused()) {
                        setFocus(UISlider.this);
                    }
                }
            }
            
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }
        });
        
        slider = new UIImage(Assets.getTexture("engine:gui_menu"));
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
        
        label = new UILabel("");
        label.setHorizontalAlign(EHorizontalAlign.CENTER);
        label.setVerticalAlign(EVerticalAlign.CENTER);
        label.setVisible(true);
        
        addDisplayElement(slider);
        addDisplayElement(label);
        
        calcRange();
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
        
        slider.setPosition(new Vector2f(valueToPos(value), 0));
        
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
        
        slider.setPosition(new Vector2f(sliderPos, 0f));
        
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
    
    /**
     * Get the value which the slider currently has.
     * @return Returns the value.
     */
    public int getValue() {
        return currentValue;
    }
    
    /**
     * Set the value which the slider should have.
     * @param value The value. The range of the value should be greater or equal than the minimum value and lower or equal than the maximum value.
     */
    public void setValue(int value) {
        changeSlider(value);
    }   
    
    /**
     * Get the the maximum value which will be allowed.
     * @return Returns the maximum value.
     */
    public int getMax() {
        return maxValue;
    }

    /**
     * Set the maximum value which will be allowed.
     * @param max The maximum value. A minimum value greater than the maximum value results in unspecified behavior.
     */
    public void setMax(int max) {
        this.maxValue = max;
        calcRange();
    }
    
    /**
     * Get the the minimum value which will be allowed.
     * @return Returns the minimum value.
     */
    public int getMin() {
        return minValue;
    }
    
    /**
     * Set the minimum value which will be allowed.
     * @param min The minimum value. A minimum value greater than the maximum value results in unspecified behavior.
     */
    public void setMin(int min) {
        this.minValue = min;
        calcRange();
    }
    
    /**
     * Set the text of the slider label.
     * @param text The text.
     */
    public void setText(String text) {
        label.setText(text);
        
        layout();
    }
    
    /**
     * Get the text of the slider label.
     * @return Returns the text.
     */
    public String getText() {
        return label.getText();
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
}

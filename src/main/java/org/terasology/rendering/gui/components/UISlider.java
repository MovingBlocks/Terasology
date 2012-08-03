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
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.framework.events.IChangedListener;

/**
 * A simple Slider.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UISlider extends UIDisplayContainer {
	
    private final ArrayList<IChangedListener> _changedListeners = new ArrayList<IChangedListener>();
	private final UIText _label;
	private final UIGraphicsElement _slider;
	
	private int _currentValue;
	private int _min;
	private int _max;
	private int _range;
	
	private static UISlider _moveActive = null;

	public UISlider(Vector2f size, int min, int max) {
        setSize(size);
        _min = min;
        _max = max;
        _currentValue = Integer.MAX_VALUE;
        
        setClassStyle("slider", "background-image: engine:gui_menu 256/512 30/512 0 0");
        setClassStyle("slider-mouseover", "background-image: engine:gui_menu 256/512 30/512 0 30/512");
        setClassStyle("slider");
        
        _slider = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));
        _slider.setParent(this);
        _slider.setVisible(true);
        _slider.setPosition(new Vector2f(0, 0));
        _slider.getTextureOrigin().set(0f, 60f / 512f);
        _slider.getTextureSize().set(new Vector2f(256f / 512f, 30f / 512f));
        _slider.setSize(new Vector2f(16f, getSize().y));
        
        _label = new UIText("");
        _label.setVisible(true);
        
        addDisplayElement(_slider);
        addDisplayElement(_label);
        
        calcRange();
	}

	public void update() {
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
        
        updateSlider(mousePos);

        if (intersects(mousePos)) {
            setClassStyle("slider-mouseover");
            if (_mouseDown && _moveActive == null)
            {
            	changeSlider(mousePos.x);
            }
        } 
        else {
            setClassStyle("slider");
        }

        // Position the label in the center of the button
        _label.setPosition(new Vector2f(getSize().x / 2 - _label.getTextWidth() / 2, getSize().y / 2 - _label.getTextHeight() / 2));
        
        super.update();
    }
    
    private void updateSlider(Vector2f mousePos) {        
		if (_slider.intersects(mousePos)) {
			if (_mouseDown && _moveActive == null) {
				_moveActive = this;
			}
		}

		if (_mouseUp) {
			_moveActive = null;
			_mouseDown = false;
			_mouseUp = false;
		}

		if (_moveActive == this) {
			changeSlider(mousePos.x);
		}
	}
	
	private void changeSlider(int value)
	{
		if (value < _min) {
			value = _min;
		}
		else if (value > _max) {
			value = _max;
		}
		
		_slider.getPosition().set(valueToPos(value), 0);
		
		if (value != _currentValue)
		{
			_currentValue = value;
			
			notifyChangedListeners();
		}
	}

	private void changeSlider(float pos) {
		float sliderPos = pos - getPosition().x - _slider.getSize().x / 2;
		if (sliderPos < 0)
		{
			sliderPos = 0;
		}
		else if (sliderPos > (getSize().x - _slider.getSize().x))
		{
			sliderPos = getSize().x - _slider.getSize().x;
		}
		
		_slider.getPosition().set(sliderPos, 0);
		
		int newValue = posToValue(sliderPos);
		if (newValue != _currentValue)
		{
			_currentValue = newValue;
			
			notifyChangedListeners();
		}
	}

	/**
	 * Calculate slider value from position.
	 * @param pos The position of the slider.
	 * @return Returns the value at the given position.
	 */
	private int posToValue(float pos) {
		int value = Math.round(pos / ((getSize().x - _slider.getSize().x) / (float)_range));
		
		value += _min;
		
		if (value < _min) {
			value = _min;
		}
		else if (value > _max) {
			value = _max;
		}
		
		return value;
	}
	
	/**
	 * Calculate slider position from value.
	 * @param value The value of the slider.
	 * @return Returns the position at the given value.
	 */
	private float valueToPos(int value) {
		if (_min < 0)
		{
			value += -_min;
		}
		else
		{
			value -= _min;
		}
		
		float pos = value * ((getSize().x - _slider.getSize().x) / (float)_range);
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
		_range = _max - _min;
	}
	
	private void notifyChangedListeners() {
		_label.setText(String.valueOf(_currentValue));
		for (IChangedListener listener : _changedListeners) {
			listener.changed(this);
		}
	}

	public void addChangedListener(IChangedListener listener) {
        _changedListeners.add(listener);
    }

    public void removeChangedListener(IChangedListener listener) {
    	_changedListeners.remove(listener);
    }
	
    public int getValue() {
		return _currentValue;
	}

	public void setValue(int value) {
		changeSlider(value);
	}    
	
	public int getMax() {
		return _max;
	}

	public void setMax(int max) {
		_max = max;
		calcRange();
	}

	public int getMin() {
		return _min;
	}

	public void setMin(int min) {
		_min = min;
		calcRange();
	}
	
	public void setText(String text) {
		_label.setText(text);
	}
	
	public String getText() {
		return _label.getText();
	}
}

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

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

/**
 * A simple graphical progressBar
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.23
 */
public class UIProgressBar extends UIDisplayContainer {

    private UIText         _label;
    private int            _value;
    private UIProgressLine _progressLine;
    
    private class UIProgressLine extends UIDisplayContainer {
        public UIProgressLine(Vector2f size){
            setSize(size);
            setCrop(true);
            setBackgroundImage("engine:gui_menu");
            setBackgroundImageSource(new Vector2f(0f, 190f), new Vector2f(248f, 9f));
            setBackgroundImageTarget(new Vector2f(0, 0), getSize());
        }
        
        public void updateProgress() {
        	Vector4f size = new Vector4f(0f, -(getSize().x - getSize().x * (_value / 100f)), 0f, 0f);
        	setCropMargin(size);
        }
    }

    public UIProgressBar() {

        _value = 50;
        setSize(new Vector2f(256f, 15f));
        setBackgroundImage("engine:gui_menu");
        setBackgroundImageSource(new Vector2f(0f, 175f), new Vector2f(256f, 15f));
        
        _label = new UIText();
        _label.setVisible(true);
        _label.setColor(Color.black);
        _label.setCroped(false);
        _label.setPosition(new Vector2f(getPosition().x, 20f));
        _label.setColor(Color.white);

        _progressLine = new UIProgressLine(new Vector2f(248f, 9f));
        _progressLine.setVisible(true);
        _progressLine.setPosition(new Vector2f(4f,3f));
        _progressLine.updateProgress();

        addDisplayElement(_label);
        addDisplayElement(_progressLine);
    }

    @Override
    public void layout() {
    	super.layout();
    	
    	if (_label != null)
    		_label.getPosition().x = (getSize().x - _label.getTextWidth())/2;
    }

    public int getValue() {
        return _value;
    }

    public void setValue(int value) {
        _value = value;
        
        _progressLine.updateProgress();
    }

    public void setTextColor(Color color) {
        _label.setColor(color);
    }
    
    public void setText(String text){
        _label.setText(text);
        
        layout();
    }
}


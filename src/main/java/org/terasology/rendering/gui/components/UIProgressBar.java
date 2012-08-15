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

    public UIProgressBar(Vector2f size) {

        _value = 50;
        setSize(size);
        setCrop(true);
        setStyle("background-image", "engine:gui_menu 256/512 15/512 0 175/512");
        setCropMargin(new Vector4f(0f, -(getSize().x - getSize().x * (_value / 100f)), 0f, 0f));

        _label = new UIText();
        _label.setVisible(true);
        _label.setColor(Color.black);
        _label.setCroped(false);
        _label.setPosition(new Vector2f(getPosition().x, 20f));
        _label.setColor(Color.white);

        _progressLine = new UIProgressLine(new Vector2f(248f, 9f));
        _progressLine.setVisible(true);
        _progressLine.setPosition(new Vector2f(4f,3f));

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
        setCropMargin(new Vector4f(0f, -(getSize().x - getSize().x * (_value/100f)), 0f, 0f));
    }

    public void setTextColor(Color color) {
        _label.setColor(color);
    }
    
    public void setText(String text){
        _label.setText(text);
        
        layout();
    }

    private class UIProgressLine extends UIDisplayContainer {
        public UIProgressLine(Vector2f size){
            setSize(size);
            setStyle("background-image", "engine:gui_menu 248/512 9/512 0 190/512");
        }
    }

}


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

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

/**
 * A simple graphical progressBar
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.23
 * 
 * TODO offer changed listener?
 */
public class UIProgressBar extends UIDisplayContainer {

    private UILabel         label;
    private int            value;
    private UIProgressLine progressLine;
    
    private class UIProgressLine extends UIDisplayContainer {
        public UIProgressLine(Vector2f size){
            setSize(size);
            setCropContainer(true);
            setCrop(true);
            setBackgroundImage("engine:gui_menu", new Vector2f(0f, 190f), new Vector2f(248f, 9f));
        }
        
        public void updateProgress() {
            Vector4f size = new Vector4f(0f, -(getSize().x - getSize().x * (value / 100f)), 0f, 0f);
            setCropMargin(size);
        }
    }

    public UIProgressBar() {

        value = 0;
        setSize(new Vector2f(256f, 15f));
        setBackgroundImage("engine:gui_menu", new Vector2f(0f, 175f), new Vector2f(256f, 15f));
        
        label = new UILabel();
        label.setColor(Color.black);
        label.setCrop(false);
        label.setHorizontalAlign(EHorizontalAlign.CENTER);
        label.setPosition(new Vector2f(0f, getSize().y + 2));
        label.setColor(Color.white);
        label.setVisible(true);

        progressLine = new UIProgressLine(new Vector2f(248f, 9f));
        progressLine.setVisible(true);
        progressLine.setPosition(new Vector2f(4f,3f));
        progressLine.updateProgress();

        addDisplayElement(label);
        addDisplayElement(progressLine);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        
        progressLine.updateProgress();
    }

    public void setTextColor(Color color) {
        label.setColor(color);
    }
    
    public void setText(String text){
        label.setText(text);
        
        layout();
    }
}


package org.terasology.rendering.gui.components;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

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
    public void update(){
        setCropMargin(new Vector4f(0f, -(getSize().x - getSize().x * (_value/100f)), 0f, 0f));
        float posX = (getSize().x - _label.getTextWidth())/2;
        _label.getPosition().x = posX;
    }

    public int getValue() {
        return _value;
    }

    public void setValue(int value) {
        _value = value;
    }

    public void setTextColor(Color color) {
        _label.setColor(color);
    }
    
    public void setText(String text){
        _label.setText(text);
    }

    private class UIProgressLine extends UIDisplayContainer {
        public UIProgressLine(Vector2f size){
            setSize(size);
            setStyle("background-image", "engine:gui_menu 248/512 9/512 0 190/512");
        }
    }

}


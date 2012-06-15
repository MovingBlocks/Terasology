package org.terasology.rendering.gui.framework.style;

import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;

/*
 * Styles for elements
 * ToDo Create manager of styles
 * ToDo Load styles from an external file
 */

public class UIStyle extends UIDisplayContainer {

    //background
    private UIPropertyBackground _background = new UIPropertyBackground();
    //borders
    UIPropertyBorder _border = new UIPropertyBorder();

    public UIStyle(Vector2f size) {
        setSize(size);
        _background.setSize(getSize());
        _border.setSize(getSize());
        addDisplayElement(_background);
        addDisplayElement(_border);
    }


    public void parse(String property, String value) {
        if (property.indexOf("border") >= 0) {
            _border.parse(property, value);
        } else if (property.indexOf("background") >= 0) {
            _background.parse(property, value);
        }
    }

    public void parse(String value) {
        String[] parseData = value.split(":", 2);
        parseData[1] = validateString(parseData[1].trim());
        parse(parseData[0], parseData[1]);
    }

    private String validateString(String value) {
        value = value.trim();
        value = value.replaceAll("[ ]+", " ");
        return value;
    }

}

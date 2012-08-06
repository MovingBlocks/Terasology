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
    private UIPropertyBackground _background;
    //borders
    UIPropertyBorder _border;

    public UIStyle(Vector2f size) {
        _border = new UIPropertyBorder();
        _background = new UIPropertyBackground();
        setSize(size);
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

    public void setSize(Vector2f size){
        super.setSize(size);
        _background.setSize(size);
        _border.setSize(size);
    }

}

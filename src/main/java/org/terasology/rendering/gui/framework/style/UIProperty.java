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

import org.terasology.rendering.gui.framework.UIDisplayElement;

import javax.vecmath.Vector4f;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UIProperty extends UIDisplayElement {
    private Logger logger = Logger.getLogger(getClass().getName());

    public void render() {
    }

    ;

    public void update() {
    }

    ;

    public void parse(String property, String value) {
    }

    ;

    //Temporary. Value can have 2 arguments. For example 2/150
    public float parseFloat(String value) {
        if (value.indexOf("/") < 0) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Bad float value - " + value);
                return 0f;
            }
        } else {
            try {
                float arg1 = Float.parseFloat(value.split("/")[0]);
                float arg2 = Float.parseFloat(value.split("/")[1]);
                return arg1 / arg2;
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage());
                return 0f;
            }
        }
    }

    public Vector4f hexToRGB(String hexColor) {
        try {
            return new Vector4f((float) Integer.parseInt(checkHex(hexColor).substring(0, 2), 16) / 255f,
                    (float) Integer.parseInt(checkHex(hexColor).substring(2, 4), 16) / 255f,
                    (float) Integer.parseInt(checkHex(hexColor).substring(4, 6), 16) / 255f,
                    1.0f);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Bad Hex color value - " + hexColor);
            return new Vector4f();
        }
    }

    public String checkHex(String h) {
        return (h.charAt(0) == '#') ? h.substring(1, 7) : h;
    }
}

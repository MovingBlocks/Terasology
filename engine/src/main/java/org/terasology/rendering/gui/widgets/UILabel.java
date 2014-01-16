/*
 * Copyright 2013 MovingBlocks
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

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.internal.ColorUtil;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glDisable;

/**
 * Simple text element supporting text shadowing.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UILabel extends UIDisplayContainer {

    protected StringBuilder text = new StringBuilder();

    private final List<ChangedListener> changedListeners = new ArrayList<>();

    //wrapping
    private final List<Integer> wrapPosition = new ArrayList<Integer>();
    private boolean isWrap;

    //font
    private Font font = Assets.getFont("engine:default");
    private Color color = new Color(Color.white);

    //shadow
    private boolean enableShadow;
    private Color shadowColor = new Color(Color.black);
    private final Vector2f shadowOffset = new Vector2f(1, 0);

    //other
    private Vector2f lastSize = new Vector2f(0f, 0f);
    private Vector4f margin = new Vector4f(0f, 0f, 0f, 0f);


    public UILabel() {
        super();
        setText("");
    }

    public UILabel(String text) {
        setText(text);
    }

    public UILabel(String text, Color shadowColor) {
        setText(text);
        this.enableShadow = true;
        this.shadowColor = shadowColor;
    }

    public void render() {
        super.render();

        PerformanceMonitor.startActivity("Render UIText");

        CoreRegistry.get(ShaderManager.class).enableDefaultTextured();

        if (enableShadow) {
            font.drawString(TeraMath.floorToInt(shadowOffset.x + margin.w), TeraMath.floorToInt(1 + shadowOffset.y + margin.x), text.toString(), shadowColor);
        }

        font.drawString(TeraMath.floorToInt(margin.w), TeraMath.floorToInt(margin.x), text.toString(), color);

        // TODO: Also ugly..
        glDisable(GL11.GL_TEXTURE_2D);

        PerformanceMonitor.endActivity();
    }

    /**
     * Calculate the width of the given string.
     *
     * @param string The string to calculate the width.
     * @return Returns the width of the given string.
     */
    private int calcTextWidth(String string) {
        return font.getWidth(string);
    }

    /**
     * Calculate the height of the given string.
     *
     * @param string The string to calculate the height.
     * @return Returns the height of the given string.
     */
    private int calcTextHeight(String string) {
        return font.getHeight(string);
    }

    @Override
    public void layout() {
        super.layout();

        //so the wrapped label will calculate the wrap position again if the parent or display was resized
        if (isWrap) {
            if (positionType == EPositionType.RELATIVE && getParent() != null) {
                if (lastSize.x != getParent().getSize().x || lastSize.y != getParent().getSize().y) {
                    lastSize.x = getParent().getSize().x;
                    lastSize.y = getParent().getSize().y;
                    setText(getText());
                }
            } else if (positionType == EPositionType.ABSOLUTE) {
                if (lastSize.x != Display.getWidth() || lastSize.y != Display.getHeight()) {
                    lastSize.x = Display.getWidth();
                    lastSize.y = Display.getHeight();
                    setText(getText());
                }
            }
        }
    }

    /**
     * Wraps the string to the with of the wrapWidth.
     *
     * @param string The string to wrap.
     * @return Returns the wrapped string.
     */
    private String wrapText(String string) {
        //wrap string
        if (isWrap()) {
            int lastSpace = 0;
            int lastWrap = 0;
            StringBuilder wrapText = new StringBuilder(string + " ");

            wrapPosition.clear();

            float wrapWidth = getSize().x - margin.y - margin.w;

            if (wrapWidth > 0) {
                //loop through whole string
                for (int i = 0; i < wrapText.length(); i++) {

                    //check if character is a space -> string can only be wrapped at spaces
                    if (wrapText.charAt(i) == ' ') {
                        //check if the string (from the beginning of the new line) is bigger than the container width
                        if (calcTextWidth(wrapText.substring(lastWrap, i)) > wrapWidth) {
                            //than wrap the string at the previous space
                            wrapText.insert(lastSpace + 1, '\n');
                            wrapPosition.add(new Integer(lastSpace + 1));

                            lastWrap = lastSpace + 1;
                        }

                        lastSpace = i;
                    } else if (wrapText.charAt(i) == '\n') {
                        lastSpace = i;
                        lastWrap = i;
                    }
                }
            }

            wrapText.replace(wrapText.length() - 1, wrapText.length(), "");

            return wrapText.toString();
        } else {
            //no wrap
            return string;
        }
    }

    /**
     * Get the displayed text of the label.
     *
     * @return Returns the text.
     */
    public String getText() {
        StringBuilder str = new StringBuilder(text.toString());
        for (int i = 0; i < wrapPosition.size(); i++) {
            str.replace(wrapPosition.get(i) - i, wrapPosition.get(i) + 1 - i, "");
        }

        return str.toString();
    }

    /**
     * Set the text of the label.
     *
     * @param text The text to set.
     */
    public void setText(String text) {
        this.text = new StringBuilder(wrapText(text));

        if (isWrap) {
            String unitX = "px";
            if (unitSizeX == EUnitType.PERCENTAGE) {
                unitX = "%";
            }

            setSize(sizeOriginal.x + unitX, (calcTextHeight(this.text.toString()) + margin.x + margin.z) + "px");
        } else {
            setSize(new Vector2f(calcTextWidth(this.text.toString()) + margin.y + margin.w, calcTextHeight(this.text.toString()) + margin.x + margin.z));
        }

        notifyChangedListeners();
    }

    /**
     * Append a string to the current displayed text of the label.
     *
     * @param string The string to append.
     */
    public void appendText(String string) {
        setText(getText() + string);
    }

    /**
     * Insert a text at a specific position into the current displayed text of the label.
     *
     * @param offset The offset, where to insert the text at.
     * @param string The text to insert.
     */
    public void insertText(int offset, String string) {
        StringBuilder builder = new StringBuilder(getText());
        builder.insert(offset, string);

        setText(builder.toString());
    }

    /**
     * Replace a string defined by its start and end index.
     *
     * @param start  The start index.
     * @param end    The end index.
     * @param string The text to replace with.
     */
    public void replaceText(int start, int end, String string) {
        StringBuilder builder = new StringBuilder(getText());
        builder.replace(start, end, string);

        setText(builder.toString());
    }

    /**
     * Get the text color.
     *
     * @return Returns the text color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the text color.
     *
     * @param color The color to set.
     */
    public void setColor(Color color) {
        this.color = color;
    }


    /**
     * Set the text color.
     *
     * @param color The color to set in hex format.
     */
    public void setColor(String colorString) {
        this.color = ColorUtil.getColorForColorHexString(colorString);
    }

    /**
     * Get the shadow color.
     *
     * @return Returns the shadow color.
     */
    public Color getTextShadowColor() {
        return shadowColor;
    }

    /**
     * Set the shadow color.
     *
     * @param value The shadow color to set.
     */
    public void setTextShadowColor(Color value) {
        this.shadowColor = value;
    }

    /**
     * Check whether the text has a shadow.
     *
     * @return Returns true if the text has a shadow.
     */
    public boolean isTextShadow() {
        return enableShadow;
    }

    /**
     * Set whether the text has a color.
     *
     * @param enable True to enable the shadow of the text.
     */
    public void setTextShadow(boolean enable) {
        this.enableShadow = enable;
    }

    /**
     * Get the font.
     *
     * @return Returns the font.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Set the font.
     *
     * @param font The font to set.
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Get the margin which will be around the text.
     *
     * @return Returns the margin.
     */
    public Vector4f getMargin() {
        return margin;
    }

    /**
     * Set the margin which will be around the text.
     *
     * @param margin The margin.
     */
    public void setMargin(Vector4f margin) {
        this.margin = margin;
    }

    /**
     * Check whether the text will be wrapped. The width where the text will be wrapped can be set by using setWrapWidth().
     *
     * @return Returns Returns true if the text will be wrapped.
     */
    public boolean isWrap() {
        return isWrap;
    }

    /**
     * Set whether the text will be wrapped. The width where the text will be wrapped can be set by using setWrapWidth().
     *
     * @param value True to enable text wrapping.
     */
    public void setWrap(boolean value) {
        this.isWrap = value;
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

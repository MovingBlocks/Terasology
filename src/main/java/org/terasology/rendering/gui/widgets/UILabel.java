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

import static org.lwjgl.opengl.GL11.glDisable;

import java.util.ArrayList;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.terasology.logic.manager.FontManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.events.ChangedListener;

/**
 * Simple text element supporting text shadowing.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UILabel extends UIDisplayContainer {

    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
    
    protected StringBuilder text = new StringBuilder();
    
    //font
    private AngelCodeFont font = FontManager.getInstance().getFont("default");
    private Color color = new Color(Color.white);
    
    //shadow
    private boolean enableShadow = true;
    private Color shadowColor = new Color(Color.black);
    private final Vector2f shadowOffset = new Vector2f(1, 0);
    
    // TODO HACK
    private Texture workaroundTexture = new TextureImpl("abc", 0, 0);

    public UILabel() {
        super();
        setText("");
    }

    public UILabel(String text) {
        setText(text);
    }

    public void render() {
        super.render();
        
        PerformanceMonitor.startActivity("Render UIText");

        ShaderManager.getInstance().enableDefaultTextured();

        // TODO HACK: Workaround because the internal Slick texture mechanism is never used
        workaroundTexture.bind();

        if (enableShadow) {
            font.drawString(shadowOffset.x, shadowOffset.y, text.toString(), shadowColor);
        }
        
        font.drawString(0, 0, text.toString(), color);

        // TODO: Also ugly..
        glDisable(GL11.GL_TEXTURE_2D);

        PerformanceMonitor.endActivity();
    }

    private int getTextHeight() {
        if (text.toString().trim().length() == 0) {
            return font.getHeight("t");
        } else {
            return font.getHeight(text.toString());
        }
    }

    private int getTextWidth() {
        return font.getWidth(text.toString());
    }

    /**
     * Get the displayed text of the label.
     * @return Returns the text.
     */
    public String getText() {
        return text.toString();
    }

    /**
     * Set the text of the label.
     * @param text The text to set.
     */
    public void setText(String text) {
        this.text = new StringBuilder(text);
        setSize(new Vector2f(getTextWidth(), getTextHeight()));
        
        notifyChangedListeners();
    }
    
    /**
     * Append a text to the current displayed text of the label.
     * @param text The text to append.
     */
    public void appendText(String text) {
        this.text.append(text);
        setSize(new Vector2f(getTextWidth(), getTextHeight()));
        
        notifyChangedListeners();
    }
    
    /**
     * Insert a text at a specific position into the current displayed text of the label.
     * @param offset The offset, where to insert the text at.
     * @param text The text to insert.
     */
    public void insertText(int offset, String text) {
        this.text.insert(offset, text);
        setSize(new Vector2f(getTextWidth(), getTextHeight()));
        
        notifyChangedListeners();
    }
    
    /**
     * Replace a string defined by its start and end index. 
     * @param start The start index.
     * @param end The end index.
     * @param text The text to replace with.
     */
    public void replaceText(int start, int end, String text) {
        this.text.replace(start, end, text);
        setSize(new Vector2f(getTextWidth(), getTextHeight()));
        
        notifyChangedListeners();
    }

    /**
     * Get the text color.
     * @return Returns the text color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the text color.
     * @param color The color to set.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Get the shadow color.
     * @return Returns the shadow color.
     */
    public Color getShadowColor() {
        return shadowColor;
    }

    /**
     * Set the shadow color.
     * @param shadowColor The shadow color to set.
     */
    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
    }

    /**
     * Check whether the text has a shadow.
     * @return Returns true if the text has a shadow.
     */
    public boolean isEnableShadow() {
        return enableShadow;
    }
    
    /**
     * Set whether the text has a color.
     * @param enable True to enable the shadow of the text.
     */
    public void setEnableShadow(boolean enable) {
        this.enableShadow = enable;
    }

    /**
     * Get the font.
     * @return Returns the font.
     */
    public AngelCodeFont getFont() {
        return font;
    }

    /**
     * Set the font.
     * @param font The font to set.
     */
    public void setFont(AngelCodeFont font) {
        this.font = font;
    }
    
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

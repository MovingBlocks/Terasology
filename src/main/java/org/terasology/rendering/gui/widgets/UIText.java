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
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;

/**
 * Simple text element supporting text shadowing.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIText extends UIDisplayElement {

    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
    protected String text = "";

    private Color shadowColor = new Color(Color.black);
    private Color color = new Color(Color.white);

    private AngelCodeFont font = FontManager.getInstance().getFont("default");
    private boolean shadowed = true;

    // TODO HACK
    private Texture _workaroundTexture = new TextureImpl("abc", 0, 0);

    private final Vector2f _shadowOffset = new Vector2f(1, 0);

    public UIText() {
        super();
    }

    public UIText(String text) {
        setText(text);
    }

    public void render() {
        PerformanceMonitor.startActivity("Render UIText");

        ShaderManager.getInstance().enableDefaultTextured();

        // TODO HACK: Workaround because the internal Slick texture mechanism is never used
        _workaroundTexture.bind();

        if (shadowed)
            font.drawString(_shadowOffset.x, _shadowOffset.y, text, shadowColor);

        font.drawString(0, 0, text, color);

        // TODO: Also ugly..
        glDisable(GL11.GL_TEXTURE_2D);

        PerformanceMonitor.endActivity();
    }

    @Override
    public void update() {

    }
    
    private void notifyChangedListeners() {
        for (ChangedListener listener : changedListeners) {
            listener.changed(this);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        setSize(new Vector2f(getTextWidth(), getTextHeight()));
        notifyChangedListeners();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
    }

    public void setShadowed(boolean shadowed) {
        this.shadowed = shadowed;
    }

    public boolean isShadowed() {
        return shadowed;
    }

    public AngelCodeFont getFont() {
        return font;
    }

    public void setFont(AngelCodeFont font) {
        this.font = font;
    }

    public int getTextHeight() {
        return font.getHeight(text);
    }

    public int getTextWidth() {
        return font.getWidth(text);
    }

    public void addChangedListener(ChangedListener listener) {
        changedListeners.add(listener);
    }

    public void removeChangedListener(ChangedListener listener) {
        changedListeners.remove(listener);
    }
}

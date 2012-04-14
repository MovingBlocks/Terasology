/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.terasology.logic.manager.FontManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.gui.framework.UIDisplayElement;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.glDisable;

/**
 * Simple text element supporting text shadowing.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIText extends UIDisplayElement {

    private String _text = "";

    private Color _shadowColor = new Color(Color.black);
    private Color _color = new Color(Color.white);

    private AngelCodeFont _font = FontManager.getInstance().getFont("default");
    private boolean _shadowed = true;

    // TODO HACK
    private Texture _workaroundTexture = new TextureImpl("abc", 0, 0);

    private final Vector2f _shadowOffset = new Vector2f(-1, 0);

    public UIText() {
        super();
    }

    public UIText(String text) {
        super();

        _text = text;
    }

    public UIText(Vector2f position) {
        super(position);
    }

    public void render() {
        PerformanceMonitor.startActivity("Render UIText");

        ShaderManager.getInstance().enableDefaultTextured();

        // TODO HACK: Workaround because the internal Slick texture mechanism is never used
        _workaroundTexture.bind();

        if (_shadowed)
            _font.drawString(_shadowOffset.x, _shadowOffset.y, _text, _shadowColor);

        _font.drawString(0, 0, _text, _color);

        // TODO: Also ugly..
        glDisable(GL11.GL_TEXTURE_2D);

        PerformanceMonitor.endActivity();
    }

    @Override
    public void update() {
        // Nothing to do here
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
    }

    public Color getColor() {
        return _color;
    }

    public void setColor(Color color) {
        _color = color;
    }

    public Color getShadowColor() {
        return _shadowColor;
    }

    public void setShadowColor(Color shadowColor) {
        _shadowColor = shadowColor;
    }

    public void setShadowed(boolean shadowed) {
        _shadowed = shadowed;
    }

    public boolean isShadowed() {
        return _shadowed;
    }

    public AngelCodeFont getFont() {
        return _font;
    }

    public void setFont(AngelCodeFont font) {
        _font = font;
    }

    public int getTextHeight() {
        return _font.getHeight(_text);
    }

    public int getTextWidth() {
        return _font.getWidth(_text);
    }

    public Vector2f calcCenterPosition() {
        // This has to be calculated separately since the width of the text depends on the selected font
        return new Vector2f(Display.getWidth() / 2 - getTextWidth() / 2, Display.getHeight() / 2 - getTextHeight());
    }
}
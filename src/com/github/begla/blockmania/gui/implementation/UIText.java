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
package com.github.begla.blockmania.gui.implementation;

import com.github.begla.blockmania.gui.framework.BlockmaniaDisplayElement;
import com.github.begla.blockmania.rendering.manager.FontManager;
import com.github.begla.blockmania.rendering.manager.TextureManager;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.glDisable;

/**
 * Composition of multiple display elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIText extends BlockmaniaDisplayElement {

    private String _text = "";

    public UIText() {
        super();
    }

    public UIText(Vector2f position) {
        super(position);
    }

    public void render() {
        // HACK: Make sure Slick binds a new texture...
        TextureManager.getInstance().getTexture("terrain").bind();

        FontManager.getInstance().getFont("default").drawString(0, 0, _text);

        glDisable(GL11.GL_TEXTURE_2D);
        /// HACK: Ends here...
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
}
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
package com.github.begla.blockmania.rendering;

import com.github.begla.blockmania.Game;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TextureManager {

    private static TextureManager _instance;
    private HashMap<String, Texture> _textures = new HashMap<String, Texture>();

    public static TextureManager getInstance() {
        if (_instance == null)
            _instance = new TextureManager();

        return _instance;
    }

    public TextureManager() {
        try {
            Game.getInstance().getLogger().log(Level.FINE, "Loading textures...");

            _textures.put("terrain", TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/terrain.png").openStream(), GL_NEAREST));
            _textures.put("sun", TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/sun.png").openStream(), GL_NEAREST));
            _textures.put("moon", TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/moon.png").openStream(), GL_NEAREST));

            _textures.put("shading", new TextureImpl("shading", GL11.GL_TEXTURE_1D, createShadeTexture()));
            Game.getInstance().getLogger().log(Level.FINE, "Finished loading textures!");
        } catch (IOException ex) {
            Game.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public int createShadeTexture() {
        int texture = GL11.glGenTextures();

        glBindTexture(GL11.GL_TEXTURE_1D, texture);
        ByteBuffer buffer = BufferUtils.createByteBuffer(3 * 256);

        for (int i = 0; i < 256; i++) {
            buffer.put((byte) i);
            buffer.put((byte) i);
            buffer.put((byte) i);
        }

        buffer.flip();

        glTexImage1D(GL11.GL_TEXTURE_1D, 0, GL11.GL_RGB, 256, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
        glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        return texture;
    }

    public void bindTexture(String s) {
        _textures.get(s).bind();
    }
}

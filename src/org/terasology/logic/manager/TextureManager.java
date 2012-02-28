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
package org.terasology.logic.manager;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.newdawn.slick.opengl.PNGDecoder;
import org.newdawn.slick.util.ResourceLoader;
import org.terasology.game.Terasology;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;

/**
 * Provides support for loading and applying textures.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TextureManager {

    public class Texture {
        public int id;
        public int width;
        public int height;
        public ByteBuffer data;
    }

    private static TextureManager _instance;
    private final HashMap<String, Texture> _textures = new HashMap<String, Texture>();

    public static TextureManager getInstance() {
        if (_instance == null)
            _instance = new TextureManager();

        return _instance;
    }

    public TextureManager() {
        Terasology.getInstance().getLogger().log(Level.FINE, "Loading textures...");

        loadDefaultTextures();

        Terasology.getInstance().getLogger().log(Level.FINE, "Finished loading textures!");
    }

    public void loadDefaultTextures() {
        addTexture("custom_lava_still");
        addTexture("custom_lava_flowing");
        addTexture("water_normal", GL11.GL_REPEAT, GL_LINEAR);
        addTexture("water_normal2", GL11.GL_REPEAT, GL_LINEAR);

        /* UI */
        addTexture("gui_menu");
        addTexture("gui");
        addTexture("icons");
        addTexture("items");
        addTexture("terasology");
        addTexture("loadingBackground", GL11.GL_CLAMP, GL_LINEAR);
        addTexture("menuBackground", GL11.GL_CLAMP, GL_LINEAR);
        addTexture("inventory");

        /* MOBS */
        addTexture("slime");
        addTexture("char");

        /* EFFECTS */
        addTexture("effects");
        addTexture("vignette", GL11.GL_CLAMP, GL_LINEAR);

        for (int i = 1; i <= 6; i++) {
            addTexture("stars" + i);
        }
    }

    public void addTexture(String title) {
        try {
            addTexture(title, "org/terasology/data/textures/" + title + ".png");
        } catch (IOException ex) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void addTexture(String title, int addressingMode, int interpolationMode) {
        try {
            addTexture(title, "org/terasology/data/textures/" + title + ".png", null, addressingMode, interpolationMode);
        } catch (IOException ex) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void readTexture(String path, Texture target) throws IOException {
        InputStream stream = ResourceLoader.getResource(path).openStream();
        PNGDecoder decoder = new PNGDecoder(stream);

        ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
        decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.RGBA);
        buf.flip();

        target.data = buf;
        target.height = decoder.getHeight();
        target.width = decoder.getWidth();
    }

    public Texture loadTexture(String path, String[] mipMapPaths, int addressingMode, int interpolationMode) throws IOException {
        Texture texture = new Texture();

        texture.id = glGenTextures();
        glBindTexture(GL11.GL_TEXTURE_2D, texture.id);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, addressingMode);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, addressingMode);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, interpolationMode);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, interpolationMode);

        readTexture(path, texture);

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, texture.width, texture.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texture.data);

        if (mipMapPaths != null) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipMapPaths.length);
            GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
            GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_NEAREST_MIPMAP_NEAREST);

            for (int i = 0; i < mipMapPaths.length; i++) {
                Texture t = new Texture();
                readTexture(mipMapPaths[i], t);

                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i + 1, GL11.GL_RGBA, t.width, t.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, t.data);
            }
        }

        return texture;
    }

    public void addTexture(String bindName, String path, String[] mipMapPaths, int addressingMode, int interpolationMode) throws IOException {
        _textures.put(bindName, loadTexture(path, mipMapPaths, addressingMode, interpolationMode));
    }

    public void addTexture(String bindName, String path, String[] mipMapPaths) throws IOException {
        _textures.put(bindName, loadTexture(path, mipMapPaths, GL_CLAMP, GL_NEAREST));
    }

    public void addTexture(String bindName, String path) throws IOException {
        _textures.put(bindName, loadTexture(path, null, GL_CLAMP, GL_NEAREST));
    }

    public void bindTexture(String s) {
        if (s == null) {
            glBindTexture(GL11.GL_TEXTURE_2D, 0);
            return;
        }

        if (_textures.containsKey(s))
            glBindTexture(GL11.GL_TEXTURE_2D, _textures.get(s).id);
    }

    public Texture getTexture(String s) {
        return _textures.get(s);
    }
}

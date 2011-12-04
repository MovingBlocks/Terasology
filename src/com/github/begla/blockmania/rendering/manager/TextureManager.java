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
package com.github.begla.blockmania.rendering.manager;

import com.github.begla.blockmania.game.Blockmania;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Provides support for loading and applying textures.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TextureManager {

    private static TextureManager _instance;
    private final HashMap<String, Texture> _textures = new HashMap<String, Texture>();

    public static TextureManager getInstance() {
        if (_instance == null)
            _instance = new TextureManager();

        return _instance;
    }

    public TextureManager() {
        Blockmania.getInstance().getLogger().log(Level.FINE, "Loading textures...");

        loadDefaultTextures();

        Blockmania.getInstance().getLogger().log(Level.FINE, "Finished loading textures!");
    }

    public void loadDefaultTextures() {
        loadTexture("terrain");
        loadTexture("button");
        loadTexture("custom_lava_still");
        loadTexture("custom_water_still");
        loadTexture("custom_lava_flowing");
        loadTexture("custom_water_flowing");
        loadTexture("blockmania");

        loadTexture("slime");

        for (int i = 1; i <= 6; i++) {
            loadTexture("stars" + i);
        }
    }

    public void loadTexture(String title) {
        try {
            _textures.put(title, TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/textures/" + title + ".png").openStream(), GL_NEAREST));
        } catch (IOException ex) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void bindTexture(String s) {
        glBindTexture(GL11.GL_TEXTURE_2D, _textures.get(s).getTextureID());
    }

    public Texture getTexture(String s) {
        return _textures.get(s);
    }
}

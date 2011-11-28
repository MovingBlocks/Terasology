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
        try {
            Blockmania.getInstance().getLogger().log(Level.FINE, "Loading textures...");

            _textures.put("custom_lava_still", TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/textures/custom_lava_still.png").openStream(), GL_NEAREST));
            _textures.put("custom_water_still", TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/textures/custom_water_still.png").openStream(), GL_NEAREST));
            _textures.put("custom_lava_flowing", TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/textures/custom_lava_flowing.png").openStream(), GL_NEAREST));
            _textures.put("custom_water_flowing", TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/textures/custom_water_flowing.png").openStream(), GL_NEAREST));
            _textures.put("terrain", TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/textures/terrain.png").openStream(), GL_NEAREST));
            _textures.put("moon", TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/textures/moon.png").openStream(), GL_NEAREST));
            _textures.put("slime", TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/textures/slime.png").openStream(), GL_NEAREST));

            for (int i = 1; i <= 6; i++) {
                _textures.put("stars" + i, TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/textures/stars" + i + ".png").openStream(), GL_NEAREST));
            }

            Blockmania.getInstance().getLogger().log(Level.FINE, "Finished loading textures!");
        } catch (IOException ex) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void bindTexture(String s) {
        glBindTexture(GL11.GL_TEXTURE_2D, _textures.get(s).getTextureID());
    }

    /*Test*/
    public Texture getTexture(String s) {
        return _textures.get(s);
    }
}

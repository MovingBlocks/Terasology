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

import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.game.Blockmania;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.MipMap;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    private static final boolean MIP_MAPPING = (Boolean) ConfigurationManager.getInstance().getConfig().get("Graphics.mipMapping");
    private static final int ANISOTROPIC_FILTERING = (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.anisotropicFiltering");

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
        loadTexture("terrain", true);
        loadTexture("custom_lava_still", false);
        loadTexture("custom_water_still", false);
        loadTexture("custom_lava_flowing", false);
        loadTexture("custom_water_flowing", false);

        /* UI */
        loadTexture("button", false);
        loadTexture("blockmania", false);

        /* MOBS */
        loadTexture("slime", false);
        loadTexture("char", false);

        for (int i = 1; i <= 6; i++) {
            loadTexture("stars" + i, false);
        }
    }

    private void updateTextureParams(Texture tex, boolean generateMipMap) {
        if (MIP_MAPPING && generateMipMap) {
            glBindTexture(GL11.GL_TEXTURE_2D, tex.getTextureID());

            int width = tex.getImageWidth();
            int height = tex.getImageHeight();

            byte[] texbytes = tex.getTextureData();
            int components = texbytes.length / (width * height);

            ByteBuffer texdata = BufferUtils.createByteBuffer(texbytes.length);
            texdata.put(texbytes);
            texdata.rewind();

            MipMap.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, components, width, height, components == 3 ? GL11.GL_RGB : GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texdata);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 2);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 1);
        }

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
    }

    public void loadTexture(String title, boolean generateMipMap) {
        try {
            Texture tex = TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/data/textures/" + title + ".png").openStream(), GL_NEAREST);
            _textures.put(title, tex);

            updateTextureParams(tex, generateMipMap);
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

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
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.ResourceLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Provides global access to fonts.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class FontManager {

    private final HashMap<String, AngelCodeFont> _fonts = new HashMap<String, AngelCodeFont>();
    private static FontManager _instance = null;

    public static FontManager getInstance() {
        if (_instance == null) {
            _instance = new FontManager();
        }

        return _instance;
    }

    private FontManager() {
        initFonts();
    }

    private void initFonts() {
        try {
            _fonts.put("default", new AngelCodeFont("Font", ResourceLoader.getResource("com/github/begla/blockmania/data/fonts/default.fnt").openStream(), ResourceLoader.getResource("com/github/begla/blockmania/data/fonts/default_0.png").openStream()));
        } catch (SlickException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, "Couldn't load fonts. Sorry. " + e.toString(), e);
        } catch (IOException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, "Couldn't load fonts. Sorry. " + e.toString(), e);
        }
    }

    public AngelCodeFont getFont(String s) {
        return _fonts.get(s);
    }
}

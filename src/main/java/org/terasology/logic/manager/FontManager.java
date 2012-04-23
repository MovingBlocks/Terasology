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

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.ResourceLoader;
import org.terasology.game.Terasology;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides global access to fonts.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class FontManager {

    private Logger logger = Logger.getLogger(getClass().getName());
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
            _fonts.put("default", new AngelCodeFont("Font", ResourceLoader.getResource("org/terasology/data/fonts/default.fnt").openStream(), ResourceLoader.getResource("org/terasology/data/fonts/default_0.png").openStream()));
        } catch (SlickException e) {
            logger.log(Level.SEVERE, "Couldn't load fonts. Sorry. " + e.toString(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Couldn't load fonts. Sorry. " + e.toString(), e);
        }
    }

    public AngelCodeFont getFont(String s) {
        return _fonts.get(s);
    }
}

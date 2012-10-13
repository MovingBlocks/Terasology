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
package org.terasology.logic.manager;

import java.io.IOException;
import java.util.HashMap;

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides global access to fonts.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class FontManager {

    private static final Logger logger = LoggerFactory.getLogger(FontManager.class);

    private final HashMap<String, AngelCodeFont> fonts = new HashMap<String, AngelCodeFont>();
    private static FontManager instance = null;

    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }

        return instance;
    }

    private FontManager() {
        initFonts();
    }

    private void initFonts() {
        try {
            fonts.put("default", new AngelCodeFont("Font", ResourceLoader.getResource("assets/fonts/default.fnt").openStream(), ResourceLoader.getResource("assets/fonts/default_0.png").openStream()));
        } catch (SlickException e) {
            logger.error("Couldn't load fonts.", e);
        } catch (IOException e) {
            logger.error("Couldn't load fonts.", e);
        }
    }

    public AngelCodeFont getFont(String s) {
        return fonts.get(s);
    }
}

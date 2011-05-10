/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.github.begla.blockmania;

import java.util.HashMap;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector3f;

/**
 * Stores the settings of the game.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Configuration {

    // Engine settings
    public static final String GAME_TITLE = "Blockmania Alpha v0.02";
    public static final Vector3f VIEWING_DISTANCE_IN_CHUNKS = new Vector3f(16.0f, 1.0f, 16.0f);
    public static final float SUN_SIZE = 64f;
    public static final String DEFAULT_SEED = "yMfLzKzZvHzQiWhA";
    public static final Vector3f CHUNK_DIMENSIONS = new Vector3f(16, 128, 16);
    public static final PixelFormat PIXEL_FORMAT = new PixelFormat().withDepthBits(24);
    public static final int DISPLAY_HEIGHT = 720;
    public static final int DISPLAY_WIDTH = 1280;
    public static final boolean FULLSCREEN = false;
    public static final byte MAX_LIGHT = 16;
    public static final byte MIN_LIGHT = 0;
    public static final float OCCLUSION_INTENS = 0.0625f;
    public static final float BLOCK_SIDE_DIMMING = 0.05f;
    public static final float LIGHT_ABSORPTION = 0.0625f;
    // Settings
    public static final HashMap<String, Float> _settingsNumeric = new HashMap<String, Float>();
    public static final HashMap<String, Boolean> _settingsBoolean = new HashMap<String, Boolean>();

    static {
        _settingsBoolean.put("SHOW_PLACING_BOX", true);
        _settingsBoolean.put("SHOW_CHUNK_OUTLINES", false);
        _settingsBoolean.put("SHOW_HUD", true);
        _settingsBoolean.put("ENABLE_BOBBING", true);
        _settingsBoolean.put("DEMO_FLIGHT", false);
        _settingsBoolean.put("GOD_MODE", false);
        _settingsNumeric.put("JUMP_INTENSITY", 10f);
        _settingsNumeric.put("MAX_GRAVITY", 64f);
        _settingsNumeric.put("WALKING_SPEED", 1.5f);
        _settingsNumeric.put("RUNNING_FACTOR", 2f);
        _settingsNumeric.put("PLAYER_HEIGHT", 1.0f);
        _settingsNumeric.put("G_FORCE", 0.05f);
        _settingsNumeric.put("SLOWDOWN_INTENS", 0.05f);
    }

    public static Float getSettingNumeric(String key) {
        return _settingsNumeric.get(key);
    }

    public static Boolean getSettingBoolean(String key) {
        return _settingsBoolean.get(key);
    }

    public static void setSetting(String key, Boolean value) {
        _settingsBoolean.put(key, value);
    }

    public static void setSetting(String key, Float value) {
        _settingsNumeric.put(key, value);
    }
}

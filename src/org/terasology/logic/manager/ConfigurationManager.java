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

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.terasology.game.Terasology;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;

/**
 * Manages and provides access to the global settings of the game.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>, Kai Kratz <kaikratz@googlemail.com>
 */
@SuppressWarnings({"UnusedDeclaration", "unchecked"})
public final class ConfigurationManager {
    private static ConfigurationManager _instance;
    private static final String CONFIG_PATH = "CONFIG/";
    private static final String CURRENT_CONFIG_FILENAME = "config";
    private ConfigObject _gameConfig;
    private ConfigObject _serverConfig;

    public static ConfigurationManager getInstance() {
        if (_instance == null) {
            _instance = new ConfigurationManager();
        }
        return _instance;
    }

    private ConfigurationManager() {
        Terasology.getInstance().getLogger().log(Level.FINEST, "Creating instance of" + this.getClass());
        Terasology.getInstance().getLogger().log(Level.FINEST, "Trying to load config of last session");
        if(!loadLastConfiguration()){
            Terasology.getInstance().getLogger().log(Level.FINEST, "Loading config of last session failed. Generating default config");
            loadDefaultConfiguration();
        }
    }

    private boolean loadLastConfiguration(){
        return load(CONFIG_PATH + CURRENT_CONFIG_FILENAME);
    }

    private void loadDefaultConfiguration(){
        _serverConfig = createServerDefaults();
        _gameConfig = createGameDefaults();
    }

    public boolean load(String filename){
        File file = new File(filename);
        ConfigObject config;
        try {
            config = new ConfigSlurper().parse(file.toURI().toURL());
            Terasology.getInstance().getLogger().log(Level.INFO, "Loaded settings from " + filename);

        } catch (Exception e) {
            Terasology.getInstance().getLogger().log(Level.INFO, e.toString(), "Could not load " + filename);
            Terasology.getInstance().getLogger().log(Level.INFO, e.toString(), e);
            return false;
        }
        _gameConfig = (ConfigObject)((ConfigObject)config.getProperty("Terasology")).getProperty("User");
        _serverConfig = (ConfigObject)((ConfigObject)config.getProperty("Terasology")).getProperty("Server");
        return true;
    }

    public void save() {
        FileWriter writer;
        try {
            File path = new File(CONFIG_PATH);
            if (!path.exists()) {
                path.mkdir();
            }
            writer = new FileWriter(CONFIG_PATH + CURRENT_CONFIG_FILENAME);
        } catch (IOException e) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, "Could not save settings!\n" + e.toString());
            return;
        }
        ConfigObject co = new ConfigObject();
        ConfigObject tco = (ConfigObject)co.getProperty("Terasology");
        tco.put("User", _gameConfig);
        tco.put("Server", _serverConfig);
        co.writeTo(writer);
    }

    public Object getGameSetting(String key) {
        Object obj = _gameConfig.flatten().get(key);
        if (obj == null) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, key + " unknown in game config.");
        }
        obj = bigDecimalFix(obj);
        return obj;
    }

    public boolean setGameSetting(String key, Object value) {
        String[] stemAndKey = splitIntoStemAndKey(key);
        ((ConfigObject)_gameConfig.getProperty(stemAndKey[0])).put(stemAndKey[1], value);
        return true;
    }

    public Object getServerSetting(String key) {
        Object obj = _serverConfig.flatten().get(key);
        if (obj == null) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, key + " unknown in server config.");
        }
        obj = bigDecimalFix(obj);
        return obj;
    }

    private Object bigDecimalFix(Object obj) {
        if(obj.getClass() == BigDecimal.class){
            BigDecimal bd = (BigDecimal)obj;
            return bd.doubleValue();
        }
        return obj;
    }

    public boolean setServerSetting(String key, Object value) {
        boolean rightsGranted = UserLevel.getInstance().hasRights();
        if (rightsGranted) {
            String[] stemAndKey = splitIntoStemAndKey(key);
            ((ConfigObject)_serverConfig.getProperty(stemAndKey[0])).put(stemAndKey[1], value);
        }
        return rightsGranted;
    }

    private String[] splitIntoStemAndKey(String key) {
        String[] stemAndKey = new String[2];
        int splitAt = key.lastIndexOf('.');
        stemAndKey[0] = key.substring(0, splitAt);
        stemAndKey[1] = key.substring(splitAt);
        return stemAndKey;
    }

    private ConfigObject createServerDefaults() {

        ConfigObject config = new ConfigObject();
        ConfigObject tmp;

        tmp = (ConfigObject)((ConfigObject)config.getProperty("World")).getProperty("Info");
        tmp.put("title", "New World");

        tmp = (ConfigObject)((ConfigObject)config.getProperty("World")).getProperty("Physics");
        tmp.put("maxGravity", 1.0d);
        tmp.put("maxGravitySwimming", 0.04d);
        tmp.put("gravity", 0.008d);
        tmp.put("gravitySwimming", 0.008d * 4d);
        tmp.put("friction", 0.15d);
        tmp.put("walkingSpeed", 0.025d);
        tmp.put("runningFactor", 1.5d);
        tmp.put("jumpIntensity", 0.16d);

        tmp = (ConfigObject)((ConfigObject)((ConfigObject)config.getProperty("World")).getProperty("Biomes")).getProperty("Forest");
        tmp.put("grassDensity", 0.3d);

        tmp = (ConfigObject)((ConfigObject)((ConfigObject)config.getProperty("World")).getProperty("Biomes")).getProperty("Plains");
        tmp.put("grassDensity", 0.1d);

        tmp = (ConfigObject)((ConfigObject)((ConfigObject)config.getProperty("World")).getProperty("Biomes")).getProperty("Snow");
        tmp.put("grassDensity", 0.001d);

        tmp = (ConfigObject)((ConfigObject)((ConfigObject)config.getProperty("World")).getProperty("Biomes")).getProperty("Mountains");
        tmp.put("grassDensity", 0.2d);

        tmp = (ConfigObject)((ConfigObject)((ConfigObject)config.getProperty("World")).getProperty("Biomes")).getProperty("Desert");
        tmp.put("grassDensity", 0.001d);

        tmp = (ConfigObject)((ConfigObject)config.getProperty("World")).getProperty("DiurnalCycle");
        tmp.put("dayNightLengthInMs", 1800000);
        tmp.put("initialTimeOffsetInMs", 60000);

        tmp = (ConfigObject)((ConfigObject)config.getProperty("World")).getProperty("Creation");
        tmp.put("spawnOriginX", -24429);
        tmp.put("spawnOriginY", 20547);
        tmp.put("defaultSeed", "Pinkie Pie!");

        tmp = (ConfigObject)((ConfigObject)config.getProperty("World")).getProperty("Debug");
        tmp.put("debug", false);
        tmp.put("debugCollision", false);
        tmp.put("renderChunkBoundingBoxes", false);
        tmp.put("demoFlight", false);
        tmp.put("demoFlightSpeed", 0.08d);
        tmp.put("godMode", false);

        return config;
    }

    private ConfigObject createGameDefaults() {
        ConfigObject config = new ConfigObject();
        ConfigObject tmp;

        tmp = (ConfigObject)((ConfigObject)config.getProperty("Game")).getProperty("Graphics");
        tmp.put("maxParticles", 256);
        tmp.put("cloudResolutionWidth", 128);
        tmp.put("cloudResolutionHeight", 128);
        tmp.put("cloudUpdateInterval", 8000);
        tmp.put("maxThreads", 2);
        tmp.put("saveChunks", true);
        tmp.put("chunkCacheSize", 2048);
        tmp.put("maxChunkVBOs", 512);
        tmp.put("gamma", 2.2d);
        tmp.put("pixelFormat", 24);
        tmp.put("displayModeWidth", 1280);
        tmp.put("displayModeHeight", 720);
        tmp.put("fullscreen", false);
        tmp.put("viewingDistanceNear", 8);
        tmp.put("viewingDistanceModerate", 16);
        tmp.put("viewingDistanceFar", 26);
        tmp.put("viewingDistanceUltra", 32);
        tmp.put("enablePostProcessingEffects", true);
        tmp.put("animatedWaterAndGrass", true);
        tmp.put("verticalChunkMeshSegments", 1);

        tmp = (ConfigObject)((ConfigObject)config.getProperty("Game")).getProperty("Controls");
        tmp.put("mouseSens", 0.075d);

        tmp = (ConfigObject)((ConfigObject)config.getProperty("Game")).getProperty("Player");
        tmp.put("fov", 86.0d);
        tmp.put("cameraBobbing", true);
        tmp.put("renderFirstPersonView", true);

        tmp = (ConfigObject)((ConfigObject)config.getProperty("Game")).getProperty("HUD");
        tmp.put("placingBox", true);

        return config;
    }
}



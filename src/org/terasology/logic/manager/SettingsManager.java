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
import org.terasology.game.Terasology;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Manages and provides access to the global settings of the game.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>, Kai Kratz <kaikratz@googlemail.com>
 */
public final class SettingsManager {
    private static final String CONFIG_BASE_PATH = "groovy/config/";
    private static final String CONFIG_PRESET_PATH = "presets/";
    private static final String DEFAULT_WORLD_SETTINGS = "DefaultWorldSettings.wrl";
    private static final String DEFAULT_USER_SETTINGS = "DefaultUserSettings.usr";
    private static final String WORLD_SETTINGS = "WorldSettings.wrl";
    private static final String USER_SETTINGS = "UserSettings.usr";
    private static final String WORLD_FILE_EXT = ".wrl";
    private static final String USER_FILE_EXT = ".usr";
    private static SettingsManager _instance;

    private Map<String, ConfigObject> _userConfigMap = new HashMap<String, ConfigObject>();
    private Map<String, ConfigObject> _worldConfigMap = new HashMap<String, ConfigObject>();
    @SuppressWarnings("rawtypes")
	private Map _userConfig;
    private Map _worldConfig;

    public static SettingsManager getInstance() {
        if (_instance == null) {
            _instance = new SettingsManager();
        }
        return _instance;
    }

    private SettingsManager() {
        loadPresets();
        loadLastSettings();
        //set last user settings as current settings
        //if no last user settings set default ones
        //no default ones? PANIC try to load any setings file
        //still no config? throw error and abort mission. good luck cowboy...
        try{
            trySetWorldSetting();
            trySetUserSetting();
        } catch (RuntimeException e){
            Terasology.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    private void trySetWorldSetting() {
        if(!setWorldSettingInternal(WORLD_SETTINGS)){
            if(!setWorldSettingInternal(DEFAULT_WORLD_SETTINGS)){
                Set<String> keys = _worldConfigMap.keySet();
                for(String s : keys){
                    if(setWorldSettingInternal(s)){
                        //if we have any config in our map we should never execute this loop more than once.
                        return;
                    }
                }
                throw new RuntimeException("Could not load any user setting.");
            }
        }
    }

    private void trySetUserSetting() {
        if(!setUserSettingInternal(USER_SETTINGS)){
            if(!setUserSettingInternal(DEFAULT_USER_SETTINGS)){
                Set<String> keys = _userConfigMap.keySet();
                for(String s : keys){
                    if(setUserSettingInternal(s)){
                        //if we have any config in our map we should never execute this loop more than once.
                        return;
                    }
                }
                throw new RuntimeException("Could not load any world setting.");
            }
        }
    }

    private boolean setWorldSettingInternal(String worldSettings) {
        ConfigObject config = _worldConfigMap.get(worldSettings);
        if(config == null)
            return false;
        _worldConfig = config.flatten();
        return true;
    }

    private boolean setUserSettingInternal(String userSettings) {
        ConfigObject config = _userConfigMap.get(userSettings);
        if(config == null)
            return false;
        _userConfig = config.flatten();
        return true;
    }

    private void loadPresets() {
        File folder = new File(CONFIG_BASE_PATH + CONFIG_PRESET_PATH);
        File[] listOfFiles = folder.listFiles();
        ConfigObject config;
        for(File file : listOfFiles){
            try{
                if(file.getName().endsWith(WORLD_FILE_EXT)){
                    config = new ConfigSlurper().parse(file.toURI().toURL());
                    _worldConfigMap.put(file.getName(), config);
                } else if(file.getName().endsWith(USER_FILE_EXT)){
                    config = new ConfigSlurper().parse(file.toURI().toURL());
                    _userConfigMap.put(file.getName(), config);
                } else {
                    //log that we encountered an unknown file, but ignore it regarding config slurping.
                    Terasology.getInstance().getLogger().log(Level.INFO, "Encountered unknown file " + file.getName() + ". File will be ignored.");
                }
            } catch(MalformedURLException e){
                Terasology.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
            }
        }

    }

    private void loadLastSettings() {
        ConfigObject config;
        File file;

        try{
            file = new File(CONFIG_BASE_PATH + WORLD_SETTINGS);
            if(file.exists())
            {
                config = new ConfigSlurper().parse(file.toURI().toURL());
                _worldConfigMap.put(file.getName(), config);
            }
        } catch(MalformedURLException e){
            Terasology.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }

        try{
            file = new File(CONFIG_BASE_PATH + USER_SETTINGS);
            if(file.exists())
            {
                config = new ConfigSlurper().parse(file.toURI().toURL());
                _userConfigMap.put(file.getName(), config);
            }
        } catch(MalformedURLException e){
            Terasology.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    public void saveCurrentSettings(String filename){
        //TODO!
    }

    public Object getUserSetting(String key){
        Object obj =  _userConfig.get(key);
        if(obj == null){
            Terasology.getInstance().getLogger().log(Level.SEVERE, key + " unknown in user config.");
        }
        return obj;
    }

    public boolean setUserSetting(String key, Object value){
        _userConfig.put(key, value);
        return true;
    }

    public Object getWorldSetting(String key){
        Object obj =  _worldConfig.get(key);
        if(obj == null){
            Terasology.getInstance().getLogger().log(Level.SEVERE, key + " unknown in world config.");
        }
        return obj;
    }

    public boolean setWorldSetting(String key, Object value){
        boolean rightsGranted = UserLevel.getInstance().hasRights();
        if(rightsGranted) {
            _worldConfig.put(key, value);
        }
        return rightsGranted;
    }
    
    public Set<String> getPossibleUserConfigurations(){
        return _userConfigMap.keySet();
    }
    
    public Set<String> getPossibleWorldConfigurations(){
        return _worldConfigMap.keySet();
    }
    
    public boolean setUserPreset(String preset){
        return setUserSettingInternal(preset);
    }
    
    public boolean setWorldPreset(String preset){
        boolean rightsGranted = UserLevel.getInstance().hasRights();
        boolean success = true;
        if(rightsGranted) {
            success = setWorldSettingInternal(preset);
        }
        return rightsGranted && success ;
    }
}

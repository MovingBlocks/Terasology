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

import java.util.Map;

/**
 * Manages and provides access to the global settings of the game.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ConfigurationManager {

    /* CONST */
    //private static final String DEFAULT_CONFIG_PATH = "groovy/config/Default.groovy";

    /* SINGLETON */
    private static ConfigurationManager _instance;

    /* CONFIGURATION */
    private Map _config;

    /**
     * Returns the currently active instance.
     *
     * @return The instance
     */
    public static ConfigurationManager getInstance() {
        if (_instance == null)
            _instance = new ConfigurationManager();

        return _instance;
    }

    /**
     * Init. a new configuration manager.
     */
    private ConfigurationManager() {
        loadConfigEnvironment(null);
    }

    /**
     * Loads a configuration environment.
     *
     * @param environment The environment (nullable)
     */
    public void loadConfigEnvironment(String environment) {
        ConfigObject config = null;
        // TODO: Clean this up - temp hack to internalize the external Config defaults in Groovy
        DefaultConfig yay = new DefaultConfig();
        config = yay.getConfig();
/*
        try {
            if (environment != null)
                config = new ConfigSlurper(environment).parse(new File(DEFAULT_CONFIG_PATH).toURI().toURL());
            else
                config = new ConfigSlurper().parse(new File(DEFAULT_CONFIG_PATH).toURI().toURL());

        } catch (IOException e) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }

        //config = new ConfigSlurper().parse(new DefaultConfig());*/
        if (config != null)
            _config = config.flatten();
    }

    public Map getConfig() {
        return _config;
    }
}

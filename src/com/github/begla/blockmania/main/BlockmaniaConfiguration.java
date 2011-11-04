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
package com.github.begla.blockmania.main;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.newdawn.slick.util.ResourceLoader;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;

/**
 * Blockmania's global settings.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class BlockmaniaConfiguration {

    /* SINGLETON */

    private static BlockmaniaConfiguration _instance;

    public static BlockmaniaConfiguration getInstance() {
        if (_instance == null)
            _instance = new BlockmaniaConfiguration();

        return _instance;
    }

    /* CONFIG SLURPER */

    private Map _config;

    private BlockmaniaConfiguration() {
        try {
            ConfigObject config = new ConfigSlurper().parse(ResourceLoader.getResource("com/github/begla/blockmania/data/config/Config.groovy").toURI().toURL());
            _config = config.flatten();

        } catch (MalformedURLException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        } catch (URISyntaxException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    public Map getConfig() {
        return _config;
    }
}

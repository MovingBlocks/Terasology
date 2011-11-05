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

package com.github.begla.blockmania.groovy;

import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.main.BlockmaniaConfiguration;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.newdawn.slick.util.ResourceLoader;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Manages everything related to using Groovy from within Java
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class GroovyManager {
    /**
     * The Binding allows us to keep variable references around where Groovy can play with them
     */
    private Binding _bind;

    /**
     * Directory where we keep "plugin" files (Groovy scripts we'll run - prolly move this setting elsewhere sometime)
     */
    private final String pluginsPath = ResourceLoader.getResource("com/github/begla/blockmania/data/plugins/").getPath();

    /**
     * Initialize the GroovyManager and "share" the given World variable via the Binding
     */
    public GroovyManager() {
        _bind = new Binding();
        _bind.setVariable("bm", Blockmania.getInstance());
        _bind.setVariable("conf", BlockmaniaConfiguration.getInstance().getConfig());

        // Could execute plugins here that must go before the game starts (in a loop) etc
        initializePlugin("Slimer");
    }

    /**
     * Method to initialize a plugin - a.k.a. execute a Groovy script in the plugin dir
     *
     * @param pluginName Name of a particular plugin file to execute
     */
    public void initializePlugin(String pluginName) {
        GroovyScriptEngine gse = null;
        try {
            // Create an engine tied to the dir we keep plugins in
            gse = new GroovyScriptEngine(pluginsPath);
        } catch (IOException ioe) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, "Failed to initialize plugin (IOException): " + pluginName + ", reason: " + ioe.toString(), ioe);
            ioe.printStackTrace();
        }

        if (gse != null) {
            try {
                // Run the specified plugin
                gse.run(pluginName + ".groovy", _bind);
            } catch (ResourceException re) {
                Blockmania.getInstance().getLogger().log(Level.SEVERE, "Failed to execute plugin (ResourceException): " + pluginName + ", reason: " + re.toString(), re);
                re.printStackTrace();
            } catch (ScriptException se) {
                Blockmania.getInstance().getLogger().log(Level.SEVERE, "Failed to execute plugin (ScriptException): " + pluginName + ", reason: " + se.toString(), se);
                se.printStackTrace();
            }
        }
    }

    /**
     * Executes the given command with Groovy - short the prefix "groovy "
     *
     * @param consoleString Contains what the user entered into the console
     * @return boolean indicating command success or not
     */
    public boolean runGroovyShell(String consoleString) {
        Blockmania.getInstance().getLogger().log(Level.INFO, "Groovy console about to execute command: " + consoleString);
        GroovyShell shell = new GroovyShell(_bind);
        try {
            shell.evaluate(consoleString);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

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
package org.terasology.persistence;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.terasology.game.Terasology;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;

/**
 * Provides some basic functionality to many objects used in Terasology.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TeraObject {

    private File _file;
    private File _path;

    public void load() {
        if (getObjectFileName() != null && getObjectSavePath() != null) {
            _file = new File(getObjectSavePath() + "/" + getObjectFileName());
            _path = new File(getObjectSavePath());

            if (_file.exists())
                readPropertiesFromConfigObject(readPropertiesFromFile());
        }
    }

    public void dispose() {
        ConfigObject configObject = new ConfigObject();
        writePropertiesToConfigObject(configObject);
        savePropertiesToFile(configObject);
    }

    private void savePropertiesToFile(ConfigObject co) {
        try {
            // Make sure the directories exist
            if (!_path.exists())
                _path.mkdirs();

            FileWriter writer = new FileWriter(_file);
            co.writeTo(writer);
            writer.close();
        } catch (IOException e) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, "Failed writing config object. Sorry.", e);
        }
    }

    private ConfigObject readPropertiesFromFile() {
        try {
            return new ConfigSlurper().parse(_file.toURI().toURL());
        } catch (MalformedURLException e) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, "Failed reading config object. Sorry.", e);
        }

        return new ConfigObject();
    }

    public String getObjectSavePath() {
        return null;
    }

    public String getObjectFileName() {
        return null;
    }

    public void writePropertiesToConfigObject(ConfigObject co) {
    }

    public void readPropertiesFromConfigObject(ConfigObject co) {
    }
}

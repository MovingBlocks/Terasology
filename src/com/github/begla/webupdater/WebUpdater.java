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
package com.github.begla.webupdater;

import com.github.begla.blockmania.utilities.Helper;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class WebUpdater {

    public WebUpdater() {
    }

    public ArrayList<UpdateComponent> getUpdateComponents() {
        ArrayList<UpdateComponent> result = new ArrayList<UpdateComponent>();

        try {
            result.add(new UpdateComponent(new URL("http://www.movingblocks.net/data/"), "sun.png"));
            result.add(new UpdateComponent(new URL("http://www.movingblocks.net/data/"), "moon.png"));
            result.add(new UpdateComponent(new URL("http://www.movingblocks.net/data/"), "terrain.png"));
            result.add(new UpdateComponent(new URL("http://www.movingblocks.net/data/"), "clouds.png"));
        } catch (MalformedURLException ex) {
            Helper.LOGGER.log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public boolean isUpToDate() {
        ArrayList<UpdateComponent> components = getUpdateComponents();

        for (UpdateComponent c : components) {
            File f = new File(c.getLocalPath());

            if (!f.exists()) {
                Helper.LOGGER.log(Level.INFO, "Blockmania is  out of date.");
                return false;
            } else {
                try {
                    URL u = c.getURL();
                    URLConnection uc = u.openConnection();

                    int length = uc.getContentLength();

                    if (length != f.length()) {
                        return false;
                    }

                } catch (IOException ex) {
                    Helper.LOGGER.log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }

        Helper.LOGGER.log(Level.INFO, "Blockmania is up-to-date.");
        return true;
    }

    public boolean update() {
        if (isUpToDate()) {
            Helper.LOGGER.log(Level.INFO, "Skipping update process.");
            return true;
        }

        Helper.LOGGER.log(Level.INFO, "Downloading new files...");
        File gfxDir = new File("DATA");

        gfxDir.mkdir();

        try {
            ArrayList<UpdateComponent> components = getUpdateComponents();

            for (UpdateComponent c : components) {
                Helper.LOGGER.log(Level.INFO, "Downloading... {0}", c.getFileName());

                InputStream is = null;
                FileOutputStream fos = null;

                is = c.getURL().openStream();
                fos = new FileOutputStream(c.getLocalPath());

                int i;
                while ((i = is.read()) != -1) {
                    fos.write(i);
                }

                Helper.LOGGER.log(Level.INFO, "Finished downloading... {0}", c.getFileName());

                fos.flush();
                fos.close();
                is.close();
            }

        } catch (MalformedURLException ex) {
            Helper.LOGGER.log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Helper.LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }

        Helper.LOGGER.log(Level.INFO, "Finished updating Blockmania!");
        return true;
    }
}

/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.paths.PathManager;

import java.applet.Applet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
@SuppressWarnings("serial")
public final class TerasologyApplet extends Applet {
    private static final Logger logger = LoggerFactory.getLogger(TerasologyApplet.class);
    private TerasologyEngine engine;
    private Thread gameThread;

    @Override
    public void init() {
        super.init();
        obtainMods();
        startGame();
    }

    private void obtainMods() {
        String[] mods = getParameter("mods").split(",");
        String modsPath = getParameter("mods_path") + "mods/";
        //int rootPathIndex = getDocumentBase().toString().lastIndexOf('/');
        //String path = getDocumentBase().toString().substring(0, rootPathIndex + 1) + "mods/";
        for (String mod : mods) {
            try {
                URL url = new URL(modsPath + mod);
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(new File(PathManager.getInstance().getHomeModPath(), mod));
                long readBytes = fos.getChannel().transferFrom(rbc, 0, 1 << 24);
                while (readBytes == 1 << 24) {
                    readBytes = fos.getChannel().transferFrom(rbc, 0, 1 << 24);
                }
                fos.close();
            } catch (MalformedURLException e) {
                logger.error("Unable to obtain mod '{}'", mod, e);
            } catch (FileNotFoundException e) {
                logger.error("Unable to obtain mod '{}'", mod, e);
            } catch (IOException e) {
                logger.error("Unable to obtain mod '{}'", mod, e);
            }
        }
    }

    private void startGame() {
        gameThread = new Thread() {
            @Override
            public void run() {
                try {
                    PathManager.getInstance().useDefaultHomePath();
                    engine = new TerasologyEngine();
                    engine.run(new StateMainMenu());
                    engine.dispose();
                } catch (Exception e) {
                    logger.error(e.toString(), e);
                }
            }
        };

        gameThread.start();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void destroy() {
        if (engine != null)
            engine.shutdown();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            logger.error("Failed to cleanly shut down engine");
        }

        super.destroy();
    }
}

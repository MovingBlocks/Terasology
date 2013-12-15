/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.paths.PathManager;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main method for launching Terasology
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Kireev   Anton   <adeon.k87@gmail.com>
 */
public final class Terasology {
    private static final String HOME_ARG = "-homedir=";
    private static final String LOCAL_ARG = "-homedir";

    private Terasology() {
    }

    public static void main(String[] args) {
        try {
            Path homePath = null;
            for (String arg : args) {
                if (arg.startsWith(HOME_ARG)) {
                    homePath = Paths.get(arg.substring(HOME_ARG.length()));
                } else if (arg.equals(LOCAL_ARG)) {
                    homePath = Paths.get("");
                }
            }
            if (homePath != null) {
                PathManager.getInstance().useOverrideHomePath(homePath);
            } else {
                PathManager.getInstance().useDefaultHomePath();
            }
            TerasologyEngine engine = new TerasologyEngine();
            engine.init();
            engine.run(new StateMainMenu());
            engine.dispose();
        } catch (Throwable t) {
            String text = getNestedMessageText(t);
            JOptionPane.showMessageDialog(null, text, "Fatal Error", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(0);
    }

    private static String getNestedMessageText(Throwable t) {
        String nl = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();

        Throwable cause = t;
        while (cause != null && cause != cause.getCause()) {
            if (cause.getMessage() != null) {
            	sb.append(cause.getClass().getSimpleName());
            	sb.append(": ");
                sb.append(cause.getLocalizedMessage());
                sb.append(nl);
            }

            cause = cause.getCause();
        }

        return sb.toString();
    }
}

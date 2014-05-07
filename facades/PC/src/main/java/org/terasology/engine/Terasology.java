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

import com.google.common.collect.Lists;

import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.engine.subsystem.headless.HeadlessAudio;
import org.terasology.engine.subsystem.headless.HeadlessGraphics;
import org.terasology.engine.subsystem.headless.HeadlessInput;
import org.terasology.engine.subsystem.headless.HeadlessTimer;
import org.terasology.engine.subsystem.headless.mode.StateHeadlessSetup;
import org.terasology.engine.subsystem.lwjgl.LwjglAudio;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.engine.subsystem.lwjgl.LwjglInput;
import org.terasology.engine.subsystem.lwjgl.LwjglTimer;

import javax.swing.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Main method for launching Terasology
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Kireev   Anton   <adeon.k87@gmail.com>
 */
public final class Terasology {

    // -------------- ARGUMENTS ----------------
    // Terasology saves data such as game saves and logs into subfolders of a "home directory".
    // Terasology provides a default, platform-specific home directory, but the user can use
    // launch arguments to use the current directory (?) or specify one explicitly.
    private static final String LOCAL_ARG = "-homedir"; // use the -current- directory (?) as Terasology's home directory.
    private static final String HOME_ARG = "-homedir="; // use the -specified- directory as Terasology's home directory.

    // Using this argument launches Terasology without graphics, i.e. to act as a server.
    private static final String HEADLESS_ARG = "-headless";

    private Terasology() {
    }

    public static void main(String[] args) {

        boolean isHeadless = false;
        Path homePath = null;

        try {

            for (String arg : args) {
                if (arg.startsWith(HOME_ARG)) {
                    // the user explicitly specified a home directory
                    homePath = Paths.get(arg.substring(HOME_ARG.length()));
                } else if (arg.equals(LOCAL_ARG)) {
                    // the user wishes to use the current directory as the home directory
                    homePath = Paths.get("");
                } else if (arg.equals(HEADLESS_ARG)) {
                    // the user is starting Terasology as headless
                    isHeadless = true;
                }
            }

            if (homePath != null) {
                PathManager.getInstance().useOverrideHomePath(homePath);
            } else {
                PathManager.getInstance().useDefaultHomePath();
            }

            Collection<EngineSubsystem> subsystemList;
            if (isHeadless) {
                subsystemList = Lists.newArrayList(new HeadlessGraphics(), new HeadlessTimer(), new HeadlessAudio(), new HeadlessInput());
            } else {
                subsystemList = Lists.<EngineSubsystem>newArrayList(new LwjglGraphics(), new LwjglTimer(), new LwjglAudio(), new LwjglInput());
            }

            // ------------------------------------------------------------------------------------------------
            // IMPORTANT SECTION: this is where the engine is initialized, started and eventually disposed of.
            // ------------------------------------------------------------------------------------------------
            TerasologyEngine engine = new TerasologyEngine(subsystemList);
            engine.init();

            if (isHeadless) {
                engine.run(new StateHeadlessSetup());
            } else {
                engine.run(new StateMainMenu());
            }
            engine.dispose();
            // ------------------------------------------------------------------------------------------------

        } catch (Throwable t) {
            String text = getNestedMessageText(t);
            JOptionPane.showMessageDialog(null, text, "Fatal Error", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(0);
    }

    /**
     The content of a throwable can be highly nested.
     This method formats it into a single string that
     can be displayed by the dialog opened by the
     JOptionPane class.
     */
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

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

import java.io.IOException;
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

    private static final String PrintUsage_ARG            = "-help";
    private static final String UseCurrentDirAsHome_ARG   = "-homedir";
    private static final String UseSpecifiedDirAsHome_ARG = "-homedir=";
    private static final String StartHeadlessEngine_ARG   = "-headless";

    private static boolean isHeadless = false;
    private static Collection<EngineSubsystem> subsystemList;

    private Terasology() {
    }

    private static void printUsageAndExit() {

            String usage =
                "Usage:\n" +
                "\n" +
                "    terasology [" + PrintUsage_ARG + "] [" + UseCurrentDirAsHome_ARG + "|" + UseSpecifiedDirAsHome_ARG + "<path>] [" + StartHeadlessEngine_ARG + "]\n" +
                "\n" +
                "By default Terasology saves data such as game saves and logs into subfolders of a platform-specific \"home directory\".\n" +
                "Optionally, the user can override the default by using one of the following launch arguments:\n" +
                "\n" +
                "    " + UseCurrentDirAsHome_ARG + "           Use the current directory as the home directory.\n" +
                "    " + UseSpecifiedDirAsHome_ARG + "<path> Use the specified directory as the home directory.\n" +
                "\n" +
                "It is also possible to start Terasology in headless mode (no graphics), i.e. to act as a server.\n" +
                "For this purpose use the " + StartHeadlessEngine_ARG + " launch argument.\n" +
                "\n" +
                "Examples:\n" +
                "\n" +
                "    Use the current directory as the home directory:\n" +
                "    terasology " + UseCurrentDirAsHome_ARG + "\n" +
                "\n" +
                "    Use \"myPath\" as the home directory:\n" +
                "    terasology " + UseSpecifiedDirAsHome_ARG + "myPath" + "\n" +
                "\n" +
                "    Start terasology in headless mode (no graphics):\n" +
                "    terasology " + StartHeadlessEngine_ARG + "\n" +
                "\n" +
                "    Don't start Terasology, just print this help:\n" +
                "    terasology " + PrintUsage_ARG + "\n" +
                "\n";

            System.out.println(usage);
            System.exit(0);
    }

    public static void main(String[] args) {

        try {

            handleLaunchArguments(args);
            fillSubsystemList();

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

    private static void handleLaunchArguments(String[] args) throws IOException {

        Path homePath = null;

        for (String arg : args) {
            if(arg.equals(PrintUsage_ARG)) {
                printUsageAndExit();
            } else if (arg.startsWith(UseSpecifiedDirAsHome_ARG)) {
                homePath = Paths.get(arg.substring(UseSpecifiedDirAsHome_ARG.length()));
            } else if (arg.equals(UseCurrentDirAsHome_ARG)) {
                homePath = Paths.get("");
            } else if (arg.equals(StartHeadlessEngine_ARG)) {
                isHeadless = true;
            }
        }

        if (homePath != null) {
            PathManager.getInstance().useOverrideHomePath(homePath);
        } else {
            PathManager.getInstance().useDefaultHomePath();
        }
    }

    private static void fillSubsystemList() {
        if (isHeadless) {
            subsystemList = Lists.newArrayList(new HeadlessGraphics(), new HeadlessTimer(), new HeadlessAudio(), new HeadlessInput());
        } else {
            subsystemList = Lists.<EngineSubsystem>newArrayList(new LwjglGraphics(), new LwjglTimer(), new LwjglAudio(), new LwjglInput());
        }
    }

    /**
     * The content of a throwable can be highly nested.
     * This method formats it into a single string that
     * can be displayed by the dialog opened through the
     * JOptionPane class.
     *
     * @param t A Throwable object.
     * @return A string, to be displayed by the message dialog.
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

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

import org.terasology.crashreporter.CrashReporter;
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Class providing the main() method for launching Terasology
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Kireev   Anton   <adeon.k87@gmail.com>
 *
 * This class can be used to start Terasology via command line and to customize
 * its launch characteristics. Default locations to store logs and game saves
 * can be overriden by using the current directory or a specified one as the
 * home directory. Furthermore, Terasology can be launched headless, to save
 * resources while acting as a server.
 *
 * To print out the command-line help and see usage examples:
 *
 *      terasology -help    or    terasology /?
 *
 * In case of crashes Terasology logs available information in <logpath>/Terasology.log
 */
public final class Terasology {

    private static final String[] PRINT_USAGE_FLAGS = {"--help", "-help", "/help", "-h", "/h",  "/?"};
    private static final String USE_CURRENT_DIR_AS_HOME = "-homedir";
    private static final String USE_SPECIFIED_DIR_AS_HOME = "-homedir=";
    private static final String START_HEADLESS = "-headless";

    private static boolean isHeadless;

    private Terasology() {
    }

    public static void main(String[] args) {

        handlePrintUsageRequest(args);
        handleLaunchArguments(args);

        try (TerasologyEngine engine = new TerasologyEngine(createSubsystemList())) {
            engine.init();
            if (isHeadless) {
                engine.run(new StateHeadlessSetup());
            } else {
                engine.run(new StateMainMenu());
            }
        } catch (Exception e) {
            if (!isHeadless) {
                logException(e);
            }
        }
        System.exit(0);
    }

    private static void handlePrintUsageRequest(String[] args) {
        for (String arg : args) {
            for (String usageArg : PRINT_USAGE_FLAGS) {
                if (usageArg.equals(arg.toLowerCase())) {
                    printUsageAndExit();
                }
            }
        }
    }

    private static void printUsageAndExit() {

        String usage =

            "Usage:\n" +
            "\n" +
            "    terasology [" + joinUsageFlags("|") + "] [" + USE_CURRENT_DIR_AS_HOME + "|" + USE_SPECIFIED_DIR_AS_HOME + "<path>] [" + START_HEADLESS + "]\n" +
            "\n" +
            "By default Terasology saves data such as game saves and logs into subfolders of a platform-specific \"home directory\".\n" +
            "Optionally, the user can override the default by using one of the following launch arguments:\n" +
            "\n" +
            "    " + USE_CURRENT_DIR_AS_HOME + "           Use the current directory as the home directory.\n" +
            "    " + USE_SPECIFIED_DIR_AS_HOME + "<path> Use the specified directory as the home directory.\n" +
            "\n" +
            "It is also possible to start Terasology in headless mode (no graphics), i.e. to act as a server.\n" +
            "For this purpose use the " + USE_SPECIFIED_DIR_AS_HOME + " launch argument.\n" +
            "\n" +
            "Examples:\n" +
            "\n" +
            "    Use the current directory as the home directory:\n" +
            "    terasology " + USE_CURRENT_DIR_AS_HOME + "\n" +
            "\n" +
            "    Use \"myPath\" as the home directory:\n" +
            "    terasology " + USE_SPECIFIED_DIR_AS_HOME + "myPath" + "\n" +
            "\n" +
            "    Start terasology in headless mode (no graphics):\n" +
            "    terasology " + START_HEADLESS + "\n" +
            "\n" +
            "    Don't start Terasology, just print this help:\n" +
            "    terasology " + PRINT_USAGE_FLAGS[1] + "\n" +
            "\n";

        System.out.println(usage);
        System.exit(0);
    }

    /**
     * Joins the items in PRINT_USAGE_FLAGS into a single string
     * using the separator provided. Example:
     *
     *      {"-help", "-h", "/?"}  becomes  "-help|-h|/?"
     */
    private static String joinUsageFlags(String separator) {
        String joinedFlags = PRINT_USAGE_FLAGS[0];
        for (int index = 1; index < PRINT_USAGE_FLAGS.length; index++) {
            joinedFlags += separator + PRINT_USAGE_FLAGS[index];
        }
        return joinedFlags;
    }

    private static void handleLaunchArguments(String[] args) {

        Path homePath = null;

        for (String arg : args) {
            if (arg.startsWith(USE_SPECIFIED_DIR_AS_HOME)) {
                homePath = Paths.get(arg.substring(USE_SPECIFIED_DIR_AS_HOME.length()));
            } else if (arg.equals(USE_CURRENT_DIR_AS_HOME)) {
                homePath = Paths.get("");
            } else if (arg.equals(START_HEADLESS)) {
                isHeadless = true;
            }
        }

        try {
            if (homePath != null) {
                PathManager.getInstance().useOverrideHomePath(homePath);
            } else {
                PathManager.getInstance().useDefaultHomePath();
            }

        } catch (IOException e) {
            if (!isHeadless) {
                logException(e);
            }
            System.exit(0);
        }
    }

    private static Collection<EngineSubsystem> createSubsystemList() {
        if (isHeadless) {
            return Lists.newArrayList(new HeadlessGraphics(), new HeadlessTimer(), new HeadlessAudio(), new HeadlessInput());
        } else {
            return Lists.<EngineSubsystem>newArrayList(new LwjglGraphics(), new LwjglTimer(), new LwjglAudio(), new LwjglInput());
        }
    }

    private static void logException(Exception e) {
        Path logPath = Paths.get(".");
        try {
            Path gameLogPath = PathManager.getInstance().getLogPath();
            if (gameLogPath != null) {
                logPath = gameLogPath;
            }
        } catch (Exception pathManagerConstructorFailure) {
            e.addSuppressed(pathManagerConstructorFailure);
        }

        Path logFile = logPath.resolve("Terasology.log");
        CrashReporter.report(e, logFile);
    }
}

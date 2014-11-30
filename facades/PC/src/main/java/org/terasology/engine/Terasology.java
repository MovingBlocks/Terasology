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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.engine.subsystem.headless.HeadlessAudio;
import org.terasology.engine.subsystem.headless.HeadlessGraphics;
import org.terasology.engine.subsystem.headless.HeadlessInput;
import org.terasology.engine.subsystem.headless.HeadlessTimer;
import org.terasology.engine.subsystem.headless.mode.HeadlessStateChangeListener;
import org.terasology.engine.subsystem.headless.mode.StateHeadlessSetup;
import org.terasology.engine.subsystem.lwjgl.LwjglAudio;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.engine.subsystem.lwjgl.LwjglInput;
import org.terasology.engine.subsystem.lwjgl.LwjglTimer;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * Class providing the main() method for launching Terasology as a PC app.
 *
 * Through the following launch arguments default locations to store logs and
 * game saves can be overridden, by using the current directory or a specified
 * one as the home directory. Furthermore, Terasology can be launched headless,
 * to save resources while acting as a server or to run in an environment with
 * no graphics, audio or input support. Additional arguments are available to
 * reload the latest game on startup and to disable crash reporting.
 *
 * Available launch arguments:
 *
 * <table>
 *  <tbody>
 *      <tr><td>-homedir</td><td>Use the current directory as the home directory.</td></tr>
 *      <tr><td>-homedir=path</td><td>Use the specified path as the home directory.</td></tr>
 *      <tr><td>-headless</td><td>Start headless.</td></tr>
 *      <tr><td>-loadlastgame</td><td>Load the latest game on startup.</td></tr>
 *      <tr><td>-noCrashReport</td><td>Disable crash reporting</td></tr>
 *  </tbody>
 * </table>
 *
 * When used via command line an usage help and some examples can be obtained via:
 *
 *      terasology -help    or    terasology /?
 *
 * In case of crashes Terasology logs available information in <logpath>/Terasology.log
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Kireev   Anton   <adeon.k87@gmail.com>
 */

public final class Terasology {

    private static final String[] PRINT_USAGE_FLAGS = {"--help", "-help", "/help", "-h", "/h", "-?", "/?"};
    private static final String USE_CURRENT_DIR_AS_HOME = "-homedir";
    private static final String USE_SPECIFIED_DIR_AS_HOME = "-homedir=";
    private static final String START_HEADLESS = "-headless";
    private static final String LOAD_LAST_GAME = "-loadlastgame";
    private static final String NO_CRASH_REPORT = "-noCrashReport";

    private static boolean isHeadless;
    private static boolean crashReportEnabled = true;
    private static boolean loadLastGame;

    private Terasology() {
    }

    public static void main(String[] args) {

        handlePrintUsageRequest(args);
        handleLaunchArguments(args);

        try (final TerasologyEngine engine = new TerasologyEngine(createSubsystemList())) {
            if (isHeadless) {
                engine.subscribeToStateChange(new HeadlessStateChangeListener());
                engine.run(new StateHeadlessSetup());
            } else {
                if (loadLastGame) {
                    engine.submitTask("loadGame", new Runnable() {
                        @Override
                        public void run() {
                            GameManifest gameManifest = getLatestGameManifest();
                            if (gameManifest != null) {
                                engine.changeState(new StateLoading(gameManifest, NetworkMode.NONE));
                            }
                        }
                    });
                }

                engine.run(new StateMainMenu());
            }
        } catch (RuntimeException e) {
            if (!GraphicsEnvironment.isHeadless()) {
                logException(e);
            }
        }
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

        String printUsageFlags = Joiner.on("|").join(PRINT_USAGE_FLAGS);

        System.out.println("Usage:");
        System.out.println();
        System.out.println("    terasology [" + printUsageFlags + "] [" + USE_CURRENT_DIR_AS_HOME + "|" + USE_SPECIFIED_DIR_AS_HOME + "<path>] [" + START_HEADLESS + "] [" + LOAD_LAST_GAME + "] [" + NO_CRASH_REPORT + "]");
        System.out.println();
        System.out.println("By default Terasology saves data such as game saves and logs into subfolders of a platform-specific \"home directory\".");
        System.out.println("Optionally, the user can override the default by using one of the following launch arguments:");
        System.out.println();
        System.out.println("    " + USE_CURRENT_DIR_AS_HOME + "           Use the current directory as the home directory.");
        System.out.println("    " + USE_SPECIFIED_DIR_AS_HOME + "<path> Use the specified directory as the home directory.");
        System.out.println();
        System.out.println("It is also possible to start Terasology in headless mode (no graphics), i.e. to act as a server.");
        System.out.println("For this purpose use the " + START_HEADLESS + " launch argument.");
        System.out.println();
        System.out.println("To automatically load the latest game on startup,");
        System.out.println("use the " + LOAD_LAST_GAME + " launch argument.");
        System.out.println();
        System.out.println("By default Crash Reporting is enabled.");
        System.out.println("To disable this feature use the " + NO_CRASH_REPORT + " launch argument.");
        System.out.println();
        System.out.println("Examples:");
        System.out.println();
        System.out.println("    Use the current directory as the home directory:");
        System.out.println("    terasology " + USE_CURRENT_DIR_AS_HOME);
        System.out.println();
        System.out.println("    Use \"myPath\" as the home directory:");
        System.out.println("    terasology " + USE_SPECIFIED_DIR_AS_HOME + "myPath");
        System.out.println();
        System.out.println("    Start terasology in headless mode (no graphics):");
        System.out.println("    terasology " + START_HEADLESS);
        System.out.println();
        System.out.println("    Load the latest game on startup and disable crash reporting");
        System.out.println("    terasology " + LOAD_LAST_GAME + " " + NO_CRASH_REPORT);
        System.out.println();
        System.out.println("    Don't start Terasology, just print this help:");
        System.out.println("    terasology " + PRINT_USAGE_FLAGS[1]);
        System.out.println();

        System.exit(0);
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
                crashReportEnabled = false;
            } else if (arg.equals(NO_CRASH_REPORT)) {
                crashReportEnabled = false;
            } else if (arg.equals(LOAD_LAST_GAME)) {
                loadLastGame = true;
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

        if (crashReportEnabled) {
            Path logFile = logPath.resolve("Terasology.log");
            CrashReporter.report(e, logFile);
        }
    }

    private static GameManifest getLatestGameManifest() {
        GameInfo latestGame = null;
        List<GameInfo> savedGames = GameProvider.getSavedGames();
        for (GameInfo savedGame : savedGames) {
            if (latestGame == null || savedGame.getTimestamp().after(latestGame.getTimestamp())) {
                latestGame = savedGame;
            }
        }

        if (latestGame == null) {
            return null;
        }

        return latestGame.getManifest();
    }

}

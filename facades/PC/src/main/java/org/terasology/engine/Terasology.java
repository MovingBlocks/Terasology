// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.StandardGameStatus;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.TerasologyEngineBuilder;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.core.subsystem.common.ConfigurationSubsystem;
import org.terasology.engine.core.subsystem.common.ThreadManager;
import org.terasology.engine.core.subsystem.common.hibernation.HibernationSubsystem;
import org.terasology.engine.core.subsystem.config.BindsSubsystem;
import org.terasology.engine.core.subsystem.headless.HeadlessAudio;
import org.terasology.engine.core.subsystem.headless.HeadlessGraphics;
import org.terasology.engine.core.subsystem.headless.HeadlessInput;
import org.terasology.engine.core.subsystem.headless.HeadlessTimer;
import org.terasology.engine.core.subsystem.headless.mode.HeadlessStateChangeListener;
import org.terasology.engine.core.subsystem.headless.mode.StateHeadlessSetup;
import org.terasology.engine.core.subsystem.lwjgl.LwjglAudio;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphics;
import org.terasology.engine.core.subsystem.lwjgl.LwjglInput;
import org.terasology.engine.core.subsystem.lwjgl.LwjglTimer;
import org.terasology.engine.core.subsystem.openvr.OpenVRInput;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.splash.SplashScreen;
import org.terasology.splash.SplashScreenBuilder;
import org.terasology.subsystem.discordrpc.DiscordRPCSubSystem;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class providing the main() method for launching Terasology as a PC app.
 * <p>
 * Through the following launch arguments default locations to store logs and game saves can be overridden, by using the
 * current directory or a specified one as the home directory. Furthermore, Terasology can be launched headless, to save
 * resources while acting as a server or to run in an environment with no graphics, audio or input support. Additional
 * arguments are available to reload the latest game on startup and to disable crash reporting.
 * <p>
 * When used via command line an usage help and some examples can be obtained via:
 * <p>
 * terasology --help    or    terasology /?
 *
 */

@CommandLine.Command(name = "terasology", usageHelpAutoWidth = true,
footer = "%nAlternatively use our standalone Launcher from%n https://github.com/MovingBlocks/TerasologyLauncher/releases")
public final class Terasology implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(Terasology.class);

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @SuppressWarnings("unused")
    @Option(names = {"--help", "-help", "/help", "-h", "/h", "-?", "/?"}, usageHelp = true, description = "show help")
    private boolean helpRequested;

    @Option(names = "--headless", description = "Start headless (no graphics)")
    private boolean isHeadless;

    @Option(names = "--crash-report", defaultValue = "true", negatable = true, description = "Enable crash reporting")
    private boolean crashReportEnabled;

    @Option(names = "--sound", defaultValue = "true", negatable = true, description = "Enable sound")
    private boolean soundEnabled;

    @Option(names = "--splash", defaultValue = "true", negatable = true, description = "Enable splash screen")
    private boolean splashEnabled;

    @Option(names = "--load-last-game", description = "Load the latest game on startup")
    private boolean loadLastGame;

    @Option(names = "--create-last-game", description = "Recreates the world of the latest game with a new save file on startup")
    private boolean createLastGame;

    @Option(names = "--permissive-security")
    private boolean permissiveSecurity;

    @Option(names = "--save-games", defaultValue = "true", negatable = true, description = "Enable new save games")
    private boolean saveGamesEnabled;

    @Option(names = "--server-port", description = "Change the server port")
    private Integer serverPort;

    @Option(names = "--override-default-config", description = "Override default config")
    private Path overrideConfigPath;

    @Option(names = "--homedir", description = "Path to home directory")
    private Path homeDir;

    private Terasology() {
    }

    public static void main(String[] args) {
        new CommandLine(new Terasology()).execute(args);
    }

    @Override
    public Integer call() throws IOException {
        handleLaunchArguments();
        setupLogging();

        SplashScreen splashScreen;
        if (splashEnabled) {
            CountDownLatch splashInitLatch = new CountDownLatch(1);
            GLFWSplashScreen glfwSplash = new GLFWSplashScreen(splashInitLatch);
            Thread thread = new Thread(glfwSplash, "splashscreen-loop");
            thread.setDaemon(true);
            thread.start();
            try {
                // wait splash initialize... we will lose some post messages otherwise.
                //noinspection ResultOfMethodCallIgnored
                splashInitLatch.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
            splashScreen = glfwSplash;
        } else {
            splashScreen = SplashScreenBuilder.createStub();
        }
        splashScreen.post("Java Runtime " + System.getProperty("java.version") + " loaded");

        try {
            TerasologyEngineBuilder builder = new TerasologyEngineBuilder();
            populateSubsystems(builder);
            TerasologyEngine engine = builder.build();
            engine.subscribe(newStatus -> {
                if (newStatus == StandardGameStatus.RUNNING) {
                    splashScreen.close();
                } else {
                    splashScreen.post(newStatus.getDescription());
                }
            });

            if (isHeadless) {
                engine.subscribeToStateChange(new HeadlessStateChangeListener(engine));
                engine.run(new StateHeadlessSetup());
            } else if (loadLastGame) {
                engine.initialize(); //initialize the managers first
                engine.getFromEngineContext(ThreadManager.class).submitTask("loadGame", () -> {
                    GameManifest gameManifest = getLatestGameManifest();
                    if (gameManifest != null) {
                        engine.changeState(new StateLoading(gameManifest, NetworkMode.NONE));
                    }
                });
            } else {
                if (createLastGame) {
                    engine.initialize();
                    engine.getFromEngineContext(ThreadManager.class).submitTask("createLastGame", () -> {
                        GameManifest gameManifest = getLatestGameManifest();
                        if (gameManifest != null) {
                            String title = gameManifest.getTitle();
                            if (!title.startsWith("New Created")) { //if first time run
                                gameManifest.setTitle("New Created " + title + " 1");
                            } else { //if not first time run
                                gameManifest.setTitle(getNewTitle(title));
                            }
                            engine.changeState(new StateLoading(gameManifest, NetworkMode.NONE));
                        }
                    });
                }

                engine.run(new StateMainMenu());
            }
        } catch (Throwable e) {
            // also catch Errors such as UnsatisfiedLink, NoSuchMethodError, etc.
            splashScreen.close();
            reportException(e);
        }

        return 0;
    }

    private static String getNewTitle(String title) {
        String newTitle = title.substring(0, getPositionOfLastDigit(title));
        int fileNumber = getLastNumber(title);
        fileNumber++;
        return (newTitle + " " + fileNumber);
    }

    private static void setupLogging() {
        Path path = PathManager.getInstance().getLogPath();
        if (path == null) {
            path = Paths.get("logs");
        }

        LoggingContext.initialize(path);
    }

    private void handleLaunchArguments() throws IOException {
        if (homeDir != null) {
            logger.info("homeDir is {}", homeDir);
            PathManager.getInstance().useOverrideHomePath(homeDir);
            // TODO: what is this?
            //   PathManager.getInstance().chooseHomePathManually();
        } else {
            PathManager.getInstance().useDefaultHomePath();
        }

        if (isHeadless) {
            crashReportEnabled = false;
            splashEnabled = false;
        }
        if (!saveGamesEnabled) {
            System.setProperty(SystemConfig.SAVED_GAMES_ENABLED_PROPERTY, "false");
        }
        if (permissiveSecurity) {
            System.setProperty(SystemConfig.PERMISSIVE_SECURITY_ENABLED_PROPERTY, "true");
        }
        if (serverPort != null) {
            System.setProperty(ConfigurationSubsystem.SERVER_PORT_PROPERTY, serverPort.toString());
        }
        if (overrideConfigPath != null) {
            System.setProperty(Config.PROPERTY_OVERRIDE_DEFAULT_CONFIG, overrideConfigPath.toString());
        }
    }

    private void populateSubsystems(TerasologyEngineBuilder builder) {
        if (isHeadless) {
            builder.add(new HeadlessGraphics())
                    .add(new HeadlessTimer())
                    .add(new HeadlessAudio())
                    .add(new HeadlessInput());
        } else {
            EngineSubsystem audio = soundEnabled ? new LwjglAudio() : new HeadlessAudio();
            builder.add(audio)
                    .add(new LwjglGraphics())
                    .add(new LwjglTimer())
                    .add(new LwjglInput())
                    .add(new BindsSubsystem())
                    .add(new OpenVRInput());
            builder.add(new DiscordRPCSubSystem());
        }
        builder.add(new HibernationSubsystem());
    }

    private void reportException(Throwable throwable) {
        Path logPath = LoggingContext.getLoggingPath();

        if (!GraphicsEnvironment.isHeadless() && crashReportEnabled) {
            CrashReporter.report(throwable, logPath);
        } else {
            throwable.printStackTrace();
            System.err.println("For more details, see the log files in " + logPath.toAbsolutePath().normalize());
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

    private static int getPositionOfLastDigit(String str) {
        int position;
        for (position = str.length() - 1; position >= 0; --position) {
            char c = str.charAt(position);
            if (!Character.isDigit(c)) {
                break;
            }
        }
        return position + 1;
    }

    private static int getLastNumber(String str) {
        int positionOfLastDigit = getPositionOfLastDigit(str);
        if (positionOfLastDigit == str.length()) {
            // string does not end in digits
            return -1;
        }
        return Integer.parseInt(str.substring(positionOfLastDigit));
    }

}

// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine;

import com.sun.jna.Platform;
import com.sun.jna.platform.unix.LibC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.StandardGameStatus;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.TerasologyEngineBuilder;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.core.subsystem.common.ConfigurationSubsystem;
import org.terasology.engine.core.subsystem.common.hibernation.HibernationSubsystem;
import org.terasology.engine.core.subsystem.config.BindsSubsystem;
import org.terasology.engine.core.subsystem.headless.HeadlessAudio;
import org.terasology.engine.core.subsystem.headless.HeadlessGraphics;
import org.terasology.engine.core.subsystem.headless.HeadlessTimer;
import org.terasology.engine.core.subsystem.headless.mode.HeadlessStateChangeListener;
import org.terasology.engine.core.subsystem.headless.mode.StateHeadlessSetup;
import org.terasology.engine.core.subsystem.lwjgl.LwjglAudio;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphics;
import org.terasology.engine.core.subsystem.lwjgl.LwjglInput;
import org.terasology.engine.core.subsystem.lwjgl.LwjglTimer;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

@CommandLine.Command(
        name = "terasology",
        usageHelpAutoWidth = true,
        footer = "%n" +
                "For details, see%n" +
                " https://github.com/MovingBlocks/Terasology/wiki/Advanced-Options%n" +
                "%n" +
                "Alternatively use our standalone Launcher from%n" +
                " https://github.com/MovingBlocks/TerasologyLauncher/releases"
)
public final class Terasology implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(Terasology.class);

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @SuppressWarnings("unused")
    @Option(names = {"--help", "-help", "/help", "-h", "/h", "-?", "/?"}, usageHelp = true, description = "Show help")
    private boolean helpRequested;

    @Option(names = "--headless", description = "Start headless (no graphics)")
    private boolean isHeadless;

    @Option(names = "--max-data-size",
            description = "Set maximum process data size [Linux only]",
            paramLabel = "<size>",
            converter = DataSizeConverter.class
    )
    private Long maxDataSize;

    @Option(names = "--oom-score",
            description = "Adjust out-of-memory score [Linux only]",
            paramLabel = "<score>"
    )
    private Integer outOfMemoryScore;

    @Option(names = "--crash-report", defaultValue = "true", fallbackValue = "true", negatable = true, description = "Enable crash reporting")
    private boolean crashReportEnabled;

    @Option(names = "--sound", defaultValue = "true", fallbackValue = "true", negatable = true, description = "Enable sound")
    private boolean soundEnabled;

    @Option(names = "--splash", defaultValue = "true", fallbackValue = "true", negatable = true, description = "Enable splash screen")
    private boolean splashEnabled;

    @Option(names = "--load-last-game", description = "Load the latest game on startup")
    private boolean loadLastGame;

    @Option(names = "--create-last-game", description = "Recreates the world of the latest game with a new save file on startup")
    private boolean createLastGame;

    @Option(names = "--permissive-security")
    private boolean permissiveSecurity;

    @Option(names = "--save-games", defaultValue = "true", fallbackValue = "true", negatable = true, description = "Enable new save games")
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
            }

            engine.initialize(); //initialize the managers first

            GameState nextState = chainMainMenuToWorkAroundBug1127(selectNextGameState());
            if (nextState == null) {
                return 1;
            }

            engine.run(nextState);
        } catch (Throwable e) {
            // also catch Errors such as UnsatisfiedLink, NoSuchMethodError, etc.
            splashScreen.close();
            reportException(e);
            return 1;
        }

        return 0;
    }

    private GameState selectNextGameState() {
        GameState nextState;

        GameManifest gameManifest = getLatestGameManifest();

        if (isHeadless) {
            nextState = new StateHeadlessSetup();
        } else if (loadLastGame) {
            if (gameManifest == null) {
                logger.error("Failed --load-last-game: last game not found.");
                return null;
            }
            nextState = new StateLoading(gameManifest, NetworkMode.NONE);
        } else if (createLastGame) {
            if (gameManifest == null) {
                logger.error("Failed --create-last-game: last game not found.");
                return null;
            }
            String title = gameManifest.getTitle();
            if (!title.startsWith("New Created")) { //if first time run
                gameManifest.setTitle("New Created " + title + " 1");
            } else { //if not first time run
                gameManifest.setTitle(getNewTitle(title));
            }
            nextState = new StateLoading(gameManifest, NetworkMode.NONE);
        } else {
            nextState = new StateMainMenu();
        }

        return nextState;
    }

    /**
     * Chain states to load after MainMenu.
     * <p>
     * Things are broken when we're not headless and try to skip MainMenu entirely.
     *
     * @see <a href="https://github.com/MovingBlocks/Terasology/issues/1126">#1126</a>
     * @see <a href="https://github.com/MovingBlocks/Terasology/issues/1127">#1127</a>
     */
    private GameState chainMainMenuToWorkAroundBug1127(GameState nextState) {
        if (isHeadless || nextState == null || nextState instanceof StateMainMenu) {
            return nextState;
        }
        return new StateMainMenu() {
            @Override
            public void init(GameEngine gameEngine) {
                super.init(gameEngine);
                gameEngine.changeState(nextState);
            }
        };
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
        if (outOfMemoryScore != null) {
            adjustOutOfMemoryScore(outOfMemoryScore);
        }
        if (maxDataSize != null) {
            setMemoryLimit(maxDataSize);
        }

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
                    .add(new HeadlessAudio());
        } else {
            EngineSubsystem audio = soundEnabled ? new LwjglAudio() : new HeadlessAudio();
            builder.add(audio)
                    .add(new LwjglGraphics())
                    .add(new LwjglTimer())
                    .add(new LwjglInput())
                    .add(new BindsSubsystem());
            builder.add(new DiscordRPCSubSystem());
        }
        builder.add(new HibernationSubsystem());
    }

    @SuppressWarnings({"PMD.SystemPrintln", "PMD.AvoidPrintStackTrace"})
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

    /**
     * Limit the amount of memory the operating system will allow this program.
     * <p>
     * Enforced by the operating system instead of the Java Virtual Machine, this limits memory usage
     * in a different way than setting Java's maximum heap size (the <code>-Xmx</code> java option).
     * Use this to prevent Terasology from gobbling all your system memory if it has a memory leak.
     * <p>
     * Set this limit to a number larger than the maximum java heap size. It is normal for a process to
     * need <em>some</em> additional memory outside the java heap.
     * <p>
     * This is currently only implemented on Linux.
     * <p>
     * On Windows, you may be able to set a limit using one of these external tools:
     * <ul>
     *   <li>
     *     <a href="https://docs.microsoft.com/en-us/windows-hardware/drivers/devtest/application-verifier">
     *       Application Verifier (<code>AppVerif.exe</code>)
     *     </a>
     *     , available from the Windows SDK
     *   </li>
     *   <li>
     *     <a href="https://github.com/lowleveldesign/process-governor">
     *       Process Governor (<code>procgov</code>)
     *     </a>
     *     , an open source third-party tool
     *   </li>
     * </ul>
     * @param bytes maximum allowed size
     * @see <a href="https://docs.oracle.com/en/java/javase/11/tools/java.html#GUID-3B1CE181-CD30-4178-9602-230B800D4FAE">Java command-line params</a>
     * @see <a href="https://man7.org/linux/man-pages/man2/setrlimit.2.html">setrlimit(2)</a>
     */
    private static void setMemoryLimit(long bytes) {
        // Memory-limiting techniques are highly platform-specific.
        if (Platform.isLinux()) {
            final LibC.Rlimit dataLimit = new LibC.Rlimit();
            dataLimit.rlim_cur = bytes;
            dataLimit.rlim_max = bytes;
            // Under Linux ≥ 4.7, we can limit the maximum size of the process's data segment, which includes its
            // heap. Note we cannot directly limit its resident set size, see setrlimit(2).
            LibC.INSTANCE.setrlimit(LibC.RLIMIT_DATA, dataLimit);
        } else {
            // OS X does have setrlimit(), but as far as we can tell, it is not enforced for RLIMIT_DATA:
            //   https://stackoverflow.com/questions/3274385/
            // There is an API that might have a similar effect on Windows:
            //   https://docs.microsoft.com/en-us/windows/win32/procthread/job-objects
            logger.warn("--max-data-size is not supported on platform {}", Platform.RESOURCE_PREFIX);
        }
    }

    /**
     * Make the Linux Out-of-Memory killer more likely to pick Terasology.
     * <p>
     * When a Linux system runs out of available memory, it invokes the Out of Memory killer (aka <i>OOM killer</i>) to
     * choose a process to terminate to free up some memory.
     * <p>
     * Add to this score if you want to make Terasology a bigger target. Why? If you'd rather the game process be the
     * thing that gets killed instead of some other memory-hungry program, like your browser or IDE. A score of 1000 is
     * equivalent to saying “this process is taking <em>all</em> the memory.”
     * <p>
     * This out-of-memory score is a Linux-specific mechanism.
     *
     * @param adjustment how much worse to make the score, 0–1000
     *
     * @see <a href="https://man7.org/linux/man-pages/man5/proc.5.html#:~:text=/proc/%5Bpid%5D/-,oom_score_adj,-(since">proc(5)</a>
     */
    private static void adjustOutOfMemoryScore(int adjustment) {
        Path procFile = Paths.get("/proc", "self", "oom_score_adj");
        try {
            Files.write(procFile, String.valueOf(adjustment).getBytes(),
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.error("Failed to adjust out-of-memory score.", e);
        }
    }
}

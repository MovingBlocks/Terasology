// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.utilities.OS;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Manager class that keeps track of the game's various paths and save directories.
 */
public final class PathManager {
    private static final String TERASOLOGY_FOLDER_NAME = "Terasology";
    private static final Path LINUX_HOME_SUBPATH = Paths.get(".local", "share", "terasology");

    private static final String SAVED_GAMES_DIR = "saves";
    private static final String RECORDINGS_LIBRARY_DIR = "recordings";
    private static final String LOG_DIR = "logs";
    private static final String SHADER_LOG_DIR = "shaders";
    private static final String MODULE_DIR = "modules";
    private static final String MODULE_CACHE_DIR = "cachedModules";
    private static final String SCREENSHOT_DIR = "screenshots";
    private static final String NATIVES_DIR = "natives";
    private static final String CONFIGS_DIR = "configs";
    private static final String SANDBOX_DIR = "sandbox";
    private static final String REGEX = "[^A-Za-z0-9-_ ]";

    private static PathManager instance;

    private static Context context;
    private Path installPath;
    private Path homePath;
    private Path savesPath;
    private Path recordingsPath;
    private Path logPath;
    private Path shaderLogPath;
    private Path currentWorldPath;
    private Path sandboxPath;

    private ImmutableList<Path> modPaths = ImmutableList.of();
    private Path screenshotPath;
    private Path nativesPath;
    private Path configsPath;

    private PathManager() {
        installPath = findInstallPath();
        homePath = installPath;
    }

    private static Path findInstallPath() {
        Path codeLocation;  // By default, the path should be the code location (where terasology.jar is)
        try {
            URI urlToSource = PathManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            codeLocation = Paths.get(urlToSource);
            // Not using logger because it's usually initialized after PathManager.
            System.out.println("PathManager: Initial code location is " + codeLocation.toAbsolutePath());
        } catch (URISyntaxException e) {
            System.err.println("PathManager: Failed to convert code location to path.");
            e.printStackTrace();
            codeLocation = Paths.get("").toAbsolutePath();
            System.out.println("PathManager: Using working dir " + codeLocation);
        }

        return Verify.verifyNotNull(findNativesHome(codeLocation.getParent(), 5),
                "Unable to find the natives directory from %s, unable to launch!",
                codeLocation);
    }

    /**
     * Searches for a parent directory containing the natives directory
     *
     * @param startPath path to start from
     * @param maxDepth  max directory levels to search
     * @return the adjusted path containing the natives directory or null if not found
     */
    private static Path findNativesHome(Path startPath, int maxDepth) {
        int levelsToSearch = maxDepth;
        Path checkedPath = startPath;
        while (levelsToSearch > 0) {
            File dirToTest = new File(checkedPath.toFile(), NATIVES_DIR);
            if (dirToTest.exists()) {
                return checkedPath;
            }

            checkedPath = checkedPath.getParent();
            if (checkedPath.equals(startPath.getRoot())) {
                System.err.println("Uh oh, reached the root path, giving up");
                return null;
            }
            levelsToSearch--;
        }

        System.err.println("Failed to find the natives dir within " + maxDepth + " levels of " + startPath);
        return null;
    }

    /**
     *
     * @return An instance of the path manager for this execution.
     */
    public static PathManager getInstance() {
        if (instance == null) {
            instance = new PathManager();
        }
        return instance;
    }

    /**
     * INTERNAL: use only for testing!
     *
     * Inject a path manager instance to be used as the "singleton" instance.
     *
     * @param pathManager the new "singleton" instance, will be returned by subsequent calls to {@link #getInstance()}
     * @return the old path manager instance
     */
    static PathManager setInstance(PathManager pathManager) {
        PathManager oldInstance = instance;
        instance = pathManager;
        return oldInstance;
    }

    /**
     * Uses the given path as the home instead of the default home path.
     * @param rootPath Path to use as the home path.
     * @throws IOException Thrown when required directories cannot be accessed.
     */
    public void useOverrideHomePath(Path rootPath) throws IOException {
        this.homePath = rootPath.toRealPath();
        updateDirs();
    }

    /**
     * Uses a platform-specific default home path for this execution.
     * @throws IOException Thrown when required directories cannot be accessed.
     */
    public void useDefaultHomePath() throws IOException {
        switch (OS.get()) {
            case LINUX:
                homePath = Paths.get(System.getProperty("user.home")).resolve(LINUX_HOME_SUBPATH);
                break;
            case MACOSX:
                homePath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", TERASOLOGY_FOLDER_NAME);
                break;
            case WINDOWS:
                String savedGamesPath = Shell32Util
                    .getKnownFolderPath(KnownFolders.FOLDERID_SavedGames);
                if (savedGamesPath == null) {
                    savedGamesPath = Shell32Util
                        .getKnownFolderPath(KnownFolders.FOLDERID_Documents);
                }
                Path rawPath;
                if (savedGamesPath != null) {
                    rawPath = Paths.get(savedGamesPath);
                } else {
                    rawPath = new JFileChooser().getFileSystemView().getDefaultDirectory()
                        .toPath();
                }
                homePath = rawPath.resolve(TERASOLOGY_FOLDER_NAME);
                break;
            default:
                homePath = Paths.get(System.getProperty("user.home")).resolve(LINUX_HOME_SUBPATH);
                break;
        }
        updateDirs();
    }

    /**
     * Gives user the option to manually choose home path.
     * @throws IOException Thrown when required directories cannot be accessed.
     */
    public void chooseHomePathManually() throws IOException {
        DisplayDevice display = context.get(DisplayDevice.class);
        boolean isHeadless = display.isHeadless();
        if (!isHeadless) {
            Path rawPath = new JFileChooser().getFileSystemView().getDefaultDirectory()
                .toPath();
            homePath = rawPath.resolve("Terasology");
        } else {
            // If the system is headless
            homePath = Paths.get("").toAbsolutePath();
        }
        updateDirs();
    }

    /**
     *
     * @return This execution's home path.
     */
    public Path getHomePath() {
        return homePath;
    }

    /**
     *
     * @return The path of the running installation.
     */
    public Path getInstallPath() {
        return installPath;
    }

    /**
     *
     * @return Path in which world saves are saved.
     */
    public Path getSavesPath() {
        return savesPath;
    }

    /**
     *
     * @return Path in which recordings are saved.
     */
    public Path getRecordingsPath() {
        return recordingsPath;
    }

    /**
     *
     * @return Path in which this execution's logs are saved.
     */
    public Path getLogPath() {
        return logPath;
    }

    /**
     *
     * @return Path in which this execution's shader logs are saved.
     */
    public Path getShaderLogPath() {
        return shaderLogPath;
    }

    /**
     *
     * @return List of paths to all of the modules.
     */
    public List<Path> getModulePaths() {
        return modPaths;
    }

    /**
     *
     * @return Path in which this execution's screen-shots are saved.
     */
    public Path getScreenshotPath() {
        return screenshotPath;
    }

    /**
     *
     * @return Path in which the game's native libraries are saved.
     */
    public Path getNativesPath() {
        return nativesPath;
    }

    /**
     *
     * @return Path in which the game's config files are saved.
     */
    public Path getConfigsPath() {
        return configsPath;
    }

    /**
     *
     * @return Path in which the modules are allowed to save files.
     */
    public Path getSandboxPath() {
        return sandboxPath;
    }

    /**
     * Updates all of the path manager's file/directory references to match the path settings. Creates directories if they don't already exist.
     * @throws IOException Thrown when required directories cannot be accessed.
     */
    private void updateDirs() throws IOException {
        savesPath = homePath.resolve(SAVED_GAMES_DIR);
        recordingsPath = homePath.resolve(RECORDINGS_LIBRARY_DIR);
        logPath = homePath.resolve(LOG_DIR);
        shaderLogPath = logPath.resolve(SHADER_LOG_DIR);
        screenshotPath = homePath.resolve(SCREENSHOT_DIR);
        nativesPath = installPath.resolve(NATIVES_DIR);
        configsPath = homePath.resolve(CONFIGS_DIR);
        if (currentWorldPath == null) {
            currentWorldPath = homePath;
        }
        sandboxPath = homePath.resolve(SANDBOX_DIR);

        modPaths = defaultModPaths();

        for (Path path : getAllPaths()) {
            Files.createDirectories(path);
        }

        // --------------------------------- Setup native paths ---------------------
        final Path path;
        switch (OS.get()) {
            case WINDOWS:
                path = nativesPath.resolve("windows");
                break;
            case MACOSX:
                path = nativesPath.resolve("macosx");
                break;
            case LINUX:
                path = nativesPath.resolve("linux");
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operating system: " + System.getProperty("os" +
                        ".name"));
        }
        final String natives = path.toAbsolutePath().toString();
        System.setProperty("org.lwjgl.librarypath", natives);
        System.setProperty("net.java.games.input.librarypath", natives);  // libjinput
        System.setProperty("org.terasology.librarypath", natives); // JNBullet

    }

    protected ImmutableList<Path> defaultModPaths() throws IOException {
        Path homeModPath = homePath.resolve(MODULE_DIR);
        Path modCachePath = homePath.resolve(MODULE_CACHE_DIR);

        if (homePath.equals(installPath)) {
            return ImmutableList.of(modCachePath, homeModPath);
        } else {
            Path installModPath = installPath.resolve(MODULE_DIR);
            return ImmutableList.of(installModPath, modCachePath, homeModPath);
        }
    }

    public Path getHomeModPath() {
        return modPaths.get(0);
    }

    public Path getSavePath(String title) {
        return getSavesPath().resolve(title.replaceAll(REGEX, ""));
    }

    public Path getRecordingPath(String title) {
        return getRecordingsPath().resolve(title.replaceAll(REGEX, ""));
    }

    public Path getSandboxPath(String title) {
        return getSandboxPath().resolve(title.replaceAll(REGEX, ""));
    }

    /**
     * The Path value from a Field.
     *
     * Provided as a workaround for the fact that we can't have checked exceptions in iterator methods.
     */
    private Path getField(Field field) {
        try {
            return (Path) field.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get own field " + field, e);
        }
    }

    /** All Paths known to this PathManager. */
    private List<Path> getAllPaths() {
        // This uses reflection to be less likely to be out of date after we add more Path fields.
        List<Path> allPaths = Arrays.stream(PathManager.class.getDeclaredFields())
                .filter(field -> Path.class.isAssignableFrom(field.getType()))
                .map(this::getField).collect(Collectors.toList());
        allPaths.addAll(modPaths);
        return allPaths;
    }
}

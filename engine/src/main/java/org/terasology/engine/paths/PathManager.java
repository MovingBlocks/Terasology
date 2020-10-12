// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.paths;

import com.google.common.collect.ImmutableList;
import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.utilities.OS;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


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
    private static final String MOD_DIR = "modules";
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
        // By default, the path should be the code location (where terasology.jar is)
        try {
            URL urlToSource = PathManager.class.getProtectionDomain().getCodeSource().getLocation();

            // TODO: Probably remove this after a while, confirming via logs that this is no longer needed
            Path codeLocation = Paths.get(urlToSource.toURI());
            System.out.println("PathManager being initialized. Initial code location is " + codeLocation.toAbsolutePath());

            // Theory that whatever reason we needed to get the path that way isn't needed anymore - try just current dir ...
            codeLocation = Paths.get("").toAbsolutePath();
            System.out.println("Switched it to expected working dir: " + codeLocation.toAbsolutePath());

            // If that fails in some situations a different approach could test for the following in the path:
            // if (codeLocation.toString().contains(".gradle") || codeLocation.toString().contains(".m2")) {

            if (Files.isRegularFile(codeLocation)) {
                installPath = findNativesHome(codeLocation.getParent(), 5);
                if (installPath == null) {
                    System.out.println("Failed to find the natives dir - unable to launch!");
                    throw new RuntimeException("Failed to find natives from .jar launch");
                }
            }
        } catch (URISyntaxException e) {
            // Can't use logger, because logger not set up when PathManager is used.
            System.out.println("Failed to convert code location to uri");
        }
        // We might be running from an IDE which can cause the installPath to be null. Try current working directory.
        if (installPath == null) {
            installPath = Paths.get("").toAbsolutePath();
            System.out.println("installPath was null, running from IDE or headless server? Setting to: " + installPath);
            installPath = findNativesHome(installPath, 5);
            if (installPath == null) {
                System.out.println("Failed to find the natives dir - unable to launch!");
                throw new RuntimeException("Failed to find natives from likely IDE launch");
            }
        }
        homePath = installPath;
    }

    /**
     * Searches for a parent directory containing the natives directory
     *
     * @param startPath path to start from
     * @param maxDepth  max directory levels to search
     * @return the adjusted path containing the natives directory or null if not found
     */
    private Path findNativesHome(Path startPath, int maxDepth) {
        int levelsToSearch = maxDepth;
        Path checkedPath = startPath;
        while (levelsToSearch > 0) {
            File dirToTest = new File(checkedPath.toFile(), NATIVES_DIR);
            if (dirToTest.exists()) {
                System.out.println("Found the natives dir: " + dirToTest);
                return checkedPath;
            }

            checkedPath = checkedPath.getParent();
            if (checkedPath.equals(startPath.getRoot())) {
                System.out.println("Uh oh, reached the root path, giving up");
                return null;
            }
            levelsToSearch--;
        }

        System.out.println("Failed to find the natives dir within " + maxDepth + " levels of " + startPath);
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
     * Uses the given path as the home instead of the default home path.
     * @param rootPath Path to use as the home path.
     * @throws IOException Thrown when required directories cannot be accessed.
     */
    public void useOverrideHomePath(Path rootPath) throws IOException {
        this.homePath = rootPath;
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
        Files.createDirectories(homePath);
        savesPath = homePath.resolve(SAVED_GAMES_DIR);
        Files.createDirectories(savesPath);
        recordingsPath = homePath.resolve(RECORDINGS_LIBRARY_DIR);
        Files.createDirectories(recordingsPath);
        logPath = homePath.resolve(LOG_DIR);
        Files.createDirectories(logPath);
        shaderLogPath = logPath.resolve(SHADER_LOG_DIR);
        Files.createDirectories(shaderLogPath);
        Path homeModPath = homePath.resolve(MOD_DIR);
        Files.createDirectories(homeModPath);
        Path installModPath = installPath.resolve(MOD_DIR);
        Files.createDirectories(installModPath);
        if (Files.isSameFile(homeModPath, installModPath)) {
            modPaths = ImmutableList.of(homeModPath);
        } else {
            modPaths = ImmutableList.of(installModPath, homeModPath);
        }
        screenshotPath = homePath.resolve(SCREENSHOT_DIR);
        Files.createDirectories(screenshotPath);
        nativesPath = installPath.resolve(NATIVES_DIR);
        configsPath = homePath.resolve(CONFIGS_DIR);
        if (currentWorldPath == null) {
            currentWorldPath = homePath;
        }
        sandboxPath = homePath.resolve(SANDBOX_DIR);
        Files.createDirectories(sandboxPath);

        // --------------------------------- Setup native paths ---------------------
        final Path path;
        switch (OS.get()) {
            case WINDOWS:
                path = PathManager.getInstance().getNativesPath().resolve("windows");
                break;
            case MACOSX:
                path = PathManager.getInstance().getNativesPath().resolve("macosx");
                break;
            case LINUX:
                path = PathManager.getInstance().getNativesPath().resolve("linux");
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
}

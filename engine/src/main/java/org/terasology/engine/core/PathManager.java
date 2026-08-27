// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import com.google.common.collect.ImmutableList;
import dev.dirs.ProjectDirectories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.utilities.OS;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Manager class that keeps track of the game's various paths and save directories.
 */
public final class PathManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathManager.class);
    private static final ProjectDirectories PROJECT_DIRS = ProjectDirectories.from("org", "terasology", "terasology");
    private static final Path PROJECT_PATH = Paths.get(PROJECT_DIRS.dataDir);
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

    // Logs and the module cache have a real OS-standard home (dataLocalDir / cacheDir) that's
    // different from where saves/configs/etc. live (dataDir). That split only makes sense while
    // homePath is the OS default; once something picks its own homePath (--homedir, or the user
    // choosing one), there's no separate OS-standard location to defer to anymore, so everything
    // - logs and module cache included - nests under that chosen homePath instead. See updateDirs().
    private boolean usingOsStandardDirs = true;

    private PathManager() {
        installPath = findInstallPath();
        // Only a fallback for whoever constructs a PathManager without then calling useDefaultHomePath()/
        // useOverrideHomePath()/chooseHomePathManually() - the normal launch path always calls one of those
        // before this default is ever read. findInstallPath() already has its own fallback (the current
        // directory) for when native-library detection fails, e.g. in a dev workspace; keeping homePath in
        // step with that here means an unconfigured PathManager still behaves the way it always has instead
        // of silently switching to the OS home directory underneath something that isn't expecting it to.
        homePath = installPath;
    }

    private static Path findInstallPath() {
        List<Path> installationSearchPaths = new ArrayList<>(2);

        try {
            // In a normal workspace or distribution, the jar with this code is somewhere near the natives directory.
            URI urlToSource = PathManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path codeLocation = Paths.get(urlToSource);
            installationSearchPaths.add(codeLocation);
            LOGGER.atInfo().log("PathManager: Initial code location is " + codeLocation.toAbsolutePath());
        } catch (URISyntaxException e) {
            LOGGER.error("PathManager: Failed to convert code location to path.", e);
        }

        // But that's not always true. This jar may be loaded from somewhere else on the classpath.
        // For example: CI runs module unit tests in a smaller workspace, and gradle gets engine.jar
        // the same as all its other dependencies, disconnected from the natives directory.
        //
        // Use the current directory as a fallback.
        Path currentDirectory = Paths.get("").toAbsolutePath();
        installationSearchPaths.add(currentDirectory);
        LOGGER.info("PathManager: Working directory is {}", currentDirectory);

        for (Path startPath : installationSearchPaths) {
            Path installationPath = findNativesHome(startPath, 5);
            if (installationPath != null) {
                return installationPath;
            }
        }

        LOGGER.error(
                "Native library installation directory not found. /n" +
                "Things will almost certainly crash as a result, /n" +
                "unless something else installed everything to java.library.path. /n" +
                "Searched: {}/n", installationSearchPaths
        );
        return currentDirectory;
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
                break;  // Uh oh, reached the root path, giving up.
            }
            levelsToSearch--;
        }
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
     * Uses the given path as the home instead of the default home path. Especially interesting for unit tests, as java>17 does not
     * make it easy to set environment variables. see: https://www.baeldung.com/java-unit-testing-environment-variables .
     *
     * Everything updateDirs() computes - saves, logs, shader logs, the module cache, and the rest -
     * nests under whatever homePath is set to here, so callers of this method (notably
     * TerasologyLauncher, via {@code --homedir}) get a fully self-contained tree at the path they
     * asked for, not just the save data.
     *
     * @param rootPath Path to use as the home path.
     * @throws IOException Thrown when required directories cannot be accessed.
     */
    public void useOverrideHomePath(Path rootPath) throws IOException {
        this.homePath = rootPath.toRealPath();
        usingOsStandardDirs = false;
        updateDirs();
    }

    /**
     * Uses a platform-specific default home path for this execution.
     * @throws IOException Thrown when required directories cannot be accessed.
     */
    public void useDefaultHomePath() throws IOException {
        // use datadir, .local/share for linux e.g.
        homePath = PROJECT_PATH;
        usingOsStandardDirs = true;
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
        usingOsStandardDirs = false;
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
        // Logs are state, not data - $XDG_STATE_HOME on Linux, not dev.dirs' dataDir/dataLocalDir.
        // dev.dirs has no state_dir field at all (it predates that part of the spec), so resolveStateDir()
        // reads $XDG_STATE_HOME itself on Linux. macOS/Windows have no OS-standard state location
        // either, so dataLocalDir stays the fallback there. Only used while homePath itself is still
        // the OS default - once homePath is chosen by something else (--homedir, manual pick), logs
        // move under it too so the whole tree stays self-contained.
        Path logBase = usingOsStandardDirs ? resolveStateDir() : homePath;
        logPath = logBase.resolve(LOG_DIR);
        shaderLogPath = logPath.resolve(SHADER_LOG_DIR);
        screenshotPath = homePath.resolve(SCREENSHOT_DIR);
        nativesPath = installPath.resolve(NATIVES_DIR);
        // configDir is dev.dirs' own OS-standard location for config (XDG_CONFIG_HOME on Linux,
        // \config under RoamingAppData on Windows) - genuinely separate from dataDir there. On macOS
        // dev.dirs has no such split; configDir just points back at the same Application Support
        // folder as dataDir/homePath, so still appending CONFIGS_DIR keeps config files in their own
        // subfolder there too, instead of dumping them loose at the tree root.
        Path configBase = usingOsStandardDirs ? Paths.get(PROJECT_DIRS.configDir) : homePath;
        configsPath = configBase.resolve(CONFIGS_DIR);
        if (currentWorldPath == null) {
            currentWorldPath = homePath;
        }
        sandboxPath = homePath.resolve(SANDBOX_DIR);

        modPaths = defaultModPaths();

        for (Path path : getAllPaths()) {
            try {
                Files.createDirectories(path);
            } catch (FileAlreadyExistsException e) {
                // It's okay if it exists as a symlink to a directory.
                if (!(Files.isSymbolicLink(path) && Files.isDirectory(path))) {
                    throw e;
                }
            }
        }

        // --------------------------------- Setup native paths ---------------------
        // Two layouts coexist, so they need two paths.
        //
        // LWJGL natives are extracted per architecture - natives/windows-amd64, natives/macos-arm64
        // and so on - because "natives-windows" is a substring of "natives-windows-arm64", so a
        // single directory had one architecture silently overwriting the other. JNBullet and JNLua
        // still extract to the legacy per-OS directory. Pointing every loader at one path leaves
        // whichever library is not in that path unable to load.
        final String legacyDirName;
        final String lwjglOsName;
        switch (OS.get()) {
            case WINDOWS:
                legacyDirName = "windows";
                lwjglOsName = "windows";
                break;
            case MACOSX:
                legacyDirName = "macosx";
                lwjglOsName = "macos";  // deliberately not "macosx" - matches the LWJGL classifier
                break;
            case LINUX:
                legacyDirName = "linux";
                lwjglOsName = "linux";
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operating system: " + System.getProperty("os" +
                        ".name"));
        }
        final String natives = nativesPath.resolve(legacyDirName).toAbsolutePath().toString();
        final String lwjglNatives =
                nativesPath.resolve(lwjglOsName + "-" + nativeArchName()).toAbsolutePath().toString();

        System.setProperty("org.lwjgl.librarypath", lwjglNatives);
        System.setProperty("net.java.games.input.librarypath", natives);  // libjinput
        System.setProperty("org.terasology.librarypath", natives); // JNBullet

    }

    /**
     * "amd64" or "arm64", matching the suffix on the native directories produced by the build.
     *
     * <p>The JVM reports "aarch64" for arm64 on every OS, and "amd64" or "x86_64" depending on the
     * platform for the other - normalize both down to the two buckets LWJGL ships classifiers for.
     * Kept in step with {@code nativeArchName()} in build-logic's {@code exec.kt}, which names the
     * directories this resolves against.
     */
    private static String nativeArchName() {
        String arch = System.getProperty("os.arch");
        switch (arch) {
            case "aarch64":
            case "arm64":
                return "arm64";
            case "amd64":
            case "x86_64":
                return "amd64";
            default:
                throw new UnsupportedOperationException("Unsupported native architecture: " + arch);
        }
    }

    /**
     * The OS-standard location for logs (state, not data). Only Linux/XDG defines one -
     * {@code $XDG_STATE_HOME} (default {@code ~/.local/state}) - dev.dirs has no {@code state_dir}
     * field for it since it predates that part of the spec, so this reads the environment itself
     * instead. macOS and Windows have no equivalent OS-standard state location at all, so those fall
     * back to {@code dataLocalDir}, same as before.
     */
    private static Path resolveStateDir() {
        if (OS.get() != OS.LINUX) {
            return Paths.get(PROJECT_DIRS.dataLocalDir);
        }
        // Spec requires $XDG_STATE_HOME to be absolute. Relative -> invalid -> ignore, use default.
        String xdgStateHome = System.getenv("XDG_STATE_HOME");
        Path stateHome = (xdgStateHome != null && !xdgStateHome.isEmpty() && Paths.get(xdgStateHome).isAbsolute())
                ? Paths.get(xdgStateHome)
                : Paths.get(System.getProperty("user.home"), ".local", "state");
        // Reuse dataDir's project-name segment ("terasology") rather than hardcoding it again.
        return stateHome.resolve(Paths.get(PROJECT_DIRS.dataDir).getFileName());
    }

    protected ImmutableList<Path> defaultModPaths() throws IOException {
        Path homeModPath = homePath.resolve(MODULE_DIR);
        // Same OS-standard-vs-homePath split as logPath in updateDirs(): the module cache is a
        // cache (cacheDir, e.g. ~/.cache on Linux) only while homePath is still the OS default.
        Path modCacheBase = usingOsStandardDirs ? Paths.get(PROJECT_DIRS.cacheDir) : homePath;
        Path modCachePath = modCacheBase.resolve(MODULE_CACHE_DIR);

        if (homePath.equals(installPath)) {
            return ImmutableList.of(modCachePath, homeModPath);
        } else {
            Path installModPath = installPath.resolve(MODULE_DIR);
            return ImmutableList.of(installModPath, modCachePath, homeModPath);
        }
    }

    public Path getHomeModPath() {
        // Not modPaths.get(0) - that's install or cache dir, not homePath's own module dir. Callers
        // (ModuleInstaller, ClientConnectionHandler, Behavior[/Collective]System) want the latter.
        return homePath.resolve(MODULE_DIR);
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

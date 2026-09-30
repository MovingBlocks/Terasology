// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import com.google.common.collect.ImmutableList;
import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.Win32Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.utilities.OS;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
    private static final String PROJECT_NAME = "terasology";
    // dev.dirs's own Windows backend shells out to PowerShell (unreleased fix: dirs-dev/directories-jvm#61,
    // needs Java 22) - resolved here directly instead, so the four OS-standard locations below use JNA's
    // Shell32Util (already a dependency, same call BenjaminAmos's PR review pointed at) on Windows, XDG env
    // vars on Linux, and the fixed ~/Library paths on macOS. See resolveDataDir() etc. below.
    private static final Path PROJECT_DATA_DIR = resolveDataDir();
    private static final Path PROJECT_DATA_LOCAL_DIR = resolveDataLocalDir();
    private static final Path PROJECT_CONFIG_DIR = resolveConfigDir();
    private static final Path PROJECT_CACHE_DIR = resolveCacheDir();
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
        migrateLegacyHomeIfPresent();
        // use datadir, .local/share for linux e.g.
        homePath = PROJECT_DATA_DIR;
        usingOsStandardDirs = true;
        updateDirs();
    }

    /**
     * Moves data from the pre-XDG/native-directories default home into the new one, so upgrading
     * without {@code --homedir} doesn't make existing saves, modules, and settings look like they
     * vanished. Safe to call on every launch: each move only happens once, when the new location
     * doesn't exist yet and the old one does.
     */
    private static void migrateLegacyHomeIfPresent() {
        Path legacyHome = legacyDefaultHomePath();
        if (legacyHome == null) {
            return;
        }
        migrateDirectory(legacyHome, PROJECT_DATA_DIR);
        // The legacy tree kept configs/logs/the module cache nested directly under home; carry
        // those the rest of the way to their own new OS-standard locations too, instead of leaving
        // them stranded inside dataDir where nothing looks for them anymore.
        migrateDirectory(PROJECT_DATA_DIR.resolve(CONFIGS_DIR), PROJECT_CONFIG_DIR.resolve(CONFIGS_DIR));
        migrateDirectory(PROJECT_DATA_DIR.resolve(LOG_DIR), resolveStateDir().resolve(LOG_DIR));
        migrateDirectory(PROJECT_DATA_DIR.resolve(MODULE_CACHE_DIR), PROJECT_CACHE_DIR.resolve(MODULE_CACHE_DIR));
    }

    /**
     * Where the default home used to be, before {@code PROJECT_DATA_DIR} and friends replaced the
     * old per-OS logic. {@code null} if it can't be determined (e.g. neither Windows known-folder
     * lookup resolves) - migration is skipped rather than guessed at.
     */
    static Path legacyDefaultHomePath() {
        switch (OS.get()) {
            case WINDOWS:
                String base = legacyWindowsSavedGamesOrDocuments();
                return base == null ? null : Paths.get(base, "Terasology");
            case MACOSX:
                return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Terasology");
            case LINUX:
            default:
                // Unchanged by this: the Linux default was already $XDG_DATA_HOME/terasology.
                return PROJECT_DATA_DIR;
        }
    }

    private static String legacyWindowsSavedGamesOrDocuments() {
        try {
            return Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_SavedGames);
        } catch (Win32Exception e) {
            try {
                return Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Documents);
            } catch (Win32Exception e2) {
                return null;
            }
        }
    }

    /** Package-private (not private) so PathManagerTest can exercise the move logic directly with @TempDir paths. */
    static void migrateDirectory(Path legacy, Path target) {
        if (legacy.equals(target) || !Files.isDirectory(legacy) || Files.exists(target)) {
            return;
        }
        try {
            Files.createDirectories(target.getParent());
            Files.move(legacy, target);
            LOGGER.info("Migrated {} to {}", legacy, target);
        } catch (IOException e) {
            LOGGER.warn("Failed to migrate {} to {} - a fresh one will be created there instead.", legacy, target, e);
        }
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
        // Logs are state, not data - $XDG_STATE_HOME on Linux, not dataDir/dataLocalDir. macOS/Windows
        // have no OS-standard state location at all, so dataLocalDir stays the fallback there. Only
        // used while homePath itself is still the OS default - once homePath is chosen by something
        // else (--homedir, manual pick), logs move under it too so the whole tree stays self-contained.
        Path logBase = usingOsStandardDirs ? resolveStateDir() : homePath;
        logPath = logBase.resolve(LOG_DIR);
        shaderLogPath = logPath.resolve(SHADER_LOG_DIR);
        screenshotPath = homePath.resolve(SCREENSHOT_DIR);
        nativesPath = installPath.resolve(NATIVES_DIR);
        // configDir is its own OS-standard location for config (XDG_CONFIG_HOME on Linux, \config
        // under RoamingAppData on Windows) - genuinely separate from dataDir there. On macOS there's
        // no such split; configDir just points back at the same Application Support folder as
        // dataDir/homePath, so still appending CONFIGS_DIR keeps config files in their own subfolder
        // there too, instead of dumping them loose at the tree root.
        Path configBase = usingOsStandardDirs ? PROJECT_CONFIG_DIR : homePath;
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
     * {@code $XDG_STATE_HOME} (default {@code ~/.local/state}). macOS and Windows have no equivalent
     * OS-standard state location at all, so those fall back to {@code dataLocalDir}, same as before.
     */
    private static Path resolveStateDir() {
        if (OS.get() != OS.LINUX) {
            return PROJECT_DATA_LOCAL_DIR;
        }
        return resolveXdgDir("XDG_STATE_HOME", ".local", "state").resolve(PROJECT_NAME);
    }

    /** {@code $XDG_DATA_HOME} on Linux, JNA's Win32 call on Windows, {@code ~/Library/...} on macOS. */
    private static Path resolveDataDir() {
        switch (OS.get()) {
            case WINDOWS:
                return windowsKnownFolder(KnownFolders.FOLDERID_RoamingAppData)
                        .resolve(PROJECT_NAME).resolve(PROJECT_NAME).resolve("data");
            case MACOSX:
                return macOsApplicationSupportDir();
            case LINUX:
            default:
                return resolveXdgDir("XDG_DATA_HOME", ".local", "share").resolve(PROJECT_NAME);
        }
    }

    /** Same as {@link #resolveDataDir()} on Linux/macOS - Windows alone splits roaming vs. local data. */
    private static Path resolveDataLocalDir() {
        switch (OS.get()) {
            case WINDOWS:
                return windowsKnownFolder(KnownFolders.FOLDERID_LocalAppData)
                        .resolve(PROJECT_NAME).resolve(PROJECT_NAME).resolve("data");
            case MACOSX:
                return macOsApplicationSupportDir();
            case LINUX:
            default:
                return resolveDataDir();
        }
    }

    /** {@code $XDG_CONFIG_HOME} on Linux, JNA's Win32 call on Windows, {@code ~/Library/...} on macOS. */
    private static Path resolveConfigDir() {
        switch (OS.get()) {
            case WINDOWS:
                return windowsKnownFolder(KnownFolders.FOLDERID_RoamingAppData)
                        .resolve(PROJECT_NAME).resolve(PROJECT_NAME).resolve("config");
            case MACOSX:
                return macOsApplicationSupportDir();
            case LINUX:
            default:
                return resolveXdgDir("XDG_CONFIG_HOME", ".config").resolve(PROJECT_NAME);
        }
    }

    /** {@code $XDG_CACHE_HOME} on Linux, JNA's Win32 call on Windows, {@code ~/Library/Caches} on macOS. */
    private static Path resolveCacheDir() {
        switch (OS.get()) {
            case WINDOWS:
                return windowsKnownFolder(KnownFolders.FOLDERID_LocalAppData)
                        .resolve(PROJECT_NAME).resolve(PROJECT_NAME).resolve("cache");
            case MACOSX:
                return Paths.get(System.getProperty("user.home"), "Library", "Caches", "org." + PROJECT_NAME + "." + PROJECT_NAME);
            case LINUX:
            default:
                return resolveXdgDir("XDG_CACHE_HOME", ".cache").resolve(PROJECT_NAME);
        }
    }

    private static Path macOsApplicationSupportDir() {
        return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "org." + PROJECT_NAME + "." + PROJECT_NAME);
    }

    /**
     * Retrieves a Windows known-folder path directly via the Win32 API through JNA - not by shelling
     * out to PowerShell, unlike the dev.dirs library this replaced (see the review on #5281).
     */
    private static Path windowsKnownFolder(GUID folderId) {
        return Paths.get(Shell32Util.getKnownFolderPath(folderId));
    }

    /**
     * Reads an XDG base-directory environment variable, falling back to the spec's default
     * ({@code $HOME}/{@code fallbackSegments}) when it's unset, empty, or - per spec - not absolute.
     */
    private static Path resolveXdgDir(String envVar, String... fallbackSegments) {
        String value = System.getenv(envVar);
        if (value != null && !value.isEmpty() && Paths.get(value).isAbsolute()) {
            return Paths.get(value);
        }
        return Paths.get(System.getProperty("user.home"), fallbackSegments);
    }

    protected ImmutableList<Path> defaultModPaths() throws IOException {
        Path homeModPath = homePath.resolve(MODULE_DIR);
        // Same OS-standard-vs-homePath split as logPath in updateDirs(): the module cache is a
        // cache (cacheDir, e.g. ~/.cache on Linux) only while homePath is still the OS default.
        Path modCacheBase = usingOsStandardDirs ? PROJECT_CACHE_DIR : homePath;
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
        // Static fields (PROJECT_DATA_DIR and friends) are excluded - they're OS-standard locations
        // computed once at class-load, not per-instance directories PathManager should be creating;
        // sweeping them in here would create them on disk even when --homedir points somewhere else.
        List<Path> allPaths = Arrays.stream(PathManager.class.getDeclaredFields())
                .filter(field -> Path.class.isAssignableFrom(field.getType()) && !Modifier.isStatic(field.getModifiers()))
                .map(this::getField).collect(Collectors.toList());
        allPaths.addAll(modPaths);
        return allPaths;
    }
}

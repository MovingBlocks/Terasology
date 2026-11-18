// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.terasology.engine.utilities.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link PathManager} path resolution and directory creation.
 */
public class PathManagerTest {

    private PathManager pathManager;
    private Path originalHomePath;

    @BeforeEach
    public void setup(@TempDir Path tempHome) throws IOException {
        pathManager = PathManager.getInstance();
        originalHomePath = pathManager.getHomePath();
        pathManager.useOverrideHomePath(tempHome);
    }

    /**
     * Leave the singleton pointing at a directory that actually exists.
     * <p>
     * Restoring the original is only right while it is still there. Simply skipping restoration when
     * it is not - which is what a bare guard does - is the worse option: it leaves the singleton on
     * this test's {@link TempDir}, which JUnit deletes the moment this class finishes, so the next
     * class to read the home path fails and the problem propagates instead of stopping here.
     */
    @AfterEach
    public void tearDown() throws IOException {
        if (originalHomePath != null && Files.isDirectory(originalHomePath)) {
            pathManager.useOverrideHomePath(originalHomePath);
        } else {
            Path fallback = Files.createTempDirectory("terasology-pathmanager");
            fallback.toFile().deleteOnExit();
            pathManager.useOverrideHomePath(fallback);
        }
    }

    @Test
    public void overrideHomePathSetsAllDirectories() {
        assertNotNull(pathManager.getHomePath());
        assertNotNull(pathManager.getSavesPath());
        assertNotNull(pathManager.getLogPath());
        assertNotNull(pathManager.getScreenshotPath());
        assertNotNull(pathManager.getConfigsPath());
        assertNotNull(pathManager.getInstallPath());
    }

    @Test
    public void overrideHomePathCreatesDirectories() {
        assertTrue(Files.isDirectory(pathManager.getSavesPath()));
        assertTrue(Files.isDirectory(pathManager.getLogPath()));
        assertTrue(Files.isDirectory(pathManager.getScreenshotPath()));
        assertTrue(Files.isDirectory(pathManager.getConfigsPath()));
    }

    /**
     * The engine previously computed log/shader-log/module-cache paths from the OS-standard dev.dirs
     * locations unconditionally, ignoring whatever homePath was actually set to - so a caller like
     * TerasologyLauncher's {@code --homedir} override (portable installs, testing multiple clients
     * against separate home directories) would still see its logs and cached modules land outside the
     * directory it asked for. They need to nest under homePath like everything else updateDirs()
     * computes, exactly as they did before dev.dirs was introduced.
     */
    @Test
    public void overrideHomePathIsRespectedByLogAndModuleCachePaths() {
        Path homePath = pathManager.getHomePath();
        assertTrue(pathManager.getLogPath().startsWith(homePath),
                "Expected log path to nest under the overridden home path: " + pathManager.getLogPath());
        assertTrue(pathManager.getShaderLogPath().startsWith(homePath),
                "Expected shader log path to nest under the overridden home path: " + pathManager.getShaderLogPath());
        Path moduleCachePath = homePath.resolve("cachedModules");
        assertTrue(pathManager.getModulePaths().contains(moduleCachePath),
                "Expected the module cache path to nest under the overridden home path, among: " + pathManager.getModulePaths());
    }

    /**
     * getHomeModPath() used to return modPaths.get(0) - the cache or install path, never homePath's
     * own module dir. Callers (install, download, behavior save) want that dir specifically.
     */
    @Test
    public void getHomeModPathReturnsHomeDirectorysOwnModulesFolder() {
        Path expected = pathManager.getHomePath().resolve("modules");
        assertEquals(expected, pathManager.getHomeModPath());
    }

    @Test
    public void getSavePathSanitizesTitle() {
        Path savePath = pathManager.getSavePath("My!World@Test");
        // Only alphanumeric, hyphens, underscores, and spaces are kept
        assertEquals("MyWorldTest", savePath.getFileName().toString());
    }

    @Test
    public void getSavePathPreservesValidTitle() {
        Path savePath = pathManager.getSavePath("Game 1 - Test_World");
        assertEquals("Game 1 - Test_World", savePath.getFileName().toString());
    }

    @Test
    public void getRecordingPathSanitizesTitle() {
        Path recordingPath = pathManager.getRecordingPath("recording<>:test");
        assertEquals("recordingtest", recordingPath.getFileName().toString());
    }

    @Test
    public void savePathIsUnderSavesDirectory() {
        Path savePath = pathManager.getSavePath("MyWorld");
        assertEquals(pathManager.getSavesPath(), savePath.getParent());
    }

    @Test
    public void getSavePathEmptyOrSpecialCharactersReturnsRoot() {
        Path savePath = pathManager.getSavePath("!!!@@@");
        // All invalid characters are removed, leaving an empty string.
        // Resolving an empty string against savesPath returns savesPath itself.
        assertEquals(pathManager.getSavesPath(), savePath);
    }

    @Test
    public void getSavePathIgnoresPathTraversal() {
        Path savePath = pathManager.getSavePath("../../Windows/System32");
        // Dots and slashes are removed, combining the remaining valid characters.
        assertEquals("WindowsSystem32", savePath.getFileName().toString());
        assertEquals(pathManager.getSavesPath(), savePath.getParent());
    }

    @Test
    @Tag("filesystem-side-effects")
    public void useDefaultHomePathResolvesToARealProjectDirectory() throws IOException {
        // PathManager asks the OS directly (JNA's Win32 API on Windows, fixed paths on macOS, XDG env
        // vars/spec on Linux) rather than going through Java's `user.home` system property, so unlike
        // the old hand-rolled per-OS logic this replaced, that property can no longer be redirected to
        // a @TempDir to sandbox this call - every platform now does what only Windows used to: this
        // really creates a directory in the developer's own OS-standard data location. That's why it's
        // tagged and excluded from `test`/`unitTest` - run it deliberately with
        // `gradlew :engine-tests:filesystemSideEffectTest`.
        pathManager.useDefaultHomePath();

        Path homePath = pathManager.getHomePath();
        assertNotNull(homePath);
        assertTrue(homePath.toString().toLowerCase().contains("terasology"),
                "Expected the default home path to be namespaced under \"terasology\": " + homePath);
        assertTrue(Files.isDirectory(homePath));
        assertTrue(Files.isDirectory(pathManager.getSavesPath()));
    }

    /**
     * Without {@code --homedir} (or a manual pick), logs, the module cache, and configs should each
     * keep following their own OS-standard location ({@code $XDG_STATE_HOME}/dataLocalDir, cacheDir,
     * configDir) instead of always nesting under homePath (dataDir) - homePath overriding everything
     * is only supposed to kick in once something actually overrides homePath. Both need to hold: the
     * override still relocates everything when used, and leaving it alone still gets the OS-standard
     * split.
     */
    @Test
    @Tag("filesystem-side-effects")
    public void defaultHomePathKeepsLogCacheAndConfigsOnTheirOwnOsStandardLocations() throws IOException {
        pathManager.useDefaultHomePath();

        // Independently recomputed (not calling into PathManager) so this genuinely checks PathManager's
        // output against the OS-standard locations, not just against itself.
        Path expectedCacheBase;
        Path expectedConfigBase;
        Path expectedLogBase;
        String home = System.getProperty("user.home");
        switch (OS.get()) {
            case WINDOWS:
                String roaming = Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_RoamingAppData);
                String local = Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_LocalAppData);
                expectedConfigBase = Paths.get(roaming, "terasology", "terasology", "config");
                expectedCacheBase = Paths.get(local, "terasology", "terasology", "cache");
                expectedLogBase = Paths.get(local, "terasology", "terasology", "data");
                break;
            case MACOSX:
                expectedConfigBase = Paths.get(home, "Library", "Application Support", "org.terasology.terasology");
                expectedCacheBase = Paths.get(home, "Library", "Caches", "org.terasology.terasology");
                expectedLogBase = expectedConfigBase;
                break;
            case LINUX:
            default:
                expectedConfigBase = xdgOrDefault("XDG_CONFIG_HOME", home, ".config").resolve("terasology");
                expectedCacheBase = xdgOrDefault("XDG_CACHE_HOME", home, ".cache").resolve("terasology");
                // Logs are state, not data - $XDG_STATE_HOME, not $XDG_DATA_HOME/dataLocalDir.
                expectedLogBase = xdgOrDefault("XDG_STATE_HOME", home, ".local", "state").resolve("terasology");
                break;
        }

        assertTrue(pathManager.getLogPath().startsWith(expectedLogBase),
                "Expected log path under the OS-standard state dir " + expectedLogBase
                        + ", got: " + pathManager.getLogPath());
        assertTrue(pathManager.getModulePaths().stream().anyMatch(path -> path.startsWith(expectedCacheBase)),
                "Expected the module cache under the OS-standard cache dir " + expectedCacheBase
                        + ", among: " + pathManager.getModulePaths());
        assertTrue(pathManager.getConfigsPath().startsWith(expectedConfigBase),
                "Expected configs under the OS-standard config dir " + expectedConfigBase
                        + ", got: " + pathManager.getConfigsPath());
    }

    private static Path xdgOrDefault(String envVar, String home, String... fallbackSegments) {
        String value = System.getenv(envVar);
        if (value != null && !value.isEmpty() && Paths.get(value).isAbsolute()) {
            return Paths.get(value);
        }
        return Paths.get(home, fallbackSegments);
    }

    /**
     * Regression coverage for the "existing saves/modules/settings look like they vanished after
     * upgrading" migration gap: exercises PathManager.migrateDirectory() directly against @TempDir
     * paths, so it's fast and doesn't touch any real OS-standard location.
     */
    @Test
    public void migrateDirectoryMovesContentWhenTargetIsAbsent(@TempDir Path tempDir) throws IOException {
        Path legacy = tempDir.resolve("legacy");
        Files.createDirectories(legacy.resolve("saves").resolve("myworld"));
        Files.writeString(legacy.resolve("saves").resolve("myworld").resolve("manifest.json"), "{}");
        Path target = tempDir.resolve("new-home");

        PathManager.migrateDirectory(legacy, target);

        assertTrue(Files.exists(target.resolve("saves").resolve("myworld").resolve("manifest.json")),
                "Expected the legacy tree's content to have moved to the new location");
        assertTrue(Files.notExists(legacy), "Expected the legacy directory to be gone after migrating");
    }

    @Test
    public void migrateDirectoryLeavesLegacyAloneWhenTargetAlreadyExists(@TempDir Path tempDir) throws IOException {
        Path legacy = tempDir.resolve("legacy");
        Files.createDirectories(legacy);
        Files.writeString(legacy.resolve("marker.txt"), "legacy");
        Path target = tempDir.resolve("new-home");
        Files.createDirectories(target);

        PathManager.migrateDirectory(legacy, target);

        assertTrue(Files.exists(legacy.resolve("marker.txt")),
                "Expected an already-populated new location to be left alone, not overwritten");
        assertTrue(Files.notExists(target.resolve("marker.txt")));
    }

    @Test
    public void migrateDirectoryIsANoOpWhenLegacyIsAbsent(@TempDir Path tempDir) {
        Path legacy = tempDir.resolve("never-existed");
        Path target = tempDir.resolve("new-home");

        PathManager.migrateDirectory(legacy, target);

        assertTrue(Files.notExists(target), "Expected nothing to be created when there's no legacy data to migrate");
    }

    @Test
    public void migrateDirectoryIsANoOpWhenLegacyAndTargetAreTheSame(@TempDir Path tempDir) throws IOException {
        Path same = tempDir.resolve("home");
        Files.createDirectories(same);
        Files.writeString(same.resolve("marker.txt"), "unchanged");

        PathManager.migrateDirectory(same, same);

        assertTrue(Files.exists(same.resolve("marker.txt")), "Expected a self-migration to be a no-op");
    }

    @Test
    public void legacyDefaultHomePathMatchesTheOldPerOsDefault() {
        Path legacy = PathManager.legacyDefaultHomePath();
        String home = System.getProperty("user.home");
        switch (OS.get()) {
            case MACOSX:
                assertEquals(Paths.get(home, "Library", "Application Support", "Terasology"), legacy);
                break;
            case LINUX:
                // Unchanged by this PR - the Linux default was already $XDG_DATA_HOME/terasology.
                assertEquals(Paths.get(home, ".local", "share", "terasology"), legacy);
                break;
            case WINDOWS:
                // Either FOLDERID_SavedGames or FOLDERID_Documents, both real Known Folders on any
                // real Windows install - just check it landed under one of them, in a "Terasology" folder.
                assertTrue(legacy == null || legacy.endsWith("Terasology"),
                        "Expected the legacy Windows home to end in \"Terasology\", got: " + legacy);
                break;
            default:
                break;
        }
    }
}

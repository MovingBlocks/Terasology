// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import dev.dirs.ProjectDirectories;
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
        assertTrue(pathManager.getModulePaths().stream().anyMatch(path -> path.startsWith(homePath)),
                "Expected the module cache path to nest under the overridden home path, among: " + pathManager.getModulePaths());
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
        // dev.dirs (ProjectDirectories) asks the OS directly (platform APIs on Windows/macOS, XDG env
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

        ProjectDirectories projectDirs = ProjectDirectories.from("org", "terasology", "terasology");
        Path expectedCacheBase = Paths.get(projectDirs.cacheDir);
        Path expectedConfigBase = Paths.get(projectDirs.configDir);
        // Logs are state, not data: on Linux that's $XDG_STATE_HOME, not dataLocalDir - dev.dirs
        // itself has no state_dir field to compare against, so mirror PathManager's own fallback here.
        Path expectedLogBase;
        if (OS.get() == OS.LINUX) {
            String xdgStateHome = System.getenv("XDG_STATE_HOME");
            Path stateHome = (xdgStateHome != null && !xdgStateHome.isEmpty())
                    ? Paths.get(xdgStateHome)
                    : Paths.get(System.getProperty("user.home"), ".local", "state");
            expectedLogBase = stateHome.resolve(Paths.get(projectDirs.dataDir).getFileName());
        } else {
            expectedLogBase = Paths.get(projectDirs.dataLocalDir);
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
}

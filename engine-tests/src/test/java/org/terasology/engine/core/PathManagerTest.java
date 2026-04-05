// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link PathManager} path resolution and directory creation.
 */
public class PathManagerTest {

    private PathManager pathManager;

    @BeforeEach
    public void setup(@TempDir Path tempHome) throws IOException {
        pathManager = PathManager.getInstance();
        pathManager.useOverrideHomePath(tempHome);
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
}

// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

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
    @EnabledOnOs(OS.MAC)
    public void useDefaultHomePathMac(@TempDir Path tempHome) throws IOException {
        String originalHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", tempHome.toString());
            pathManager.useDefaultHomePath();
            
            Path expectedMacPath = tempHome.resolve("Library/Application Support/Terasology");
            assertEquals(expectedMacPath.toAbsolutePath(), pathManager.getHomePath().toAbsolutePath());
            assertTrue(Files.isDirectory(pathManager.getHomePath()));
            assertTrue(Files.isDirectory(pathManager.getSavesPath()));
        } finally {
            if (originalHome != null) {
                System.setProperty("user.home", originalHome);
            } else {
                System.clearProperty("user.home");
            }
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    public void useDefaultHomePathLinux(@TempDir Path tempHome) throws IOException {
        String originalHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", tempHome.toString());
            pathManager.useDefaultHomePath();
            
            Path expectedLinuxPath = tempHome.resolve(".local/share/terasology");
            assertEquals(expectedLinuxPath.toAbsolutePath(), pathManager.getHomePath().toAbsolutePath());
            assertTrue(Files.isDirectory(pathManager.getHomePath()));
            assertTrue(Files.isDirectory(pathManager.getSavesPath()));
        } finally {
            if (originalHome != null) {
                System.setProperty("user.home", originalHome);
            } else {
                System.clearProperty("user.home");
            }
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void useDefaultHomePathWindows() throws IOException {
        // Windows relies on JNA (Shell32Util) which directly interrogates the Windows Registry/API.
        // We cannot easily redirect this to a @TempDir. We only verify that the path resolves to a valid Windows dir.
        // Note: This will create a 'Terasology' folder in the local user's AppData/Saved Games! 
        pathManager.useDefaultHomePath();
        assertNotNull(pathManager.getHomePath());
        
        // It should end with Terasology
        assertTrue(pathManager.getHomePath().toString().endsWith("Terasology"));
        assertTrue(Files.isDirectory(pathManager.getHomePath()));
        assertTrue(Files.isDirectory(pathManager.getSavesPath()));
    }
}

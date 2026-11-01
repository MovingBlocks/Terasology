// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.network.internal.ServerConnectListManager;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link org.terasology.engine.network.internal.ServerConnectListManager}.
 * Verifies that legacy {@code blacklist.json} and {@code whitelist.json} files are
 * automatically migrated to {@code denylist.json} and {@code allowlist.json} on startup,
 * and that existing new-format files are never overwritten by legacy ones.
 */
public class ServerConnectListManagerTest {

    @TempDir
    private Path tempDir;

    private Context context;
    private Path originalHomePath;

    @BeforeEach
    void setUp() throws IOException {
        originalHomePath = PathManager.getInstance().getHomePath();
        PathManager.getInstance().useOverrideHomePath(tempDir);

        DisplayDevice display = mock(DisplayDevice.class);
        when(display.isHeadless()).thenReturn(true);

        context = mock(Context.class);
        when(context.get(DisplayDevice.class)).thenReturn(display);
    }

    @AfterEach
    void tearDown() throws IOException {
        PathManager.getInstance().useOverrideHomePath(originalHomePath);
    }

    @Test
    void blacklistJsonIsMigratedToDenylistOnStartup() throws IOException {
        writeJson(tempDir.resolve("blacklist.json"), Set.of("banned-client"));

        ServerConnectListManager manager = new ServerConnectListManager(context);

        assertTrue(manager.getDenylist().contains("banned-client"),
                "Entry from blacklist.json should be loaded into the denylist after migration");
        assertFalse(Files.exists(tempDir.resolve("blacklist.json")),
                "blacklist.json should be removed after migration");
        assertTrue(Files.exists(tempDir.resolve("denylist.json")),
                "denylist.json should exist after migration");
    }

    @Test
    void whitelistJsonIsMigratedToAllowlistOnStartup() throws IOException {
        writeJson(tempDir.resolve("whitelist.json"), Set.of("trusted-client"));

        ServerConnectListManager manager = new ServerConnectListManager(context);

        assertTrue(manager.getAllowlist().contains("trusted-client"),
                "Entry from whitelist.json should be loaded into the allowlist after migration");
        assertFalse(Files.exists(tempDir.resolve("whitelist.json")),
                "whitelist.json should be removed after migration");
        assertTrue(Files.exists(tempDir.resolve("allowlist.json")),
                "allowlist.json should exist after migration");
    }

    @Test
    void existingDenylistIsNotOverwrittenByLegacyBlacklist() throws IOException {
        writeJson(tempDir.resolve("blacklist.json"), Set.of("legacy-client"));
        writeJson(tempDir.resolve("denylist.json"), Set.of("current-client"));

        ServerConnectListManager manager = new ServerConnectListManager(context);

        assertTrue(manager.getDenylist().contains("current-client"),
                "Existing denylist.json entries should be preserved");
        assertFalse(manager.getDenylist().contains("legacy-client"),
                "blacklist.json should not overwrite an existing denylist.json");
        assertTrue(Files.exists(tempDir.resolve("blacklist.json")),
                "blacklist.json should be left untouched when denylist.json already exists");
    }

    private void writeJson(Path path, Set<String> entries) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            writer.write(new Gson().toJson(entries));
        }
    }
}

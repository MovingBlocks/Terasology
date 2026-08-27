// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * See https://github.com/MovingBlocks/Terasology/issues/5099. The tool exists so a content author who hits the
 * load-time rejection from {@link BehaviorTreeBuilder} doesn't have to hand-edit the file's JSON to find and
 * remove the offending comma - it does the same fix mechanically.
 */
public class BehaviorTreeRepairToolTest {

    @Test
    public void trailingCommaInCompositeArrayIsRemoved() {
        BehaviorTreeRepairTool.Result result = BehaviorTreeRepairTool.clean(
                "{ \"selector\": [\"success\", \"success\",] }");

        assertTrue(result.changed);
        assertEquals(1, result.nullArrayEntriesRemoved);
        assertTrue(result.unfixableIssues.isEmpty());
        // loads with no custom actions/decorators needed
        BehaviorNode node = new BehaviorTreeBuilder().fromJson(result.cleanedJson);
        assertEquals(2, node.getChildrenCount());
    }

    @Test
    public void explicitNullInCompositeArrayIsRemoved() {
        BehaviorTreeRepairTool.Result result = BehaviorTreeRepairTool.clean(
                "{ \"selector\": [\"success\", null, \"success\"] }");

        assertTrue(result.changed);
        assertEquals(1, result.nullArrayEntriesRemoved);
        assertTrue(result.unfixableIssues.isEmpty());
        BehaviorNode node = new BehaviorTreeBuilder().fromJson(result.cleanedJson);
        assertEquals(2, node.getChildrenCount());
    }

    @Test
    public void nothingToFixIsReportedAsUnchanged() {
        BehaviorTreeRepairTool.Result result = BehaviorTreeRepairTool.clean(
                "{ \"selector\": [\"success\", \"success\"] }");

        assertFalse(result.changed);
        assertEquals(0, result.nullArrayEntriesRemoved);
        assertTrue(result.unfixableIssues.isEmpty());
    }

    @Test
    public void explicitNullDecoratorChildIsReportedNotSilentlyDropped() {
        BehaviorTreeRepairTool.Result result = BehaviorTreeRepairTool.clean(
                "{ \"invert\": { \"child\": null } }");

        assertFalse(result.unfixableIssues.isEmpty());
        String issue = result.unfixableIssues.get(0);
        assertTrue(issue.contains("invert"));
        assertTrue(issue.contains("child"));
    }

    @Test
    public void repairWritesTheFileAndKeepsABackup(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("broken.behavior");
        String original = "{ \"selector\": [\"success\", \"success\",] }";
        Files.write(file, original.getBytes(StandardCharsets.UTF_8));

        BehaviorTreeRepairTool.Result result = BehaviorTreeRepairTool.repair(file);

        assertTrue(result.changed);
        Path backup = dir.resolve("broken.behavior.bak");
        assertTrue(Files.exists(backup));
        assertEquals(original, new String(Files.readAllBytes(backup), StandardCharsets.UTF_8));
        BehaviorNode node = new BehaviorTreeBuilder().fromJson(
                new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
        assertEquals(2, node.getChildrenCount());
    }

    @Test
    public void repairLeavesUnfixableFileUntouched(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("broken.behavior");
        String original = "{ \"invert\": { \"child\": null } }";
        Files.write(file, original.getBytes(StandardCharsets.UTF_8));

        BehaviorTreeRepairTool.Result result = BehaviorTreeRepairTool.repair(file);

        assertFalse(result.changed);
        assertFalse(Files.exists(dir.resolve("broken.behavior.bak")));
        assertEquals(original, new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
    }

    @Test
    public void repairNeverClobbersAnExistingBackup(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("broken.behavior");
        String original = "{ \"selector\": [\"success\", \"success\",] }";
        Files.write(file, original.getBytes(StandardCharsets.UTF_8));
        Path existingBackup = dir.resolve("broken.behavior.bak");
        String someoneElsesBackup = "not the original - already there before repair() ran";
        Files.write(existingBackup, someoneElsesBackup.getBytes(StandardCharsets.UTF_8));

        BehaviorTreeRepairTool.repair(file);

        assertEquals(someoneElsesBackup, new String(Files.readAllBytes(existingBackup), StandardCharsets.UTF_8));
        Path versionedBackup = dir.resolve("broken.behavior.bak.1");
        assertTrue(Files.exists(versionedBackup));
        assertEquals(original, new String(Files.readAllBytes(versionedBackup), StandardCharsets.UTF_8));
    }

    @Test
    public void repairSkipsPastADanglingBackupSymlink(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("broken.behavior");
        String original = "{ \"selector\": [\"success\", \"success\",] }";
        Files.write(file, original.getBytes(StandardCharsets.UTF_8));
        Path danglingBackup = dir.resolve("broken.behavior.bak");
        try {
            Files.createSymbolicLink(danglingBackup, dir.resolve("does-not-exist"));
        } catch (UnsupportedOperationException | IOException e) {
            assumeTrue(false, "symlinks not supported here: " + e);
            return;
        }

        BehaviorTreeRepairTool.repair(file);

        Path versionedBackup = dir.resolve("broken.behavior.bak.1");
        assertTrue(Files.exists(versionedBackup));
        assertEquals(original, new String(Files.readAllBytes(versionedBackup), StandardCharsets.UTF_8));
    }

    @Test
    public void repairLeavesOnlyTheFinalFileBehindNoStrayTempFiles(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("broken.behavior");
        Files.write(file, "{ \"selector\": [\"success\", \"success\",] }".getBytes(StandardCharsets.UTF_8));

        BehaviorTreeRepairTool.repair(file);

        try (Stream<Path> entries = Files.list(dir)) {
            List<String> names = entries.map(p -> p.getFileName().toString())
                    .sorted().collect(Collectors.toList());
            assertEquals(Arrays.asList("broken.behavior", "broken.behavior.bak"), names);
        }
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repairs malformed {@code .behavior} JSON: a stray/trailing comma in a composite's child array parses as a
 * phantom {@code null} entry (see {@link BehaviorTreeBuilder#getCompositeNode},
 * https://github.com/MovingBlocks/Terasology/issues/5099) - safe to strip automatically.
 * <p>
 * A decorator with a null/missing {@code child} is different: a real child is missing and can't be guessed, so
 * it's reported, not touched. A missing key (vs explicit null) can't be told apart from a legit childless action
 * without a live module environment, so it slips through here - {@link BehaviorTreeBuilder} still catches it at
 * load time.
 */
public final class BehaviorTreeRepairTool {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorTreeRepairTool.class);

    private BehaviorTreeRepairTool() {
    }

    /**
     * Strips phantom null entries from composite child arrays; reports (doesn't touch) null/missing decorator
     * children. Doesn't touch any file.
     *
     * @param json raw file contents
     * @return scan result - {@link Result#changed} false if nothing to remove
     */
    public static Result clean(String json) {
        JsonElement root = JsonParser.parseString(json);
        List<String> unfixable = new ArrayList<>();
        int[] removed = {0};
        JsonElement cleaned = stripPhantomNulls(root, unfixable, removed);
        if (removed[0] == 0) {
            return new Result(false, 0, unfixable, null);
        }
        String cleanedJson = new GsonBuilder().setPrettyPrinting().create().toJson(cleaned);
        return new Result(true, removed[0], unfixable, cleanedJson);
    }

    /**
     * Repairs a {@code .behavior} file in place if it has fixable phantom nulls. Backs up the original as
     * {@code <name>.bak} (or {@code .bak.1}, etc. if one's already there), writes via a temp file + atomic move
     * so a failure can't corrupt the source.
     * <p>
     * Skips writing if an unfixable issue exists too (see class doc) - fixing just the arrays would still leave
     * a broken file. Doesn't validate via {@link BehaviorTreeBuilder}: that needs a live module environment this
     * offline tool doesn't have.
     *
     * @param file the {@code .behavior} file to repair
     * @return the repair result
     * @throws IOException if the file can't be read, backed up or written
     */
    public static Result repair(Path file) throws IOException {
        String original = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        Result result = clean(original);
        if (!result.changed) {
            return result;
        }
        if (!result.unfixableIssues.isEmpty()) {
            logger.warn("Not repairing '{}': found unfixable issues too - {}", file, result.unfixableIssues);
            return new Result(false, result.nullArrayEntriesRemoved, result.unfixableIssues, null);
        }
        Path backup = nextFreeBackupPath(file);
        Files.write(backup, original.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

        Path temporary = Files.createTempFile(file.toAbsolutePath().getParent(), file.getFileName().toString(), ".tmp");
        try {
            Files.write(temporary, result.cleanedJson.getBytes(StandardCharsets.UTF_8));
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }

        logger.info("Repaired '{}': removed {} phantom null entr{} (backup at '{}')",
                file, result.nullArrayEntriesRemoved, result.nullArrayEntriesRemoved == 1 ? "y" : "ies", backup);
        return result;
    }

    private static Path nextFreeBackupPath(Path file) {
        Path candidate = file.resolveSibling(file.getFileName() + ".bak");
        int suffix = 1;
        // NOFOLLOW_LINKS: a dangling .bak symlink still occupies the name (CREATE_NEW would fail on it),
        // even though a link-following exists() check would call it absent.
        while (Files.exists(candidate, LinkOption.NOFOLLOW_LINKS)) {
            candidate = file.resolveSibling(file.getFileName() + ".bak." + suffix);
            suffix++;
        }
        return candidate;
    }

    /**
     * CLI entry point - a broken file stops the game from starting, so this runs standalone against the raw file(s).
     *
     * @param args one or more {@code .behavior} file paths
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: BehaviorTreeRepairTool <file.behavior> [more files...]");
            System.exit(1);
        }
        for (String arg : args) {
            Path file = Path.of(arg);
            Result result = repair(file);
            if (result.changed) {
                System.out.println(file + ": removed " + result.nullArrayEntriesRemoved + " phantom null entr"
                        + (result.nullArrayEntriesRemoved == 1 ? "y" : "ies") + ", backup saved alongside it.");
            } else if (!result.unfixableIssues.isEmpty()) {
                System.out.println(file + ": NOT repaired, needs manual fixing - " + result.unfixableIssues);
            } else {
                System.out.println(file + ": nothing to repair.");
            }
        }
    }

    private static JsonElement stripPhantomNulls(JsonElement element, List<String> unfixable, int[] removedCount) {
        return stripPhantomNulls(element, unfixable, removedCount, null);
    }

    // parentKey: the enclosing object's key for this element (e.g. "invert"), so an unfixable null
    // child can be reported against the decorator that owns it, not just "a decorator".
    private static JsonElement stripPhantomNulls(JsonElement element, List<String> unfixable, int[] removedCount,
                                                  String parentKey) {
        if (element.isJsonArray()) {
            JsonArray source = element.getAsJsonArray();
            JsonArray cleaned = new JsonArray();
            for (JsonElement child : source) {
                if (child.isJsonNull()) {
                    removedCount[0]++;
                } else {
                    cleaned.add(stripPhantomNulls(child, unfixable, removedCount, parentKey));
                }
            }
            return cleaned;
        } else if (element.isJsonObject()) {
            JsonObject source = element.getAsJsonObject();
            JsonObject cleaned = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
                JsonElement value = entry.getValue();
                if ("child".equals(entry.getKey()) && value.isJsonNull()) {
                    String owner = parentKey != null ? "'" + parentKey + "'" : "a decorator";
                    unfixable.add(owner + " has a null/missing 'child' - needs a real child added by hand");
                    cleaned.add(entry.getKey(), value);
                } else {
                    cleaned.add(entry.getKey(), stripPhantomNulls(value, unfixable, removedCount, entry.getKey()));
                }
            }
            return cleaned;
        }
        return element;
    }

    public static final class Result {
        public final boolean changed;
        public final int nullArrayEntriesRemoved;
        public final List<String> unfixableIssues;
        /** The cleaned JSON, or {@code null} if {@link #changed} is false. */
        public final String cleanedJson;

        private Result(boolean changed, int nullArrayEntriesRemoved, List<String> unfixableIssues, String cleanedJson) {
            this.changed = changed;
            this.nullArrayEntriesRemoved = nullArrayEntriesRemoved;
            this.unfixableIssues = unfixableIssues;
            this.cleanedJson = cleanedJson;
        }
    }
}

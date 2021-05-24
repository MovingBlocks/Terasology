// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.utilities;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.terasology.engine.utilities.StringUtility;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link StringUtility}
 */
public class CamelCaseMatcherTest {

    @Test
    public void testDefault() {
        List<String> commands = ImmutableList.of("MyPossibleResultString");
        List<String> queries = ImmutableList.of("MyPossibleResultString", "MPRS",
                "MPRString", "MyPosResStr", "M", "MyP*RString", "*PosResString", "My*String");

        for (String query : queries) {
            Collection<String> matches = StringUtility.wildcardMatch(query, commands, true);
            assertEquals(1, matches.size(), "The query did not match the command");
        }
    }

    @Test
    public void testTeraCommands() {
        List<String> commands = ImmutableList.of("azerty", "bindKey", "clearChunkCache", "countAI",
                "damage", "debugTarget", "destroyAI", "destroyEntitiesUsingPrefab", "dumpEntities",
                "exit", "fullscreen", "generateNameList", "generateNameList", "ghost", "giveBlock",
                "giveBlock", "giveItem", "health", "help", "hjump", "hspeed", "initNameGenerator",
                "kill", "listBlocks", "listBlocksByCategory", "listFreeShapeBlocks", "listItems",
                "listShapes", "mute", "neo", "nextName", "placeBlock", "playTestSound", "purgeWorld",
                "restoreSpeed", "say", "setGroundFriction", "setJumpSpeed", "setMaxGhostSpeed",
                "setMaxGroundSpeed", "setMaxHealth", "setRegenRaterate", "showHealth", "showMovement",
                "sleigh", "spawnBlock", "spawnPrefab", "stepHeight", "teleport");

        List<String> noHitQueries = ImmutableList.of("asdfd", "AvDS", "MPRString");

        for (String query : noHitQueries) {
            Collection<String> matches = StringUtility.wildcardMatch(query, commands, true);
            assertEquals(0, matches.size(), () -> "The query '" + query + "' should not match any command");
        }

        List<String> oneHitQueries = ImmutableList.of("liFSB", "puW", "liI");

        for (String query : oneHitQueries) {
            Collection<String> matches = StringUtility.wildcardMatch(query, commands, true);
            assertEquals(1, matches.size(), () -> "The query '" + query + "' should match exactly 1 command, not " + matches.size());
        }

        List<String> multiHitQueries = ImmutableList.of("liB", "spa", "seMaGSpe");

        for (String query : multiHitQueries) {
            Collection<String> matches = StringUtility.wildcardMatch(query, commands, true);
            assertTrue(matches.size() > 1, "The query '" + query + "' should match multiple commands, not " + matches.size());
        }
    }

}

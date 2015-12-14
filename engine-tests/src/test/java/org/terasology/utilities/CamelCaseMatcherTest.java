/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.utilities;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Tests {@link CamelCaseMatcher}
 *
 */
public class CamelCaseMatcherTest {

    @Test
    public void testDefault() {
        List<String> commands = ImmutableList.of("MyPossibleResultString");
        List<String> queries = ImmutableList.of("MyPossibleResultString", "MPRS",
                "MPRString", "MyPosResStr", "M", "MyP*RString", "*PosResString", "My*String");

        for (String query : queries) {
            Collection<String> matches = CamelCaseMatcher.getMatches(query, commands, true);
            assertTrue("The query did not match the command", matches.size() == 1);
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
            Collection<String> matches = CamelCaseMatcher.getMatches(query, commands, true);
            assertTrue("The query '" + query + "' should not match any command", matches.size() == 0);
        }

        List<String> oneHitQueries = ImmutableList.of("liFSB", "puW", "liI");

        for (String query : oneHitQueries) {
            Collection<String> matches = CamelCaseMatcher.getMatches(query, commands, true);
            assertTrue("The query '" + query + "' should match exactly 1 command, not " + matches.size(), matches.size() == 1);
        }

        List<String> multiHitQueries = ImmutableList.of("liB", "spa", "seMaGSpe");

        for (String query : multiHitQueries) {
            Collection<String> matches = CamelCaseMatcher.getMatches(query, commands, true);
            assertTrue("The query '" + query + "' should match multiple commands, not " + matches.size(), matches.size() > 1);
        }
    }

}

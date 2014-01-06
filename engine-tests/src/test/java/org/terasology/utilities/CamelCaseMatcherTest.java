/*
 * Copyright (C) 2012-2014 Martin Steiger
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terasology.utilities;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Tests {@link CamelCaseMatcher}
 * @author Martin Steiger
 */
public class CamelCaseMatcherTest
{
	@Test
	public void testDefault() {
		List<String> commands = ImmutableList.of("MyPossibleResultString");
		List<String> queries = ImmutableList.of("MyPossibleResultString", "MPRS", 
				"MPRString", "MyPosResStr" , "M", "MyP*RString", "*PosResString", "My*String");
		
		for (String query : queries) {
			Collection<String> matches = CamelCaseMatcher.getMatches(query, commands);
			assertTrue("The query did not match the command",  matches.size() == 1);
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
			Collection<String> matches = CamelCaseMatcher.getMatches(query, commands);
			assertTrue("The query '" + query + "' should not match any command",  matches.size() == 0);
		}

		List<String> oneHitQueries = ImmutableList.of("liFSB", "puW", "liI");
		
		for (String query : oneHitQueries) {
			Collection<String> matches = CamelCaseMatcher.getMatches(query, commands);
			assertTrue("The query '" + query + "' should match exactly 1 command, not " + matches.size(), matches.size() == 1);
		}

		List<String> multiHitQueries = ImmutableList.of("liB", "spa", "seMaGSpe");
		
		for (String query : multiHitQueries) {
			Collection<String> matches = CamelCaseMatcher.getMatches(query, commands);
			assertTrue("The query '" + query + "' should match multiple commands, not " + matches.size(),  matches.size() > 1);
		}
	}
	
}

/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.rendering.md5;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class MD5PatternsTests {

	public static class IntegerPatternTest {
		@Test
		public void testIntegerPattern() {
			String[] testNumbers = {"10", "6", "9E1", "76E1", "5E15", "3e4"};
			for (String number : testNumbers) {
				Matcher testMatcher = Pattern.compile(MD5Patterns.INTEGER_PATTERN).matcher(number);
				if (testMatcher.find()) {
					assertEquals(number, testMatcher.group());
				}
			}
		}
	}
	
	public static class FloatPatternTest {
		@Test
		public void testFloatPattern() {
			String[] testNumbers = {"10.1", "10.0", "99E1", "99.0E1.0", "99.0011E1.22", "78e1.0"};
			for (String number : testNumbers) {
				Matcher testMatcher = Pattern.compile(MD5Patterns.FLOAT_PATTERN).matcher(number);
				if (testMatcher.find()) {
					assertEquals(number, testMatcher.group());
				}
			}
		}
	}
	
	public static class Vector3PatternTest {
		@Test
		public void testVector3Pattern() {
			String[] testVectors = {"(10.5 25.0 99E1 )", "(9.5 54.0e9.3 99E1 )", "(4 6.2 11E6.6 )"};
			for (String vector : testVectors) {
				Matcher testMatcher = Pattern.compile(MD5Patterns.VECTOR3_PATTERN).matcher(vector);
				if (testMatcher.find()) {
					assertEquals(vector, testMatcher.group());
				}
			}
		}
	}
	
	public static class Vector2PatternTest {
		@Test
		public void testVector2Pattern() {
			String[] testVectors = {"(10.1 25.0 )", "(9.5 9E1 )", "(1 4e9.0 )"};
			for (String vector : testVectors) {
				Matcher testMatcher = Pattern.compile(MD5Patterns.VECTOR3_PATTERN).matcher(vector);
				if (testMatcher.find()) {
					assertEquals(vector, testMatcher.group());
				}
			}
		}
	}
	
	public static class CommandLinePatternTest {
		@Test
		public void testCommandLinePattern() {
			String[] testCommands = {"\"help\" ", "\"this\"that", "\"faster\""};
			for (String command : testCommands) {
				Matcher testMatcher = Pattern.compile(MD5Patterns.VECTOR3_PATTERN).matcher("commandLine " + command);
				if (testMatcher.find()) {
					assertEquals(command, testMatcher.group());
				}
			}
		}
	}
}

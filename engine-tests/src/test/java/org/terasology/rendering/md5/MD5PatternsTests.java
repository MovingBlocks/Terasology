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
	// https://stackoverflow.com/questions/34571/how-do-i-test-a-private-function-or-a-class-that-has-private-methods-fields-or
	public static class IntegerPatternTest {
		@Test
		public void testIntegerPattern() {
			String[] testNumbers = {"10", "6", "9E1", "76E1", "5E15", "3e4"};
			for (String number : testNumbers) {
				Matcher testMatcher = Pattern.compile(MD5Patterns.INTEGER_PATTERN).matcher(number);
				if (testMatcher.find())
					assertEquals(number, testMatcher.group());
			}
		}
	}
	
	public static class FloatPatternTest {
		@Test
		public void testFloatPattern() {
			String[] testNumbers = {"10.1", "10.0", "99E1", "99.0E1.0", "99.0011E1.22", "78e1.0"};
			for (String number : testNumbers) {
				Matcher testMatcher = Pattern.compile(MD5Patterns.FLOAT_PATTERN).matcher(number);
				if (testMatcher.find())
					assertEquals(number, testMatcher.group());
			}
		}
	}
	
	public static class Vector3PatternTest {
		
	}
	
	public static class Vector2PattersTest {
		
	}
	
	public static class CommandLinePatternTest {
		
	}
}

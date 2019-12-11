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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MD5PatternsTests {

    @Nested
    public class IntegerPatternTest {
        @Test
        public void testIntegerPattern() {
            String[] testNumbers = {"10", "6", "9E1", "76E1", "5E15", "3e4"};
            compareStringsToPattern(testNumbers, MD5Patterns.INTEGER_PATTERN);
        }
    }

    @Nested
    public class FloatPatternTest {
        @Test
        public void testFloatPattern() {
            String[] testNumbers = {"6", "10.0", "-11.9", "+759.76"};
            compareStringsToPattern(testNumbers, MD5Patterns.FLOAT_PATTERN);
        }
    }

    @Nested
    public class Vector3PatternTest {
        @Test
        public void testVector3Pattern() {
            String[] testVectors = {"(10.5 25.0 -1 )", "(9.5 -75.98 97 )", "(4 6.2 +5 )"};
            compareStringsToPattern(testVectors, MD5Patterns.VECTOR3_PATTERN);
        }
    }

    @Nested
    public class Vector2PatternTest {
        @Test
        public void testVector2Pattern() {
            String[] testVectors = {"(10.1 25.0 )", "(9.5 -30 )", "(1 76.075 )"};
            compareStringsToPattern(testVectors, MD5Patterns.VECTOR2_PATTERN);
        }
    }

    @Nested
    public class CommandLinePatternTest {
        @Test
        public void testCommandLinePattern() {
            String[] testCommands = {"\"help\" ", "\"this\" -w", "\"faster\""};
            for (int i = 0; i < testCommands.length; i++) {
                testCommands[i] = "commandline " + testCommands[i];
            }
            compareStringsToPattern(testCommands, MD5Patterns.COMMAND_LINE_PATTERN);
        }
    }
    
    /**
     * Method to test a given regex pattern against each string from the list.
     * @param testStrings List of strings to match against the pattern
     * @param pattern Regex pattern to test
     */
    private static void compareStringsToPattern(String[] testStrings, String pattern) {
        for (String number : testStrings) {
            Matcher matcher = Pattern.compile(pattern).matcher(number);
            if (matcher.find()) {
                assertEquals(number, matcher.group());
            } else {
                fail("The pattern did not match " + number);
            }
        }
    }
}



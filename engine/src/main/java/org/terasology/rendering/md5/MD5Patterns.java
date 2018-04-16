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

/**
 * Class to hold the REGEX patterns for 
 * MD5SkeletonLoader and MD5AnimationLoader.
 *
 */
public class MD5Patterns {
    public static final String INTEGER_PATTERN = "((?:[\\+-]?\\d+)(?:[eE][\\+-]?\\d+)?)";
    public static final String FLOAT_PATTERN = "([-+]?[0-9]*\\.?[0-9]+)";
    public static final String VECTOR3_PATTERN = "\\(\\s*" + FLOAT_PATTERN + "\\s+" + FLOAT_PATTERN + "\\s+" + FLOAT_PATTERN + "\\s+\\)";
    public static final String VECTOR2_PATTERN = "\\(\\s*" + FLOAT_PATTERN + "\\s+" + FLOAT_PATTERN + "\\s+\\)";
    public static final String COMMAND_LINE_PATTERN = "commandline \"(.*)\".*";
}

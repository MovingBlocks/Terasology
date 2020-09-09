// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.md5;

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

/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.engine.module;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for a semantic version string - for version numbers of the form MAJOR.minor.patch.
 *
 * @author Immortius
 */
public class Version implements Comparable<Version> {
    private static final Pattern VERSION_PATTERN = Pattern.compile("(0|[1-9][0-9]*).(0|[1-9][0-9]*).(0|[1-9][0-9]*)");

    private int major;
    private int minor;
    private int patch;

    public Version(int major, int minor, int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Illegal version " + major + "." + minor + "." + patch);
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * @param version The string of the version
     * @return The version, or null if the version string does not represent a valid version.
     */
    public static Version create(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (matcher.matches()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = Integer.parseInt(matcher.group(3));
            return new Version(major, minor, patch);
        }
        return null;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public Version getNextMajorVersion() {
        return new Version(major + 1, 0, 0);
    }

    public Version getNextMinorVersion() {
        return new Version(major, minor + 1, 0);
    }

    public Version getNextPatchVersion() {
        return new Version(major, minor, patch + 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Version) {
            Version other = (Version) obj;
            return other.major == major && other.minor == minor && other.patch == patch;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public int compareTo(Version other) {
        if (other.major != major) {
            return major - other.major;
        }
        if (other.minor != minor) {
            return minor - other.minor;
        }
        return patch - other.patch;
    }
}

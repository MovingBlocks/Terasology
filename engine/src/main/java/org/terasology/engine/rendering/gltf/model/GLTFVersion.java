// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

import java.util.Objects;

/**
 * Represents a two-part version of the form 'major.minor'.
 */
public class GLTFVersion {
    private int major;
    private int minor;

    /**
     * @param major The major version part
     * @param minor The minor version part
     */
    public GLTFVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    /**
     * @return The major version part
     */
    public int getMajor() {
        return major;
    }

    /**
     * @return The minor version part
     */
    public int getMinor() {
        return minor;
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof GLTFVersion) {
            GLTFVersion other = (GLTFVersion) obj;
            return major == other.major && minor == other.minor;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor);
    }
}

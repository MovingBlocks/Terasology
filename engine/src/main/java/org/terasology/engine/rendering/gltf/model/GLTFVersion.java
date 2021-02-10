/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.gltf.model;

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

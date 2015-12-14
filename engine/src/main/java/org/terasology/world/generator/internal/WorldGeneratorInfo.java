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
package org.terasology.world.generator.internal;

import org.terasology.engine.SimpleUri;

import java.util.Objects;

/**
 */
public class WorldGeneratorInfo implements Comparable<WorldGeneratorInfo> {

    private SimpleUri uri;
    private String displayName;
    private String description;

    public WorldGeneratorInfo(SimpleUri uri, String displayName, String description) {
        this.uri = uri;
        this.displayName = displayName;
        this.description = description;
    }

    public SimpleUri getUri() {
        return uri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(WorldGeneratorInfo o) {
        return displayName.compareTo(o.displayName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof WorldGeneratorInfo) {
            WorldGeneratorInfo other = (WorldGeneratorInfo) obj;
            return Objects.equals(uri, other.getUri()) && Objects.equals(other.displayName, displayName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, displayName);
    }

    @Override
    public String toString() {
        return displayName + " (" + uri + ")";
    }

}

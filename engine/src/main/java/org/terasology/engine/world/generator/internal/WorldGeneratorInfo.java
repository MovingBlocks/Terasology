// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generator.internal;

import org.terasology.engine.core.SimpleUri;

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

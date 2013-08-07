/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.world.generator;

import java.util.Locale;

/**
 * @author Immortius
 */
public class WorldGeneratorInfo implements Comparable<WorldGeneratorInfo> {

    private WorldGeneratorUri uri;
    private String displayName;
    private String description;

    public WorldGeneratorInfo(WorldGeneratorUri uri, String displayName, String description) {
        this.uri = uri;
        this.displayName = displayName;
        this.description = description;
    }

    public WorldGeneratorUri getUri() {
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
        return displayName.toLowerCase(Locale.ENGLISH).compareTo(o.displayName.toLowerCase(Locale.ENGLISH));
    }
}

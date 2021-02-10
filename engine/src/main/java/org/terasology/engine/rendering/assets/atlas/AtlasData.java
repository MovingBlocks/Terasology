/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.assets.atlas;

import org.terasology.assets.AssetData;
import org.terasology.naming.Name;
import org.terasology.rendering.assets.texture.subtexture.SubtextureData;

import java.util.Map;

/**
 */
public class AtlasData implements AssetData {

    private Map<Name, SubtextureData> subtextures;

    public AtlasData(Map<Name, SubtextureData> subtextureMap) {
        this.subtextures = subtextureMap;
    }

    public Map<Name, SubtextureData> getSubtextures() {
        return subtextures;
    }
}

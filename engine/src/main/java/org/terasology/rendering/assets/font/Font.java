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

package org.terasology.rendering.assets.font;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Vector2i;

import java.util.List;

/**
 */
public abstract class Font extends Asset<FontData> {

    protected Font(ResourceUrn urn, AssetType<?, FontData> assetType) {
        super(urn, assetType);
    }

    public abstract int getWidth(String text);

    public abstract int getWidth(Character c);

    public abstract int getHeight(String text);

    public abstract int getLineHeight();

    public abstract int getBaseHeight();

    public abstract Vector2i getSize(List<String> lines);

    public abstract boolean hasCharacter(Character c);

    public abstract FontCharacter getCharacterData(Character c);

    public abstract int getUnderlineOffset();

    public abstract int getUnderlineThickness();
}

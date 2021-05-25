// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.font;

import org.joml.Vector2i;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;

import java.util.List;

public abstract class Font extends Asset<FontData> implements org.terasology.nui.asset.font.Font {

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

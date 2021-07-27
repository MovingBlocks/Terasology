// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.font;

import org.joml.Vector2i;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;

import java.util.List;

public final class FontImpl extends Font {

    protected FontData data;

    public FontImpl(ResourceUrn urn, AssetType<?, FontData> assetType, FontData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    protected void doReload(FontData fontData) {
        this.data = fontData;
    }

    @Override
    public int getWidth(String text) {
        int largestWidth = 0;
        int currentWidth = 0;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                largestWidth = Math.max(largestWidth, currentWidth);
                currentWidth = 0;
            } else {
                FontCharacter character = data.getCharacter(c);
                if (character != null) {
                    currentWidth += character.getxAdvance();
                }
            }
        }
        return Math.max(largestWidth, currentWidth);
    }

    @Override
    public int getWidth(Character c) {
        FontCharacter character = data.getCharacter(c);
        if (character != null) {
            return character.getxAdvance();
        }
        return 0;
    }

    @Override
    public int getHeight(String text) {
        int height = data.getLineHeight();
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                height += data.getLineHeight();
            }
        }
        return height;
    }

    @Override
    public int getLineHeight() {
        return data.getLineHeight();
    }

    @Override
    public int getBaseHeight() {
        return data.getBaseHeight();
    }

    @Override
    public Vector2i getSize(List<String> lines) {
        int height = getLineHeight() * lines.size();
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, getWidth(line));
        }
        return new Vector2i(width, height);
    }

    @Override
    public boolean hasCharacter(Character c) {
        return c == '\n' || data.getCharacter(c) != null;
    }

    @Override
    public FontCharacter getCharacterData(Character c) {
        return data.getCharacter(c);
    }

    @Override
    public int getUnderlineOffset() {
        return data.getUnderlineOffset();
    }

    @Override
    public int getUnderlineThickness() {
        return data.getUnderlineThickness();
    }

    @Override
    public String toString() {
        return getUrn().toString();
    }
}

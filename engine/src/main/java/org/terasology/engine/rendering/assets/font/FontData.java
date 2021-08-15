// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.font;

import com.google.common.collect.ImmutableMap;
import org.terasology.gestalt.assets.AssetData;

import java.util.Map;

public class FontData implements AssetData {
    private int lineHeight;
    private int baseHeight;
    private int underlineOffset = 2;
    private int underlineThickness = 1;
    private Map<Integer, FontCharacter> characters;

    public FontData(int lineHeight, int baseHeight, Map<Integer, FontCharacter> characters) {
        this.lineHeight = lineHeight;
        this.baseHeight = baseHeight;
        this.characters = ImmutableMap.copyOf(characters);
    }

    public FontData(FontData other) {
        this.lineHeight = other.lineHeight;
        this.baseHeight = other.baseHeight;
        this.underlineOffset = other.underlineOffset;
        this.underlineThickness = other.underlineThickness;
        this.characters = other.characters;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public int getBaseHeight() {
        return baseHeight;
    }

    public Iterable<Map.Entry<Integer, FontCharacter>> getCharacters() {
        return characters.entrySet();
    }

    public FontCharacter getCharacter(int index) {
        return characters.get(index);
    }

    public int getUnderlineOffset() {
        return underlineOffset;
    }

    public int getUnderlineThickness() {
        return underlineThickness;
    }

    public void setUnderlineOffset(int underlineOffset) {
        this.underlineOffset = underlineOffset;
    }

    public void setUnderlineThickness(int underlineThickness) {
        this.underlineThickness = underlineThickness;
    }
}

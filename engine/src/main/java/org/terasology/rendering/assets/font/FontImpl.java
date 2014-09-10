/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.math.Vector2i;

import java.util.List;

public final class FontImpl extends AbstractAsset<FontData> implements Font {

    protected FontData data;

    public FontImpl(AssetUri uri, FontData data) {
        super(uri);
        reload(data);
    }

    @Override
    public void reload(FontData fontData) {
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
    public void dispose() {
        this.data = null;
    }

    @Override
    public boolean isDisposed() {
        return data == null;
    }

    @Override
    public String toString() {
        return getURI().toString();
    }
}

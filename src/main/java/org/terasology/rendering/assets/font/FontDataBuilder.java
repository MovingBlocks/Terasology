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

import com.google.common.collect.Maps;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.terasology.rendering.assets.texture.Texture;

import java.util.Map;

/**
 * @author Immortius
 */
public class FontDataBuilder {

    private int lineHeight;
    private TIntObjectMap<Texture> pages = new TIntObjectHashMap<>();
    private Map<Integer, FontCharacter> characters = Maps.newHashMap();

    private int currentCharacterId;
    private int characterX;
    private int characterY;
    private int characterWidth;
    private int characterHeight;
    private int characterXOffset;
    private int characterYOffset;
    private int characterXAdvance;
    private int characterPage;

    public FontDataBuilder() {
    }

    public FontData build() {
        return new FontData(lineHeight, characters);
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public void addPage(int pageId, Texture texture) {
        pages.put(pageId, texture);
    }

    public FontDataBuilder startCharacter(int characterId) {
        this.currentCharacterId = characterId;
        return this;
    }

    public FontDataBuilder setCharacterX(int value) {
        this.characterX = value;
        return this;
    }

    public FontDataBuilder setCharacterY(int value) {
        this.characterY = value;
        return this;
    }

    public FontDataBuilder setCharacterWidth(int value) {
        this.characterWidth = value;
        return this;
    }

    public FontDataBuilder setCharacterHeight(int value) {
        this.characterHeight = value;
        return this;
    }

    public FontDataBuilder setCharacterXOffset(int value) {
        this.characterXOffset = value;
        return this;
    }

    public FontDataBuilder setCharacterYOffset(int value) {
        this.characterYOffset = value;
        return this;
    }

    public FontDataBuilder setCharacterXAdvance(int value) {
        this.characterXAdvance = value;
        return this;
    }

    public FontDataBuilder setCharacterPage(int value) {
        this.characterPage = value;
        if (pages.get(value) == null) {
            throw new IllegalArgumentException("Invalid font - character on missing page '" + value + "'");
        }
        return this;
    }

    public FontDataBuilder endCharacter() {
        Texture page = pages.get(characterPage);
        FontCharacter character = new FontCharacter(((float) characterX / page.getWidth()), ((float) characterY / page.getHeight()),
                characterWidth, characterHeight, characterXOffset, characterYOffset, characterXAdvance, page);
        characters.put(currentCharacterId, character);
        return this;
    }

}

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

import org.terasology.asset.Asset;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.nui.HorizontalAlignment;

import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public interface Font extends Asset<FontData> {

    void drawString(int x, int y, String text, org.newdawn.slick.Color color);

    /**
     * Produces a map of texture to mesh to render the given text.
     *
     * @param lines
     * @return A map of texture to mesh, where each texture is a font page and each mesh is the characters of that mesh page.
     */
    Map<Material, Mesh> createTextMesh(List<String> lines, int width, HorizontalAlignment alignment);

    int getWidth(String text);

    int getWidth(Character c);

    int getHeight(String text);

    int getLineHeight();
}

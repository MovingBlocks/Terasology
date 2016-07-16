/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.editor;

import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.itemRendering.StringTextIconRenderer;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeValue;
import org.terasology.utilities.Assets;

import java.util.Optional;

public class NUIEditorItemRenderer extends StringTextIconRenderer<JsonTreeValue> {
    private static final String OBJECT_TEXTURE_NAME = "object";
    private static final String ARRAY_TEXTURE_NAME = "array";

    public NUIEditorItemRenderer() {
        super(false, 2, 2, 5, 5);
    }

    @Override
    public String getString(JsonTreeValue value) {
        return value.toString();
    }

    @Override
    public Texture getTexture(JsonTreeValue value) {
        String textureName;

        if (value.getType() == JsonTreeValue.Type.KEY_VALUE_PAIR && value.getKey().equalsIgnoreCase("type")) {
            textureName = value.getValue().toString();
        } else if (value.getType() == JsonTreeValue.Type.OBJECT && value.getKey() == null) {
            textureName = OBJECT_TEXTURE_NAME;
        } else if (value.getType() == JsonTreeValue.Type.ARRAY && value.getKey() == null) {
            textureName = ARRAY_TEXTURE_NAME;
        } else {
            textureName = value.getKey();
        }

        Optional<Texture> texture = Assets.getTexture(String.format("engine:editor_%s", textureName));
        if (texture.isPresent()) {
            return texture.get();
        } else {
            return null;
        }
    }
}

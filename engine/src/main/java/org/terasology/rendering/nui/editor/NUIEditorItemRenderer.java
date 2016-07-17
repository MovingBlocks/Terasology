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
import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeValue;
import org.terasology.rendering.nui.widgets.treeView.Tree;
import org.terasology.rendering.nui.widgets.treeView.TreeModel;
import org.terasology.utilities.Assets;

import java.util.Optional;

public class NUIEditorItemRenderer extends StringTextIconRenderer<JsonTreeValue> {
    private static final String OBJECT_TEXTURE_NAME = "object";
    private static final String ARRAY_TEXTURE_NAME = "array";
    private static final String ATTRIBUTE_TEXTURE_NAME = "attribute";

    /**
     * The tree model the nodes of which are to be rendered
     * using this renderer.
     */
    private TreeModel editorTreeViewModel;

    public NUIEditorItemRenderer(TreeModel editorTreeViewModel) {
        super(false, 2, 2, 5, 5);
        this.editorTreeViewModel = editorTreeViewModel;
    }

    @Override
    public String getString(JsonTreeValue value) {
        if (value.getType() == JsonTreeValue.Type.OBJECT && value.getKey() == null) {
            JsonTree node = (JsonTree) editorTreeViewModel.getNodeByValue(value);

            // If the current node is a widget with a set id, use the id for the string representation.
            if (node != null) {
                for (Tree<JsonTreeValue> child : node.getChildren()) {
                    JsonTreeValue childValue = child.getValue();
                    if (childValue.getType() == JsonTreeValue.Type.KEY_VALUE_PAIR
                            && childValue.getKey().equalsIgnoreCase("id")) {
                        return String.format("{%s}", childValue.getValue());
                    }
                }
            }
        }

        return value.toString();
    }

    @Override
    public Texture getTexture(JsonTreeValue value) {
        String textureName = null;

        if (value.getType() == JsonTreeValue.Type.KEY_VALUE_PAIR) {
            textureName = ATTRIBUTE_TEXTURE_NAME;
        } else if (value.getType() == JsonTreeValue.Type.OBJECT && value.getKey() == null) {
            JsonTree node = (JsonTree) editorTreeViewModel.getNodeByValue(value);

            // If the node is a widget, use the type's name for the texture instead of the default name.
            if (node != null) {
                for (Tree<JsonTreeValue> child : node.getChildren()) {
                    JsonTreeValue childValue = child.getValue();
                    if (childValue.getType() == JsonTreeValue.Type.KEY_VALUE_PAIR
                            && childValue.getKey().equalsIgnoreCase("type")) {
                        textureName = (String) childValue.getValue();
                    }
                }
            }

            if (textureName == null) {
                textureName = OBJECT_TEXTURE_NAME;
            }
        } else if (value.getType() == JsonTreeValue.Type.ARRAY && value.getKey() == null) {
            textureName = ARRAY_TEXTURE_NAME;
        } else {
            textureName = value.getKey();
        }

        if (textureName != null) {
            Optional<Texture> texture = Assets.getTexture(String.format("engine:editor_%s", textureName));
            if (texture.isPresent()) {
                return texture.get();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}

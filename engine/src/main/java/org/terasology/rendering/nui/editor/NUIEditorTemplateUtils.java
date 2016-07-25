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

import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeValue;

public class NUIEditorTemplateUtils {
    private static final String SAMPLE_LABEL_TEXT = "Welcome to the Terasology NUI editor!\r\n"
                                                    + "TODO: add tiny tutorial, keybinds etc.";

    /**
     * @return The {@link JsonTree} to be used as an initial tree template within {@link NUIEditorScreen}.
     */
    public static JsonTree newTree() {
        JsonTree tree = new JsonTree(new JsonTreeValue(null, null, JsonTreeValue.Type.OBJECT));
        tree.addChild(new JsonTreeValue("type", "PlaceholderScreenLayer", JsonTreeValue.Type.KEY_VALUE_PAIR));
        tree.addChild(new JsonTreeValue("skin", "engine:default", JsonTreeValue.Type.KEY_VALUE_PAIR));

        JsonTree layout = new JsonTree(new JsonTreeValue("contents", null, JsonTreeValue.Type.OBJECT));
        layout.addChild(new JsonTreeValue("type", "relativeLayout", JsonTreeValue.Type.KEY_VALUE_PAIR));

        JsonTree contents = new JsonTree(new JsonTreeValue("contents", null, JsonTreeValue.Type.ARRAY));

        JsonTree label = newWidget("UILabel", "sampleLabel");
        label.addChild(new JsonTreeValue("text", SAMPLE_LABEL_TEXT, JsonTreeValue.Type.KEY_VALUE_PAIR));

        contents.addChild(label);
        layout.addChild(contents);
        tree.addChild(layout);
        return tree;
    }

    public static JsonTree newWidget(String type, String id) {
        JsonTree widget = new JsonTree(new JsonTreeValue(null, null, JsonTreeValue.Type.OBJECT));
        widget.addChild(new JsonTreeValue("type", type, JsonTreeValue.Type.KEY_VALUE_PAIR));
        widget.addChild(new JsonTreeValue("id", id, JsonTreeValue.Type.KEY_VALUE_PAIR));

        JsonTree layoutInfo = new JsonTree(new JsonTreeValue("layoutInfo", null, JsonTreeValue.Type.OBJECT));
        layoutInfo.addChild(new JsonTreeValue("cc", "", JsonTreeValue.Type.KEY_VALUE_PAIR));

        widget.addChild(layoutInfo);
        return widget;
    }
}

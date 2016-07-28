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

import org.terasology.rendering.nui.layouts.relative.RelativeLayout;
import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeValue;

public class NUIEditorNodeBuilder {
    private static final String SAMPLE_LABEL_TEXT = "Welcome to the Terasology NUI editor!\r\n" + "TODO: add tiny " +
                                                    "tutorial, keybinds etc.";

    /**
     * @return The {@link JsonTree} to be used as an initial screen template within {@link NUIEditorScreen}.
     */
    public static JsonTree createNewScreen() {
        JsonTree tree = new JsonTree(new JsonTreeValue(null, null, JsonTreeValue.Type.OBJECT));
        tree.addChild(new JsonTreeValue("type", "PlaceholderScreenLayer", JsonTreeValue.Type.KEY_VALUE_PAIR));
        tree.addChild(new JsonTreeValue("skin", "engine:default", JsonTreeValue.Type.KEY_VALUE_PAIR));

        JsonTree layout = new JsonTree(new JsonTreeValue("contents", null, JsonTreeValue.Type.OBJECT));
        layout.addChild(new JsonTreeValue("type", "RelativeLayout", JsonTreeValue.Type.KEY_VALUE_PAIR));

        JsonTree contents = new JsonTree(new JsonTreeValue("contents", null, JsonTreeValue.Type.ARRAY));

        JsonTree label = createNewWidget("UILabel", "sampleLabel", true);
        label.addChild(new JsonTreeValue("text", SAMPLE_LABEL_TEXT, JsonTreeValue.Type.KEY_VALUE_PAIR));

        contents.addChild(label);
        layout.addChild(contents);
        tree.addChild(layout);
        return tree;
    }

    /**
     * @param type          The type of the widget.
     * @param id            The id of the widget.
     * @param addLayoutInfo Whether a few layout settings from {@link RelativeLayout} should be added.
     * @return The {@link JsonTree} with the given type/id to be used as an empty widget template within {@link NUIEditorScreen}.
     */
    public static JsonTree createNewWidget(String type, String id, boolean addLayoutInfo) {
        JsonTree widget = new JsonTree(new JsonTreeValue(null, null, JsonTreeValue.Type.OBJECT));
        widget.addChild(new JsonTreeValue("type", type, JsonTreeValue.Type.KEY_VALUE_PAIR));
        widget.addChild(new JsonTreeValue("id", id, JsonTreeValue.Type.KEY_VALUE_PAIR));

        JsonTree layoutInfo = new JsonTree(new JsonTreeValue("layoutInfo", null, JsonTreeValue.Type.OBJECT));
        layoutInfo.addChild(new JsonTreeValue("width", 500, JsonTreeValue.Type.KEY_VALUE_PAIR));

        JsonTree hPosition = new JsonTree(new JsonTreeValue("position-horizontal-center", null, JsonTreeValue.Type
            .OBJECT));
        JsonTree vPosition = new JsonTree(new JsonTreeValue("position-vertical-center", null, JsonTreeValue.Type
            .OBJECT));

        layoutInfo.addChild(hPosition);
        layoutInfo.addChild(vPosition);
        widget.addChild(layoutInfo);
        return widget;
    }
}

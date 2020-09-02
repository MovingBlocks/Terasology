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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.engine.subsystem.headless.renderer.HeadlessCanvasRenderer;
import org.terasology.i18n.TranslationSystem;
import org.terasology.i18n.TranslationSystemImpl;
import org.terasology.input.InputSystem;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.editor.layers.PlaceholderScreen;
import org.terasology.rendering.nui.editor.utils.NUIEditorNodeUtils;
import org.terasology.nui.canvas.CanvasRenderer;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.nui.layouts.RowLayout;
import org.terasology.nui.layouts.RowLayoutHint;
import org.terasology.nui.layouts.relative.HorizontalInfo;
import org.terasology.nui.layouts.relative.RelativeLayout;
import org.terasology.nui.layouts.relative.RelativeLayoutHint;
import org.terasology.nui.layouts.relative.VerticalInfo;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.treeView.JsonTree;
import org.terasology.nui.widgets.treeView.JsonTreeConverter;
import org.terasology.rendering.nui.internal.TerasologyCanvasRenderer;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ContextMenuUtilsTest extends TerasologyTestingEnvironment {
    private static JsonTree inputTree;

    @BeforeAll
    public static void setupInput() {
        context.put(InputSystem.class, new InputSystem());
        context.put(TranslationSystem.class, new TranslationSystemImpl(context));
        context.put(CanvasRenderer.class, new HeadlessCanvasRenderer());
        context.put(NUIManager.class, new NUIManagerInternal((TerasologyCanvasRenderer) context.get(CanvasRenderer.class), context));

        File file = new File(ContextMenuUtilsTest.class.getClassLoader().getResource("contextMenuBuilderInput.ui").getFile());
        String content = null;
        try {
            content = Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            fail("Could not load input file");
        }
        inputTree = JsonTreeConverter.serialize(new JsonParser().parse(content));
    }

    @Test
    public void testNodeTypes() {
        JsonTree currentNode = inputTree;

        assertEquals(PlaceholderScreen.class, getNodeType(currentNode));
        currentNode = currentNode.getChildWithKey("contents");
        assertEquals(RelativeLayout.class, getNodeType(currentNode));
        currentNode = currentNode.getChildWithKey("contents");
        assertEquals(UIButton.class, getNodeType(currentNode.getChildAt(0)));
        assertEquals(RelativeLayoutHint.class, getNodeType(currentNode.getChildAt(0).getChildWithKey("layoutInfo")));
        assertEquals(VerticalInfo.class, getNodeType(currentNode.getChildAt(0)
            .getChildWithKey("layoutInfo").getChildWithKey("position-top")));
        assertEquals(HorizontalInfo.class, getNodeType(currentNode.getChildAt(0)
            .getChildWithKey("layoutInfo").getChildWithKey("position-horizontal-center")));
        currentNode = currentNode.getChildAt(1);
        assertEquals(RowLayout.class, getNodeType(currentNode));
        assertEquals(RelativeLayoutHint.class, getNodeType(currentNode.getChildWithKey("layoutInfo")));
        currentNode = currentNode.getChildWithKey("contents").getChildAt(0);
        assertEquals(UILabel.class, getNodeType(currentNode));
        assertEquals(RowLayoutHint.class, getNodeType(currentNode.getChildWithKey("layoutInfo")));
    }

    private Class getNodeType(JsonTree node) {
        return NUIEditorNodeUtils.getNodeInfo(node, context.get(NUIManager.class)).getNodeClass();
    }
}

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
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.engine.subsystem.headless.renderer.HeadlessCanvasRenderer;
import org.terasology.i18n.TranslationSystem;
import org.terasology.i18n.TranslationSystemImpl;
import org.terasology.input.InputSystem;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.CanvasRenderer;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.RowLayoutHint;
import org.terasology.rendering.nui.layouts.relative.HorizontalInfo;
import org.terasology.rendering.nui.layouts.relative.RelativeLayout;
import org.terasology.rendering.nui.layouts.relative.RelativeLayoutHint;
import org.terasology.rendering.nui.layouts.relative.VerticalInfo;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeConverter;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ContextMenuBuilderTest extends TerasologyTestingEnvironment {
    private static JsonTree inputTree;
    private static NUIEditorContextMenuBuilder builder;

    @BeforeClass
    public static void setupInput() {
        context.put(InputSystem.class, new InputSystem());
        context.put(TranslationSystem.class, new TranslationSystemImpl(context));
        context.put(CanvasRenderer.class, new HeadlessCanvasRenderer());
        context.put(NUIManager.class, new NUIManagerInternal(context.get(CanvasRenderer.class), context));

        File file = new File(ContextMenuBuilderTest.class.getClassLoader().getResource("contextMenuBuilderInput.ui").getFile());
        String content = null;
        try {
            content = Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            fail("Could not load input file");
        }
        inputTree = JsonTreeConverter.serialize(new JsonParser().parse(content));
        builder = new NUIEditorContextMenuBuilder();
        builder.setManager(context.get(NUIManager.class));
    }

    @Test
    public void testNodeTypes() {
        JsonTree currentNode = inputTree;
        assertEquals(PlaceholderScreenLayer.class, builder.getNodeType(currentNode));
        currentNode = currentNode.getChildWithKey("contents");
        assertEquals(RelativeLayout.class, builder.getNodeType(currentNode));
        currentNode = currentNode.getChildWithKey("contents");
        assertEquals(UIButton.class, builder.getNodeType(currentNode.getChildAt(0)));
        assertEquals(RelativeLayoutHint.class, builder.getNodeType(currentNode.getChildAt(0).getChildWithKey("layoutInfo")));
        assertEquals(VerticalInfo.class, builder.getNodeType(currentNode.getChildAt(0)
            .getChildWithKey("layoutInfo").getChildWithKey("position-top")));
        assertEquals(HorizontalInfo.class, builder.getNodeType(currentNode.getChildAt(0)
            .getChildWithKey("layoutInfo").getChildWithKey("position-horizontal-center")));
        currentNode = currentNode.getChildAt(1);
        assertEquals(RowLayout.class, builder.getNodeType(currentNode));
        assertEquals(RelativeLayoutHint.class, builder.getNodeType(currentNode.getChildWithKey("layoutInfo")));
        currentNode = currentNode.getChildWithKey("contents").getChildAt(0);
        assertEquals(UILabel.class, builder.getNodeType(currentNode));
        assertEquals(RowLayoutHint.class, builder.getNodeType(currentNode.getChildWithKey("layoutInfo")));
    }
}

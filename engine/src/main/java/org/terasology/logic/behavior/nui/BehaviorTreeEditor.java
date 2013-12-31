/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.nui;

import com.google.common.collect.Lists;
import org.terasology.engine.CoreRegistry;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.BehaviorSystem;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UIDropdown;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layout.MigLayout;
import org.terasology.rendering.nui.layout.ZoomableLayout;

import java.util.Arrays;
import java.util.List;

/**
 * @author synopia
 */
public class BehaviorTreeEditor extends UIScreen {
    private final BTLayout layout;
    private final BehaviorNodeFactory nodeFactory;

    public BehaviorTreeEditor() {
        nodeFactory = CoreRegistry.get(BehaviorNodeFactory.class);
        layout = new BTLayout();
        layout.init(0, 0, 50, 50, 1200, 1000);
        MigLayout treeSelectBar = new MigLayout();
        treeSelectBar.addElement(new UIDropdown<BehaviorTree>("tree"), "w 180!, h 40!");
        treeSelectBar.addElement(new UIDropdown<Interpreter>("entity"), "w 180!, h 40!");
        MigLayout debugTools = new MigLayout();
        debugTools.addElement(new UIButton("stop", "[]"), "w 40!, h 40!");
        debugTools.addElement(new UIButton("pause", "||"), "w 40!, h 40!");
        debugTools.addElement(new UIButton("step", "|>"), "w 40!, h 40!");
        debugTools.addElement(new UIButton("continue", ">>"), "w 40!, h 40!");
        MigLayout palette = createPalette();
        MigLayout migLayout = new MigLayout("", "[min][grow][min]", "");
        migLayout.addElement(treeSelectBar, "cell 0 0 3");
        migLayout.addElement(debugTools, "cell 0 1 3");
        migLayout.addElement(palette, "cell 0 2, top");
        migLayout.addElement(layout, "cell 1 2, w 500!, h 500!");
        setContents(migLayout);

        find("tree", UIDropdown.class).bindSelection(new Binding<BehaviorTree>() {
            @Override
            public BehaviorTree get() {
                return null;
            }

            @Override
            public void set(BehaviorTree value) {
                layout.setTree(value);
            }
        });
        find("tree", UIDropdown.class).bindOptions(new Binding<List<BehaviorTree>>() {
            @Override
            public List<BehaviorTree> get() {
                return Lists.newArrayList(CoreRegistry.get(BehaviorSystem.class).getTrees());
            }

            @Override
            public void set(List<BehaviorTree> value) {
            }
        });

        find("entity", UIDropdown.class).bindOptions(new Binding<List<Interpreter>>() {
            @Override
            public List<Interpreter> get() {
                BehaviorTree selection = (BehaviorTree) find("tree", UIDropdown.class).getSelection();
                if (selection != null) {
                    return CoreRegistry.get(BehaviorSystem.class).getInterpreter(selection);
                } else {
                    return Arrays.asList();
                }
            }

            @Override
            public void set(List<Interpreter> value) {
            }
        });

    }

    private MigLayout createPalette() {
        MigLayout palette = new MigLayout();
        UIDropdown<BehaviorNodeComponent> item = new UIDropdown<>("palette.items");
        item.bindOptions(new Binding<List<BehaviorNodeComponent>>() {
            @Override
            public List<BehaviorNodeComponent> get() {
                return Lists.newArrayList(CoreRegistry.get(BehaviorNodeFactory.class).getNodeComponents());
            }

            @Override
            public void set(List<BehaviorNodeComponent> value) {
            }
        });
        palette.addElement(item, "w 150!, h 40!, wrap");
        return palette;
    }

    private static class BTLayout extends ZoomableLayout {
        public void setTree(BehaviorTree tree) {
            removeAll();
            for (RenderableNode widget : tree.getRenderableNodes()) {
                addWidget(widget);
            }
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            try (SubRegion subRegion = canvas.subRegion(canvas.getRegion(), false)) {
                canvas.setDrawOnTop(true);
                for (UIWidget widget : this) {
                    if (widget instanceof RenderableNode) {
                        RenderableNode renderableNode = (RenderableNode) widget;
                        for (Port port : renderableNode.getPorts()) {
                            int sx = worldToScreenX(renderableNode.getPosition().x + port.midX());
                            int sy = worldToScreenY(renderableNode.getPosition().y + port.midY());
                            Port targetPort = port.getTargetPort();
                            if (port.isInput() || targetPort == null) {
                                continue;
                            }
                            int ex = worldToScreenX(targetPort.getSourceNode().getPosition().x + targetPort.midX());
                            int ey = worldToScreenY(targetPort.getSourceNode().getPosition().y + targetPort.midY());
                            canvas.drawLine(sx, sy, ex, ey, Color.WHITE);
                        }
                    }
                }
                canvas.setDrawOnTop(false);
            }
        }
    }
}

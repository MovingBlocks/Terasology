/*
 * Copyright 2014 MovingBlocks
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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import org.terasology.context.Context;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.BehaviorSystem;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.layers.mainMenu.EnterTextPopup;
import org.terasology.rendering.nui.layouts.PropertyLayout;
import org.terasology.rendering.nui.properties.OneOfProviderFactory;
import org.terasology.rendering.nui.properties.PropertyProvider;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UIList;

import com.google.common.collect.Lists;

/**
 */
public class BehaviorEditorScreen extends CoreScreenLayer {

    public static final String PALETTE_ITEM_OPEN = "--";
    public static final String PALETTE_ITEM_CLOSE = "++";

    private PropertyLayout entityProperties;
    private BehaviorEditor behaviorEditor;
    private PropertyLayout properties;
    private UIDropdown<BehaviorTree> selectTree;
    private UIDropdown<Interpreter> selectActorEntity;
    private UIList<BehaviorNodeComponent> palette;
    private BehaviorTree selectedTree;
    private Interpreter selectedInterpreter;
    private RenderableNode selectedNode;
    private BehaviorDebugger debugger;
    private List<BehaviorNodeComponent> paletteItems;

    @In
    private NUIManager nuiManager;
    @In
    private BehaviorNodeFactory nodeFactory;
    @In
    private BehaviorSystem behaviorSystem;
    @In
    private OneOfProviderFactory providerFactory;

    @In
    private Context context;

    @Override
    public void initialise() {
        debugger = new BehaviorDebugger(nodeFactory);
        entityProperties = find("entity_properties", PropertyLayout.class);
        behaviorEditor = find("tree", BehaviorEditor.class);
        properties = find("properties", PropertyLayout.class);
        selectTree = find("select_tree", UIDropdown.class);
        selectActorEntity = find("select_entity", UIDropdown.class);
        palette = find("palette", UIList.class);

        behaviorEditor.initialize(context);
        behaviorEditor.bindSelection(new Binding<RenderableNode>() {
            private PropertyProvider provider = new PropertyProvider();

            @Override
            public RenderableNode get() {
                return selectedNode;
            }

            @Override
            public void set(RenderableNode value) {
                selectedNode = value;
                properties.clear();
                if (value != null) {
                    properties.addProperties("Behavior Node", provider.createProperties(value.getNode()));
                }
            }
        });

        Binding<List<BehaviorTree>> treeBinding = new ReadOnlyBinding<List<BehaviorTree>>() {
            @Override
            public List<BehaviorTree> get() {
                return behaviorSystem.getTrees();
            }
        };
        selectTree.bindOptions(treeBinding);
        providerFactory.register("behaviorTrees", treeBinding);

        selectTree.bindSelection(new Binding<BehaviorTree>() {
            @Override
            public BehaviorTree get() {
                return behaviorEditor.getTree();
            }

            @Override
            public void set(BehaviorTree value) {
                selectedTree = value;
                behaviorEditor.setTree(value);
                updateDebugger();
            }
        });

        selectActorEntity.bindOptions(new ReadOnlyBinding<List<Interpreter>>() {
            @Override
            public List<Interpreter> get() {
                return behaviorSystem.getInterpreter();
            }
        });

        selectActorEntity.bindSelection(new Binding<Interpreter>() {
            private PropertyProvider provider = new PropertyProvider();

            @Override
            public Interpreter get() {
                return selectedInterpreter;
            }

            @Override
            public void set(Interpreter value) {
                if (selectedInterpreter != null) {
                    selectedInterpreter.setDebugger(null);
                }
                selectedInterpreter = value;
                if (selectedInterpreter != null) {
                    EntityRef entity = value.actor().getEntity();
                    entityProperties.clear();
                    for (Component component : entity.iterateComponents()) {
                        String name = component.getClass().getSimpleName().replace("Component", "");
                        entityProperties.addProperties(name, provider.createProperties(component));
                    }
                }
                updateDebugger();
            }
        });

        palette.bindSelection(new Binding<BehaviorNodeComponent>() {
            @Override
            public BehaviorNodeComponent get() {
                return null;
            }

            @Override
            public void set(BehaviorNodeComponent value) {
                switch (value.name.substring(0, 2)) {
                    case PALETTE_ITEM_OPEN:
                        int pos = paletteItems.indexOf(value) + 1;
                        while (shouldCollapse(pos)) {
                            paletteItems.remove(pos);
                        }
                        paletteItems.remove(pos - 1);
                        paletteItems.add(pos - 1, createCategory(value.category, false));
                        break;
                    case PALETTE_ITEM_CLOSE:
                        pos = paletteItems.indexOf(value);
                        paletteItems.remove(pos);
                        BehaviorNodeComponent categoryItem = createCategory(value.category, true);
                        paletteItems.add(pos, categoryItem);
                        paletteItems.addAll(pos + 1, nodeFactory.getNodesComponents(value.category));
                        break;
                    default:
                        behaviorEditor.createNode(value);
                        break;
                }
            }

            private boolean shouldCollapse(int pos) {
                if (pos < paletteItems.size()) {
                    String currentItemName = paletteItems.get(pos).name;
                    return !currentItemName.startsWith(PALETTE_ITEM_OPEN) &&
                           !currentItemName.startsWith(PALETTE_ITEM_CLOSE);
                }
                return false;
            }
        });
        palette.bindList(new ReadOnlyBinding<List<BehaviorNodeComponent>>() {
            @Override
            public List<BehaviorNodeComponent> get() {
                return paletteItems;
            }
        });
        palette.setItemRenderer(new ToStringTextRenderer<BehaviorNodeComponent>() {
            @Override
            public String getTooltip(BehaviorNodeComponent value) {
                return value.description;
            }
        });

        WidgetUtil.trySubscribe(this, "copy", button -> {
            Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String data = behaviorEditor.save();
            StringSelection contents1 = new StringSelection(data);
            systemClipboard.setContents(contents1, contents1);
        });

        WidgetUtil.trySubscribe(this, "layout", button -> {
            BehaviorTree selection = selectTree.getSelection();
            if (selection != null) {
                selection.layout(selectedNode);
            }
        });

        WidgetUtil.trySubscribe(this, "new", button -> {
            nuiManager.pushScreen("engine:enterTextPopup", EnterTextPopup.class).bindInput(new Binding<String>() {
                @Override
                public String get() {
                    return null;
                }

                @Override
                public void set(String value) {
                    BehaviorTree tree = behaviorSystem.createTree(value, new RepeatNode());
                    selectTree.setSelection(tree);
                }
            });
        });
        WidgetUtil.trySubscribe(this, "assign", button -> {
            if (selectedTree != null && selectedInterpreter != null) {
                EntityRef entity = selectedInterpreter.actor().getEntity();
                entity.removeComponent(BehaviorComponent.class);
                BehaviorComponent component = new BehaviorComponent();
                component.tree = selectedTree;
                entity.addComponent(component);
                List<Interpreter> interpreter = behaviorSystem.getInterpreter();
                selectActorEntity.setSelection(null);
                for (Interpreter i : interpreter) {
                    if (i.actor().getEntity().equals(entity)) {
                        selectActorEntity.setSelection(i);
                        break;
                    }
                }
            }
        });
        WidgetUtil.trySubscribe(this, "remove", button -> {
            if (selectedNode != null && selectedTree != null) {
                RenderableNode targetNode = selectedNode.getInputPort().getTargetNode();
                if (targetNode != null) {
                    for (int i = 0; i < targetNode.getChildrenCount(); i++) {
                        if (targetNode.getChild(i) == selectedNode) {
                            targetNode.withModel().removeChild(i);
                            break;
                        }
                    }
                }
                removeWidget(selectedNode);
                behaviorEditor.nodeClicked(null);
                behaviorSystem.treeModified(selectedTree);
            }
        });

        WidgetUtil.trySubscribe(this, "debug_run", button -> {
            if (debugger != null) {
                debugger.run();
            }
        });
        WidgetUtil.trySubscribe(this, "debug_pause", button -> {
            if (debugger != null) {
                debugger.pause();
            }
        });
        WidgetUtil.trySubscribe(this, "debug_reset", button -> {
            if (selectedInterpreter != null) {
                selectedInterpreter.reset();
            }
        });
        WidgetUtil.trySubscribe(this, "debug_step", button -> {
            if (debugger != null) {
                debugger.step();
            }
        });

        paletteItems = findPaletteItems();
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    private void removeWidget(RenderableNode node) {
        behaviorEditor.removeWidget(node);
        node.children().forEach(this::removeWidget);
    }

    private void updateDebugger() {
        if (selectedInterpreter != null && selectedTree != null) {
            debugger.setTree(selectedTree);
            selectedInterpreter.setDebugger(debugger);
        }
    }

    private List<BehaviorNodeComponent> findPaletteItems() {
        List<BehaviorNodeComponent> items = Lists.newArrayList();
        for (String category : nodeFactory.getCategories()) {
            BehaviorNodeComponent categoryItem = createCategory(category, true);
            items.add(categoryItem);
            items.addAll(nodeFactory.getNodesComponents(category));
        }
        return items;
    }

    private BehaviorNodeComponent createCategory(String category, boolean open) {
        String prefix = open ? PALETTE_ITEM_OPEN : PALETTE_ITEM_CLOSE;
        BehaviorNodeComponent categoryItem = new BehaviorNodeComponent();
        categoryItem.category = category;
        categoryItem.name = prefix + category.toUpperCase() + prefix;
        return categoryItem;
    }
}

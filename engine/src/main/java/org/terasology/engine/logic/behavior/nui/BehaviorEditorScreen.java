// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.nui;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.logic.behavior.core.ActionNode;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.BehaviorComponent;
import org.terasology.engine.logic.behavior.BehaviorSystem;
import org.terasology.engine.logic.behavior.Interpreter;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIDropdown;
import org.terasology.nui.widgets.UIList;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.ToStringTextRenderer;
import org.terasology.engine.rendering.nui.layers.mainMenu.EnterTextPopup;
import org.terasology.nui.layouts.PropertyLayout;
import org.terasology.nui.properties.OneOfProviderFactory;
import org.terasology.nui.properties.Property;
import org.terasology.nui.properties.PropertyProvider;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

public class BehaviorEditorScreen extends CoreScreenLayer {
    public static final Logger logger = LoggerFactory.getLogger(BehaviorEditorScreen.class);
    public static final String PALETTE_ITEM_OPEN = "--";
    public static final String PALETTE_ITEM_CLOSE = "++";

    private PropertyLayout entityProperties;
    private BehaviorEditor behaviorEditor;
    private PropertyLayout properties;
    private UIDropdown<BehaviorTree> selectTree;
    private UIDropdown<Interpreter> selectEntity;
    private UIList<BehaviorNodeComponent> palette;
    private BehaviorTree selectedTree;
    private Interpreter selectedInterpreter;
    private RenderableNode selectedNode;
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
        entityProperties = find("entity_properties", PropertyLayout.class);
        behaviorEditor = find("tree", BehaviorEditor.class);
        properties = find("properties", PropertyLayout.class);
        selectTree = find("select_tree", UIDropdown.class);
        selectEntity = find("select_entity", UIDropdown.class);
        palette = find("palette", UIList.class);
        behaviorEditor.initialize(context);
        behaviorEditor.bindSelection(new Binding<RenderableNode>() {
            private PropertyProvider provider = new PropertyProvider(context.get(ReflectFactory.class), providerFactory);

            @Override
            public RenderableNode get() {
                return selectedNode;
            }

            @Override
            public void set(RenderableNode value) {
                onNodeSelected(value, provider);
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
                onTreeSelected(value);
            }
        });
        selectEntity.bindOptions(new ReadOnlyBinding<List<Interpreter>>() {
            @Override
            public List<Interpreter> get() {
                return behaviorSystem.getInterpreters();
            }
        });
        selectEntity.bindSelection(new Binding<Interpreter>() {
            private PropertyProvider provider = new PropertyProvider(context.get(ReflectFactory.class), providerFactory);

            @Override
            public Interpreter get() {
                return selectedInterpreter;
            }

            @Override
            public void set(Interpreter value) {
                onEntitySelected(value, provider);
            }
        });
        palette.bindSelection(new Binding<BehaviorNodeComponent>() {
            @Override
            public BehaviorNodeComponent get() {
                return null;
            }

            @Override
            public void set(BehaviorNodeComponent value) {
                onPaletteSelected(value);
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
        WidgetUtil.trySubscribe(this, "copy", button -> onCopyPressed());
        WidgetUtil.trySubscribe(this, "layout", button -> onLayoutPressed());
        WidgetUtil.trySubscribe(this, "new", button -> onNewPressed());
        WidgetUtil.trySubscribe(this, "assign", button -> onAssignPressed());
        WidgetUtil.trySubscribe(this, "remove", button -> onRemovePressed());
        WidgetUtil.trySubscribe(this, "debug_run", button -> {
            if (selectedInterpreter != null) {
                selectedInterpreter.run();
            }
        });
        WidgetUtil.trySubscribe(this, "debug_pause", button -> {
            if (selectedInterpreter != null) {
                selectedInterpreter.pause();
            }
        });
        WidgetUtil.trySubscribe(this, "debug_reset", button -> {
            if (selectedInterpreter != null) {
                selectedInterpreter.reset();
            }
        });
        WidgetUtil.trySubscribe(this, "debug_step", button -> {
            if (selectedInterpreter != null) {
                selectedInterpreter.tick(0.1f);
            }
        });
        paletteItems = findPaletteItems();
    }

    private void onRemovePressed() {
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
    }

    private void onAssignPressed() {
        if (selectedTree != null && selectedInterpreter != null) {
            EntityRef minion = selectedInterpreter.actor().getEntity();
            minion.removeComponent(BehaviorComponent.class);
            BehaviorComponent component = new BehaviorComponent();
            component.tree = selectedTree;
            minion.addComponent(component);
            List<Interpreter> interpreter = behaviorSystem.getInterpreters();
            selectEntity.setSelection(null);
            for (Interpreter i : interpreter) {
                if (i.actor().getEntity() == minion) {
                    selectEntity.setSelection(i);
                    break;
                }
            }
        }
    }

    private void onNewPressed() {
        if (selectedNode != null) {
            nuiManager.pushScreen("engine:enterTextPopup", EnterTextPopup.class).bindInput(new Binding<String>() {
                @Override
                public String get() {
                    return null;
                }

                @Override
                public void set(String value) {
                    BehaviorEditorScreen.this.behaviorSystem.createTree(value, BehaviorEditorScreen.this.selectedNode.getNode());
                }
            });
        }
    }

    private void onLayoutPressed() {
        BehaviorTree selection = selectTree.getSelection();
        if (selection != null) {
            behaviorEditor.layout(selectedNode);
        }
    }

    private void onCopyPressed() {
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String data = behaviorEditor.save();
        StringSelection contents = new StringSelection(data);
        systemClipboard.setContents(contents, contents);
    }

    private void onPaletteSelected(BehaviorNodeComponent value) {
        switch (value.displayName.substring(0, 2)) {
            case PALETTE_ITEM_OPEN:
                int pos = paletteItems.indexOf(value) + 1;
                while (pos < paletteItems.size() && !paletteItems.get(pos).displayName.startsWith(PALETTE_ITEM_OPEN)) {
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

    private void onEntitySelected(Interpreter value, PropertyProvider provider) {
        if (selectedInterpreter != null) {
            selectedInterpreter.setCallback(null);
        }
        selectedInterpreter = value;
        if (selectedInterpreter != null) {
            EntityRef entity = value.actor().getEntity();
            onTreeSelected(selectedInterpreter.getTree());
            entityProperties.clear();
            for (Component component : entity.iterateComponents()) {
                String name = component.getClass().getSimpleName().replace("Component", "");
                List<Property<?, ?>> componentProperties = provider.createProperties(component);
                entityProperties.addProperties(name, componentProperties);
            }
            selectedInterpreter.setCallback(behaviorEditor);
        }

    }

    private void onTreeSelected(BehaviorTree value) {
        selectedTree = value;
        behaviorEditor.setTree(value);
    }

    private void onNodeSelected(RenderableNode value, PropertyProvider provider) {
        selectedNode = value;
        properties.clear();
        if (value != null) {
            BehaviorNode underlyingNode = value.getNode();
            if (underlyingNode instanceof ActionNode) {
                this.properties.addProperties("Behavior Node", provider.createProperties(((ActionNode) underlyingNode).getAction()));
            }
            properties.addProperties("Behavior Node", provider.createProperties(value.getNode()));
        }
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    private void removeWidget(RenderableNode node) {
        behaviorEditor.removeWidget(node);
        for (RenderableNode renderableNode : node.children()) {
            removeWidget(renderableNode);
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
        categoryItem.displayName = prefix + category.toUpperCase() + prefix;
        return categoryItem;
    }
}

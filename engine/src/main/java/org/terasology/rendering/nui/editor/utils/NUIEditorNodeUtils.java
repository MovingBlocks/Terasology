// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.editor.utils;

import com.google.common.collect.Queues;
import com.google.gson.annotations.SerializedName;
import org.reflections.ReflectionUtils;
import org.terasology.engine.module.ModuleContext;
import org.terasology.nui.UILayout;
import org.terasology.nui.UIWidget;
import org.terasology.nui.layouts.relative.RelativeLayout;
import org.terasology.nui.skin.UIStyleFragment;
import org.terasology.nui.widgets.treeView.JsonTree;
import org.terasology.nui.widgets.treeView.JsonTreeValue;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.editor.layers.NUIEditorScreen;
import org.terasology.rendering.nui.editor.layers.NUISkinEditorScreen;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class NUIEditorNodeUtils {
    private static final String SAMPLE_LABEL_TEXT = "Welcome to the Terasology NUI editor!\r\n" +
        "Visit https://github.com/Terasology/TutorialNui/wiki for a quick overview of the editor,\r\n" +
        "as well as the NUI framework itself.";

    private NUIEditorNodeUtils() {
    }

    /**
     * @return The {@link JsonTree} to be used as an initial screen template within {@link NUIEditorScreen}.
     */
    public static JsonTree createNewScreen() {
        JsonTree tree = new JsonTree(new JsonTreeValue(null, null, JsonTreeValue.Type.OBJECT));
        tree.addChild(new JsonTreeValue("type", "PlaceholderScreen", JsonTreeValue.Type.KEY_VALUE_PAIR));
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
     * @return The {@link JsonTree} to be used as an initial skin template within {@link NUISkinEditorScreen}.
     */
    public static JsonTree createNewSkin() {
        JsonTree tree = new JsonTree(new JsonTreeValue(null, null, JsonTreeValue.Type.OBJECT));
        tree.addChild(new JsonTreeValue("inherit", "default", JsonTreeValue.Type.KEY_VALUE_PAIR));
        tree.addChild(new JsonTreeValue("elements", null, JsonTreeValue.Type.OBJECT));
        tree.addChild(new JsonTreeValue("families", null, JsonTreeValue.Type.OBJECT));
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

        if (addLayoutInfo) {
            layoutInfo.addChild(new JsonTreeValue("width", 500, JsonTreeValue.Type.KEY_VALUE_PAIR));

            JsonTree hPosition = new JsonTree(new JsonTreeValue("position-horizontal-center", null, JsonTreeValue.Type
                .OBJECT));
            JsonTree vPosition = new JsonTree(new JsonTreeValue("position-vertical-center", null, JsonTreeValue.Type
                .OBJECT));

            layoutInfo.addChild(hPosition);
            layoutInfo.addChild(vPosition);
        }

        widget.addChild(layoutInfo);
        return widget;
    }

    private static Deque<JsonTree> getPathToRoot(JsonTree node) {
        Deque<JsonTree> pathToRoot = Queues.newArrayDeque();

        // Create a stack with the root node at the top and the argument at the bottom.
        JsonTree currentNode = node;
        while (!currentNode.isRoot()) {
            pathToRoot.push(currentNode);
            currentNode = (JsonTree) currentNode.getParent();
        }
        pathToRoot.push(currentNode);
        return pathToRoot;
    }

    /**
     * @param node       A node in an asset tree.
     * @param nuiManager The {@link NUIManager} to be used for widget type resolution.
     * @return The info about this node's type.
     */
    public static NodeInfo getNodeInfo(JsonTree node, NUIManager nuiManager) {
        Deque<JsonTree> pathToRoot = getPathToRoot(node);

        // Start iterating from top to bottom.
        Class currentClass = null;
        Class activeLayoutClass = null;
        for (JsonTree n : pathToRoot) {
            if (n.isRoot()) {
                // currentClass is not set - set it to the screen type.
                String type = (String) n.getChildWithKey("type").getValue().getValue();
                currentClass = nuiManager
                    .getWidgetMetadataLibrary()
                    .resolve(type, ModuleContext.getContext())
                    .getType();
            } else {
                if (List.class.isAssignableFrom(currentClass)
                    && n.getValue().getKey() == null
                    && "contents".equals(n.getParent().getValue().getKey())) {
                    // Transition from a "contents" list to a UIWidget.
                    currentClass = UIWidget.class;
                } else {
                    // Retrieve the type of an unspecified UIWidget.
                    if (currentClass == UIWidget.class && n.hasSiblingWithKey("type")) {
                        String type = (String) n.getSiblingWithKey("type").getValue().getValue();
                        currentClass = nuiManager
                            .getWidgetMetadataLibrary()
                            .resolve(type, ModuleContext.getContext())
                            .getType();
                    }

                    // If the current class is a layout, remember its' value (but do not set until later on!)
                    Class layoutClass = null;
                    if (UILayout.class.isAssignableFrom(currentClass)) {
                        layoutClass = currentClass;
                    }

                    if (UILayout.class.isAssignableFrom(currentClass) && "contents".equals(n.getValue().getKey())) {
                        // "contents" fields of a layout are always (widget) lists.
                        currentClass = List.class;
                    } else if (UIWidget.class.isAssignableFrom(currentClass) && "layoutInfo".equals(n.getValue().getKey())) {
                        // Set currentClass to the layout hint type for the active layout.
                        currentClass = (Class) ReflectionUtil.getTypeParameter(activeLayoutClass.getGenericSuperclass(), 0);
                    } else {
                        String value = n.getValue().toString();
                        Set<Field> fields = ReflectionUtils.getAllFields(currentClass);
                        Optional<Field> newField = fields
                            .stream().filter(f -> f.getName().equalsIgnoreCase(value)).findFirst();

                        if (newField.isPresent()) {
                            currentClass = newField.get().getType();
                        } else {
                            Optional<Field> serializedNameField = fields
                                .stream()
                                .filter(f -> f.isAnnotationPresent(SerializedName.class)
                                    && f.getAnnotation(SerializedName.class).value().equals(value)).findFirst();
                            if (serializedNameField.isPresent()) {
                                currentClass = serializedNameField.get().getType();
                            } else {
                                return null;
                            }
                        }
                    }

                    // Set the layout class value.
                    if (layoutClass != null) {
                        activeLayoutClass = layoutClass;
                    }
                }
            }
        }

        // If the final result is a generic UIWidget, attempt to retrieve its' type.
        if (currentClass == UIWidget.class && node.hasChildWithKey("type")) {
            String type = (String) node.getChildWithKey("type").getValue().getValue();
            currentClass = nuiManager
                .getWidgetMetadataLibrary()
                .resolve(type, ModuleContext.getContext())
                .getType();
        }
        return new NodeInfo(currentClass, activeLayoutClass);
    }

    /**
     * @param node A node in an asset tree.
     * @return The info about this node's type.
     */
    public static NodeInfo getSkinNodeInfo(JsonTree node) {
        Deque<JsonTree> pathToRoot = getPathToRoot(node);

        // Start iterating from top to bottom.
        Class nodeClass = null;
        for (JsonTree n : pathToRoot) {
            if (n.isRoot()) {
                nodeClass = UIStyleFragment.class;
            } else {
                if ("elements".equals(n.getValue().getKey()) || "families".equals(n.getValue().getKey())) {
                    nodeClass = null;
                } else if (n.getParent().getValue().getKey() != null
                    && ("elements".equals(n.getParent().getValue().getKey())
                    || "families".equals(n.getParent().getValue().getKey()))) {
                    nodeClass = UIStyleFragment.class;
                } else {
                    String value = n.getValue().toString();
                    Set<Field> fields = ReflectionUtils.getAllFields(nodeClass);
                    Optional<Field> newField = fields
                        .stream().filter(f -> f.getName().equalsIgnoreCase(value)).findFirst();

                    if (newField.isPresent()) {
                        nodeClass = newField.get().getType();
                    } else {
                        Optional<Field> serializedNameField = fields
                            .stream()
                            .filter(f -> f.isAnnotationPresent(SerializedName.class)
                                && f.getAnnotation(SerializedName.class).value().equals(value)).findFirst();
                        if (serializedNameField.isPresent()) {
                            nodeClass = serializedNameField.get().getType();
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        return new NodeInfo(nodeClass, null);
    }

    /**
     * Contains information about a node's types.
     */
    public static class NodeInfo {
        /**
         * The type of the field this node represents.
         */
        private Class nodeClass;
        /**
         * The type of the layout this node is a part of. null if it's not a part of a layout.
         */
        private Class layoutClass;

        public NodeInfo(Class nodeClass, Class layoutClass) {
            this.nodeClass = nodeClass;
            this.layoutClass = layoutClass;
        }

        public Class getNodeClass() {
            return nodeClass;
        }

        public Class getLayoutClass() {
            return layoutClass;
        }
    }
}

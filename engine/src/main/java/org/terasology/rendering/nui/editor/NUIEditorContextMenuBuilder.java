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

import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.ModuleContext;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UILayout;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.contextMenu.ContextMenuBuilder;
import org.terasology.rendering.nui.contextMenu.ContextMenuLevel;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.widgets.UpdateListener;
import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeValue;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NUIEditorContextMenuBuilder {

    private Logger logger = LoggerFactory.getLogger(NUIEditorContextMenuBuilder.class);

    // Context menu options.
    public static final String OPTION_ADD_EXTENDED = "Add...";
    public static final String OPTION_ADD_WIDGET = "Add Widget";
    public static final String OPTION_COPY = "Copy";
    public static final String OPTION_EDIT = "Edit";
    public static final String OPTION_PASTE = "Paste";

    /**
     * A {@link NUIManager} instance retrieved from the editor screen.
     */
    private NUIManager nuiManager;
    /**
     * External consumer methods to be used when creating the menu.
     */
    private Map<String, Consumer<JsonTree>> externalConsumers = Maps.newHashMap();
    /**
     * Listeners fired when one of the options within {@code LEVEL_ADD_EXTENDED} is selected.
     */
    private List<UpdateListener> addContextMenuListeners = Lists.newArrayList();

    public void setManager(NUIManager nuiManager) {
        this.nuiManager = nuiManager;
    }

    public void putConsumer(String key, Consumer<JsonTree> value) {
        externalConsumers.put(key, value);
    }

    public void subscribeAddContextMenu(UpdateListener listener) {
        addContextMenuListeners.add(listener);
    }

    public ContextMenuBuilder createPrimaryContextMenu(JsonTree node) {
        ContextMenuBuilder contextMenu = new ContextMenuBuilder();
        ContextMenuLevel primaryLevel = contextMenu.addLevel(true);

        JsonTreeValue.Type type = node.getValue().getType();

        // Create the ADD_EXTENDED level.
        if ((type == JsonTreeValue.Type.ARRAY && !node.getValue().getKey().equals("contents"))
            || type == JsonTreeValue.Type.OBJECT) {
            ContextMenuLevel addLevel = contextMenu.addLevel(false);
            primaryLevel.addNavigationOption(OPTION_ADD_EXTENDED, addLevel);
            createAddContextMenu(node, addLevel);
        }

        // If the node is a "contents" array, add the widget addition option (redirects to WidgetSelectionScreen).
        if (type == JsonTreeValue.Type.ARRAY && node.getValue().getKey().equals("contents")) {
            primaryLevel.addOption(OPTION_ADD_WIDGET, externalConsumers.get(OPTION_ADD_WIDGET), node, true);
        }

        // Always add the copy&paste options.
        primaryLevel.addOption(OPTION_COPY, externalConsumers.get(OPTION_COPY), node, true);
        primaryLevel.addOption(OPTION_PASTE, externalConsumers.get(OPTION_PASTE), node, true);

        // Unless the node is an OBJECT child of an ARRAY (should always have an empty key), add the edit option.
        if (type != JsonTreeValue.Type.NULL && !(type == JsonTreeValue.Type.OBJECT
                                                 && !node.isRoot()
                                                 && node.getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
            primaryLevel.addOption(OPTION_EDIT, externalConsumers.get(OPTION_EDIT), node, true);
        }

        return contextMenu;
    }

    public void createAddContextMenu(JsonTree node, ContextMenuLevel addLevel) {
        JsonTreeValue.Type type = node.getValue().getType();

        if (type == JsonTreeValue.Type.ARRAY) {
            // Add generic item addition options.
            addLevel.addOption("Boolean value", n -> {
                n.addChild(new JsonTreeValue(null, false, JsonTreeValue.Type.VALUE));
                addContextMenuListeners.forEach(UpdateListener::onAction);
            }, node, true);
            addLevel.addOption("Number value", n -> {
                n.addChild(new JsonTreeValue(null, 0.0f, JsonTreeValue.Type.VALUE));
                addContextMenuListeners.forEach(UpdateListener::onAction);
            }, node, true);
            addLevel.addOption("String value", n -> {
                n.addChild(new JsonTreeValue(null, "", JsonTreeValue.Type.VALUE));
                addContextMenuListeners.forEach(UpdateListener::onAction);
            }, node, true);
        } else if (type == JsonTreeValue.Type.OBJECT) {
            // Add a generic key/value pair addition option.
            addLevel.addOption("Key/value pair", n -> {
                n.addChild(new JsonTreeValue("", "", JsonTreeValue.Type.KEY_VALUE_PAIR));
                addContextMenuListeners.forEach(UpdateListener::onAction);
            }, node, true);

            // Populate the menu with additional options.
            if (node.hasChildWithKey("type")) {
                populateWidgetContextMenu(node, addLevel);
            } else if (node.getValue().getKey().equalsIgnoreCase("layoutInfo")) {
                populateLayoutContextMenu(node, addLevel);
            } else {
                populateGenericContextMenu(node, addLevel);
            }
        }
    }

    private void populateWidgetContextMenu(JsonTree node, ContextMenuLevel addLevel) {
        // Get the ClassMetadata of the widget with the given type.
        String type = (String) node.getChildWithKey("type").getValue().getValue();
        ClassMetadata<? extends UIWidget, ?> elementMetadata = nuiManager
            .getWidgetMetadataLibrary()
            .resolve(type, ModuleContext.getContext());

        if (elementMetadata != null) {
            for (FieldMetadata fieldMetadata : elementMetadata.getFields()) {
                Field field = fieldMetadata.getField();
                field.setAccessible(true);

                // Add options for all @LayoutConfig-annotated fields.
                if (field.isAnnotationPresent(LayoutConfig.class)) {
                    String name = getNodeName(field);
                    if (!node.hasChildWithKey(name)) {
                        addLevel.addOption(name, n -> {
                            try {
                                createChild(name, node, field, elementMetadata);
                            } catch (IllegalAccessException e) {
                                return;
                            }
                            addContextMenuListeners.forEach(UpdateListener::onAction);
                        }, node, true);
                    }
                }
            }
        } else {
            logger.warn("Unknown widget type: {}", type);
        }
    }

    private void populateLayoutContextMenu(JsonTree node, ContextMenuLevel addLevel) {
        String type = "";
        JsonTree currentNode = node;
        ClassMetadata layoutMetadata = null;
        Class layoutHintType = null;

        // Iterate down the tree and attempt to find a UILayout node.
        while (layoutHintType == null) {
            if (currentNode.hasSiblingWithKey("type")) {
                type = (String) currentNode.getSiblingWithKey("type").getValue().getValue();
                layoutMetadata = nuiManager.getWidgetMetadataLibrary().resolve(type, ModuleContext.getContext());

                // If one is found, attempt to get the layout hint type.
                if (layoutMetadata.getType().getGenericSuperclass() != null) {
                    layoutHintType = (Class) ReflectionUtil
                        .getTypeParameter(layoutMetadata.getType().getGenericSuperclass(), 0);
                }
            }
            if (layoutHintType == null) {
                if (currentNode.isRoot()) {
                    break;
                }
                currentNode = (JsonTree) currentNode.getParent();
            }
        }

        if (layoutHintType != null) {
            for (Field field : layoutHintType.getDeclaredFields()) {
                field.setAccessible(true);

                // Add options for all @LayoutConfig-annotated fields.
                if (field.isAnnotationPresent(LayoutConfig.class)) {
                    String name = getNodeName(field);
                    ClassMetadata finalMetadata = layoutMetadata;
                    if (!node.hasChildWithKey(name)) {
                        addLevel.addOption(name, n -> {
                            try {
                                createChild(name, node, field, finalMetadata);
                            } catch (IllegalAccessException e) {
                                return;
                            }
                            addContextMenuListeners.forEach(UpdateListener::onAction);
                        }, node, true);
                    }
                }
            }
        } else {
            logger.warn("Unknown layout type: {}", type);
        }
    }

    private void populateGenericContextMenu(JsonTree node, ContextMenuLevel addLevel) {
        if (node.hasSiblingWithKey("type")) {
            // Get the ClassMetadata of the widget with the given type.
            String type = (String) node.getSiblingWithKey("type").getValue().getValue();
            ClassMetadata elementMetadata = nuiManager.getWidgetMetadataLibrary().resolve(type, ModuleContext.getContext());

            // Get the type of the node as a field within the widget.
            Class clazz = elementMetadata.getField(node.getValue().getKey()).getType();

            if (clazz.equals(UIWidget.class)) {
                clazz = AbstractWidget.class;
            }

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String name = getNodeName(field);

                // Add options for all fields of the class.
                if (!node.hasChildWithKey(name)) {
                    field.setAccessible(true);
                    addLevel.addOption(name, n -> {
                        try {
                            createChild(name, node, field, elementMetadata);
                        } catch (IllegalAccessException e) {
                            return;
                        }
                        addContextMenuListeners.forEach(UpdateListener::onAction);
                    }, node, true);
                }
            }
        }
    }

    private void createChild(String name, JsonTree node, Field field, ClassMetadata metadata) throws IllegalAccessException {
        JsonTreeValue childValue = new JsonTreeValue();
        childValue.setKey(name);

        if (UISkin.class.isAssignableFrom(field.getType())) {
            // Skin fields should always be KEY_VALUE_PAIR nodes with string values.
            childValue.setValue("engine:default");
            childValue.setType(JsonTreeValue.Type.KEY_VALUE_PAIR);
        } else if (UILayout.class.isAssignableFrom(metadata.getType())) {
            // If the current node is a layout hint, add options to add its' nodes.
            Class layoutHintType = (Class) ReflectionUtil.getTypeParameter(metadata.getType().getGenericSuperclass(), 0);
            try {
                childValue.setValue(field.get(layoutHintType.newInstance()));
                childValue.setType(getNodeType(field.get(layoutHintType.newInstance())));
            } catch (Exception e) {
                childValue.setValue(getFieldValue(field, metadata));
                childValue.setType(getNodeType(getFieldValue(field, metadata)));
            }
        } else {
            if (isWidget(field, metadata)) {
                createWidgetChild(name, node);
                return;
            } else {
                childValue.setValue(getFieldValue(field, metadata));
                childValue.setType(getNodeType(getFieldValue(field, metadata)));
            }
        }

        node.addChild(childValue);
    }

    private boolean isWidget(Field field, ClassMetadata metadata) throws IllegalAccessException {
        // The field is a Binding<? extends UIWidget>.
        if (Binding.class.isAssignableFrom(field.getType())
            && UIWidget.class.isAssignableFrom((Class<?>)
            ((ParameterizedType) (field.getGenericType())).getActualTypeArguments()[0])) {
            return true;
        }

        // The field is UIWidget or its' override.
        if (UIWidget.class.isAssignableFrom(field.getType())) {
            return true;
        }
        return false;
    }

    private void createWidgetChild(String name, JsonTree node) {
        JsonTree widgetTree = new JsonTree(new JsonTreeValue(name, null, JsonTreeValue.Type.OBJECT));
        NUIEditorNodeBuilder
            .createNewWidget("UILabel", "newWidget", false)
            .getChildren()
            .forEach(widgetTree::addChild);
        widgetTree.addChild(new JsonTreeValue("text", "", JsonTreeValue.Type.KEY_VALUE_PAIR));
        node.addChild(widgetTree);
    }

    private String getNodeName(Field field) {
        return field.isAnnotationPresent(SerializedName.class)
            ? field.getAnnotation(SerializedName.class).value() : field.getName();
    }

    private JsonTreeValue.Type getNodeType(Object value) {
        return value instanceof UISkin
               || value instanceof Boolean || value instanceof String || value instanceof Number
            ? JsonTreeValue.Type.KEY_VALUE_PAIR : JsonTreeValue.Type.OBJECT;
    }

    private Object getFieldValue(Field field, ClassMetadata metadata) throws IllegalAccessException {
        if (Binding.class.isAssignableFrom(field.getType())) {
            Binding binding = (Binding) field.get(metadata.newInstance());
            return binding.get();
        } else {
            return field.get(metadata.newInstance());
        }
    }
}
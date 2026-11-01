// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.editor.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.rendering.nui.contextMenu.MenuTree;
import org.terasology.nui.AbstractWidget;
import org.terasology.nui.Border;
import org.terasology.nui.Color;
import org.terasology.nui.LayoutConfig;
import org.terasology.nui.UILayout;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.UIWidget;
import org.terasology.nui.asset.font.Font;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.widgets.treeView.JsonTree;
import org.terasology.nui.widgets.treeView.JsonTreeValue;
import org.terasology.engine.rendering.nui.NUIManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A utility class to construct {@link MenuTree} instances.
 */
@SuppressWarnings("unchecked")
public class NUIEditorMenuTreeBuilder {
    // Context menu options.
    public static final String OPTION_ADD_EXTENDED = "Add...";
    public static final String OPTION_ADD_WIDGET = "Add Widget";
    public static final String OPTION_COPY = "Copy";
    public static final String OPTION_DELETE = "Delete";
    public static final String OPTION_EDIT = "Edit";
    public static final String OPTION_PASTE = "Paste";

    private Logger logger = LoggerFactory.getLogger(NUIEditorMenuTreeBuilder.class);

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
    private List<Consumer<JsonTree>> addContextMenuListeners = Lists.newArrayList();

    public void setManager(NUIManager manager) {
        this.nuiManager = manager;
    }

    public void putConsumer(String key, Consumer<JsonTree> value) {
        externalConsumers.put(key, value);
    }

    public void subscribeAddContextMenu(Consumer<JsonTree> listener) {
        addContextMenuListeners.add(listener);
    }

    public MenuTree createPrimaryContextMenu(JsonTree node) {
        MenuTree primaryTree = new MenuTree(null);

        JsonTreeValue.Type type = node.getValue().getType();

        // Create the ADD_EXTENDED level.
        if ((type == JsonTreeValue.Type.ARRAY && !"contents".equals(node.getValue().getKey()))
                || type == JsonTreeValue.Type.OBJECT) {
            MenuTree addTree = createAddContextMenu(node);
            primaryTree.addSubmenu(addTree);
        }

        // If the node is a "contents" array, add the widget addition option (redirects to WidgetSelectionScreen).
        if (type == JsonTreeValue.Type.ARRAY && "contents".equals(node.getValue().getKey())) {
            primaryTree.addOption(OPTION_ADD_WIDGET, externalConsumers.get(OPTION_ADD_WIDGET), node);
        }

        // Always add the copy&paste options.
        primaryTree.addOption(OPTION_COPY, externalConsumers.get(OPTION_COPY), node);
        primaryTree.addOption(OPTION_PASTE, externalConsumers.get(OPTION_PASTE), node);

        // Unless the node is an OBJECT child of an ARRAY (should always have an empty key), add the edit option.
        if (type != JsonTreeValue.Type.NULL && !(type == JsonTreeValue.Type.OBJECT
                && !node.isRoot()
                && node.getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
            primaryTree.addOption(OPTION_EDIT, externalConsumers.get(OPTION_EDIT), node);
        }

        if (!node.isRoot()) {
            primaryTree.addOption(OPTION_DELETE, externalConsumers.get(OPTION_DELETE), node);
        }

        return primaryTree;
    }

    public MenuTree createPrimarySkinContextMenu(JsonTree node) {
        MenuTree primaryTree = new MenuTree(null);

        JsonTreeValue.Type type = node.getValue().getType();

        // Create the ADD_EXTENDED level.
        if (type == JsonTreeValue.Type.ARRAY || (type == JsonTreeValue.Type.OBJECT
                && !"elements".equals(node.getValue().getKey()))) {
            MenuTree addTree = createAddSkinContextMenu(node);
            primaryTree.addSubmenu(addTree);
        }

        // If the node is an "elements" object, add the widget addition option (redirects to WidgetSelectionScreen).
        if (type == JsonTreeValue.Type.OBJECT && "elements".equals(node.getValue().getKey())) {
            primaryTree.addOption(OPTION_ADD_WIDGET, externalConsumers.get(OPTION_ADD_WIDGET), node);
        }

        // Always add the copy&paste and edit options.
        primaryTree.addOption(OPTION_COPY, externalConsumers.get(OPTION_COPY), node);
        primaryTree.addOption(OPTION_PASTE, externalConsumers.get(OPTION_PASTE), node);
        primaryTree.addOption(OPTION_EDIT, externalConsumers.get(OPTION_EDIT), node);

        return primaryTree;
    }

    public MenuTree createAddContextMenu(JsonTree node) {
        MenuTree addTree = new MenuTree(OPTION_ADD_EXTENDED);
        JsonTreeValue.Type type = node.getValue().getType();

        if (type == JsonTreeValue.Type.ARRAY) {
            // Add generic item addition options.
            addTree.addOption("Boolean value", n -> {
                JsonTree child = new JsonTree(new JsonTreeValue(null, false, JsonTreeValue.Type.VALUE));
                n.addChild(child);
                for (Consumer<JsonTree> listener : addContextMenuListeners) {
                    listener.accept(child);
                }
            }, node);
            addTree.addOption("Number value", n -> {
                JsonTree child = new JsonTree((new JsonTreeValue(null, 0.0f, JsonTreeValue.Type.VALUE)));
                n.addChild(child);
                for (Consumer<JsonTree> listener : addContextMenuListeners) {
                    listener.accept(n);
                }
            }, node);
            addTree.addOption("String value", n -> {
                JsonTree child = new JsonTree((new JsonTreeValue(null, "", JsonTreeValue.Type.VALUE)));
                n.addChild(child);
                for (Consumer<JsonTree> listener : addContextMenuListeners) {
                    listener.accept(child);
                }
            }, node);
        } else if (type == JsonTreeValue.Type.OBJECT) {
            // Add a generic key/value pair addition option.
            addTree.addOption("Key/value pair", n -> {
                JsonTree child = new JsonTree((new JsonTreeValue("", "", JsonTreeValue.Type.KEY_VALUE_PAIR)));
                n.addChild(child);
                for (Consumer<JsonTree> listener : addContextMenuListeners) {
                    listener.accept(child);
                }
            }, node);

            populateContextMenu(node, addTree, false);
        }

        return addTree;
    }

    public MenuTree createAddSkinContextMenu(JsonTree node) {
        MenuTree addTree = new MenuTree(OPTION_ADD_EXTENDED);
        JsonTreeValue.Type type = node.getValue().getType();

        if (type == JsonTreeValue.Type.OBJECT) {
            if ("families".equals(node.getValue().getKey())) {
                // Add an option to add a family for a "families" node.
                addTree.addOption("New family", n -> {
                    JsonTree child = new JsonTree(new JsonTreeValue("", null, JsonTreeValue.Type.OBJECT));
                    child.setExpanded(true);
                    n.addChild(child);
                    for (Consumer<JsonTree> listener : addContextMenuListeners) {
                        listener.accept(child);
                    }
                }, node);
            } else {
                addTree.addOption("Key/value pair", n -> {
                    JsonTree child = new JsonTree((new JsonTreeValue("", "", JsonTreeValue.Type.KEY_VALUE_PAIR)));
                    n.addChild(child);
                    for (Consumer<JsonTree> listener : addContextMenuListeners) {
                        listener.accept(child);
                    }
                }, node);

                populateContextMenu(node, addTree, true);
            }
        }

        return addTree;
    }

    private void populateContextMenu(JsonTree node, MenuTree addTree, boolean isSkin) {
        NUIEditorNodeUtils.NodeInfo nodeInfo;
        if (isSkin) {
            nodeInfo = NUIEditorNodeUtils.getSkinNodeInfo(node);
        } else {
            nodeInfo = NUIEditorNodeUtils.getNodeInfo(node, nuiManager);
        }

        if (nodeInfo != null) {
            Class clazz = nodeInfo.getNodeClass();
            if (clazz != null) {
                for (Field field : ReflectionUtils.getAllFields(clazz)) {
                    if ((!UIWidget.class.isAssignableFrom(clazz) || field.isAnnotationPresent(LayoutConfig.class))
                            // Exclude static final fields, as they shouldn't be modified.
                            && !(Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers()))) {
                        field.setAccessible(true);
                        String name = getNodeName(field);
                        if (!node.hasChildWithKey(name)) {
                            addTree.addOption(name, n -> {
                                try {
                                    JsonTree child = createChild(name, node, field, clazz);
                                    for (Consumer<JsonTree> listener : addContextMenuListeners) {
                                        listener.accept(child);
                                    }
                                } catch (IllegalAccessException | InstantiationException e) {
                                    logger.warn("Could not add child", e);
                                }
                            }, node);
                        }
                    }
                }
                // If the node is part of a layout, add an option to add the layoutInfo node.
                if (!node.isRoot() && !node.getParent().isRoot()) {
                    String layoutInfo = "layoutInfo";
                    String contents = "contents";
                    Class parentParentClass = NUIEditorNodeUtils
                            .getNodeInfo((JsonTree) node.getParent().getParent(), nuiManager)
                            .getNodeClass();
                    if (!node.hasChildWithKey(layoutInfo)
                            && UIWidget.class.isAssignableFrom(clazz)
                            && node.getParent().getValue() != null
                            && node.getParent().getValue().getKey() != null
                            && node.getParent().getValue().getKey().equals(contents)
                            && UILayout.class.isAssignableFrom(parentParentClass)) {
                        addTree.addOption(layoutInfo, n -> {
                            JsonTree child = new JsonTree(new JsonTreeValue(layoutInfo, null, JsonTreeValue.Type.OBJECT));
                            child.setExpanded(true);
                            n.addChild(child);
                        }, node);
                    }
                }
            }
        } else {
            logger.warn("Could not get class for node {}", node.getValue()); //NOPMD
        }
    }

    private JsonTree createChild(String name, JsonTree node, Field field, Class clazz)
            throws IllegalAccessException, InstantiationException {
        JsonTreeValue childValue = new JsonTreeValue();
        childValue.setKey(name);

        if (UISkin.class.isAssignableFrom(field.getType())) {
            // Skin fields should always be KEY_VALUE_PAIR nodes with string values.
            childValue.setValue("engine:default");
            childValue.setType(JsonTreeValue.Type.KEY_VALUE_PAIR);
        } else {
            if (isWidget(field)) {
                createWidgetChild(name, node);
                return null;
            } else {
                childValue.setValue(getFieldValue(field, clazz));
                childValue.setType(getNodeType(field, getFieldValue(field, clazz)));
            }
        }

        JsonTree child = new JsonTree(childValue);
        child.setExpanded(true);
        node.addChild(child);
        return child;
    }

    private boolean isWidget(Field field) throws IllegalAccessException {
        // The field is a Binding<? extends UIWidget>.
        if (Binding.class.isAssignableFrom(field.getType())
                && UIWidget.class.isAssignableFrom((Class<?>)
                ((ParameterizedType) (field.getGenericType())).getActualTypeArguments()[0])) {
            return true;
        }

        return UIWidget.class.isAssignableFrom(field.getType());
    }

    private void createWidgetChild(String name, JsonTree node) {
        JsonTree widgetTree = new JsonTree(new JsonTreeValue(name, null, JsonTreeValue.Type.OBJECT));
        NUIEditorNodeUtils
                .createNewWidget("UILabel", "newWidget", false)
                .getChildren()
                .forEach(widgetTree::addChild);
        widgetTree.addChild(new JsonTreeValue("text", "", JsonTreeValue.Type.KEY_VALUE_PAIR));
        node.addChild(widgetTree);
    }

    /**
     * @param field A class field.
     * @return The name of the field as it would appear in a JSON file.
     */
    private String getNodeName(Field field) {
        return field.isAnnotationPresent(SerializedName.class)
                ? field.getAnnotation(SerializedName.class).value() : field.getName();
    }

    /**
     * @param field A class field.
     * @param value The value of the field.
     * @return The type of a JSON node that would contain the field.
     */
    private JsonTreeValue.Type getNodeType(Field field, Object value) {
        return Enum.class.isAssignableFrom(field.getType()) || value instanceof UISkin
                || value instanceof Boolean || value instanceof String || value instanceof Number
                ? JsonTreeValue.Type.KEY_VALUE_PAIR : JsonTreeValue.Type.OBJECT;
    }

    private Object getFieldValue(Field field, Class clazz) throws IllegalAccessException, InstantiationException {
        if (Enum.class.isAssignableFrom(field.getType())) {
            return field.getType().getEnumConstants()[0].toString();
        } else {
            Object value;
            if (Binding.class.isAssignableFrom(field.getType())) {
                if (AbstractWidget.class.isAssignableFrom(clazz) && Objects.equals(field.getName(), "family")) {
                    // The default - and acceptable - value for the AbstractWidget.family binding is null, but a user
                    // most likely wants to use a custom family. Therefore, the default is set to an empty string instead.
                    value = "";
                } else {
                    Binding binding = (Binding) field.get(newInstance(clazz));
                    value = binding.get();
                }
            } else if (Optional.class.isAssignableFrom(field.getType())) {
                value = newInstance((Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
            } else {
                value = field.get(newInstance(clazz));
            }

            if (value instanceof Boolean) {
                value = !(Boolean) value;
            }

            return (value != null || Binding.class.isAssignableFrom(field.getType()))
                    ? value : newInstance(field.getType());
        }
    }

    private Object newInstance(Class clazz) throws IllegalAccessException, InstantiationException {
        if (Boolean.class.isAssignableFrom(clazz)) {
            return false;
        }
        if (Border.class.isAssignableFrom(clazz)) {
            return Border.ZERO;
        }
        if (Color.class.isAssignableFrom(clazz)) {
            return "000000FF";
        }
        if (Font.class.isAssignableFrom(clazz)) {
            return "NotoSans-Regular";
        }
        if (Number.class.isAssignableFrom(clazz)) {
            return 0;
        }
        if (String.class.isAssignableFrom(clazz)) {
            return "";
        }
        if (UITextureRegion.class.isAssignableFrom(clazz)) {
            return "";
        }
        return clazz.newInstance();
    }
}

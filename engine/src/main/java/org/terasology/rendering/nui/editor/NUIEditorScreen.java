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
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.Config;
import org.terasology.config.NUIEditorConfig;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.persistence.ModuleContext;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.asset.UIFormat;
import org.terasology.rendering.nui.contextMenu.ContextMenuBuilder;
import org.terasology.rendering.nui.contextMenu.ContextMenuLevel;
import org.terasology.rendering.nui.contextMenu.ContextMenuScreen;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.widgets.UIBox;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UITextEntry;
import org.terasology.rendering.nui.widgets.UITreeView;
import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeConverter;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeValue;
import org.terasology.rendering.nui.widgets.treeView.Tree;
import org.terasology.utilities.ReflectionUtil;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The main NUI editor screen.
 * Contains file selection, editing & preview widgets.
 */
public class NUIEditorScreen extends CoreScreenLayer {

    private Logger logger = LoggerFactory.getLogger(NUIEditorScreen.class);

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nuiEditorScreen");

    // Available asset dropdown option to create a new screen.
    private static final String CREATE_NEW_SCREEN = "New Screen";

    // Context menu level names.
    private static final String LEVEL_PRIMARY = "Primary";
    private static final String LEVEL_ADD_EXTENDED = "Add";

    // Context menu options.
    private static final String OPTION_COPY = "Copy";
    private static final String OPTION_PASTE = "Paste";
    private static final String OPTION_ADD_EXTENDED = "Add...";
    private static final String OPTION_ADD_WIDGET = "Add Widget";
    private static final String OPTION_EDIT = "Edit";

    @In
    private NUIEditorSystem nuiEditorSystem;

    @In
    private AssetManager assetManager;

    @In
    private Config config;

    /**
     * A list of available {@link UIElement} asset {@link ResourceUrn}s and an option to add a new screen.
     */
    private List<String> availableAssetList = Lists.newArrayList();
    /**
     * The dropdown containing a list of available asset {@link ResourceUrn}s.
     */
    private UIDropdownScrollable<String> availableAssetDropdown;
    /**
     * The currently selected UIElement.
     */
    private String selection;
    /**
     * A tree view containing a {@link JsonTree} representation of the asset being edited.
     */
    private UITreeView<JsonTreeValue> editorTreeView;
    /**
     * The {@link UIBox} containing the screen being edited.
     */
    private UIBox selectedScreenContainer;
    /**
     * A list of tree view model states (earliest first).
     */
    private List<JsonTree> editorHistory = Lists.newArrayList();
    /**
     * The index of the currently displayed tree view model state.
     */
    private int editorHistoryPosition;
    /**
     * The common widget used as an inline JSON property editor.
     */
    private UITextEntry<JsonTree> inlineEditorEntry;

    @Override
    public void initialise() {
        // Fetch the interface widgets.
        availableAssetDropdown = find("availableAssets", UIDropdownScrollable.class);
        editorTreeView = find("editor", UITreeView.class);
        selectedScreenContainer = find("selectedScreen", UIBox.class);

        // Populate the asset dropdown with the asset list.
        availableAssetList.add(CREATE_NEW_SCREEN);

        List<ResourceUrn> availableAssetUrns = assetManager
            .getAvailableAssets(UIElement.class)
            .stream()
            .collect(Collectors.toList());

        // Exclude screens used by the NUI editor, then sort the list.
        availableAssetUrns.removeIf(asset -> asset.getRootUrn().equals(ASSET_URI));
        availableAssetUrns.removeIf(asset -> asset.getRootUrn().equals(ContextMenuScreen.ASSET_URI));
        availableAssetUrns.removeIf(asset -> asset.getRootUrn().equals(NUIEditorSettingsScreen.ASSET_URI));
        availableAssetUrns.removeIf(asset -> asset.getRootUrn().equals(WidgetSelectionScreen.ASSET_URI));
        availableAssetUrns.sort(Comparator.comparing(ResourceUrn::toString));

        availableAssetList.addAll(availableAssetUrns.stream().map(ResourceUrn::toString).collect(Collectors.toList()));

        availableAssetDropdown.setOptions(availableAssetList);
        availableAssetDropdown.bindSelection(new Binding<String>() {
            @Override
            public String get() {
                return selection;
            }

            @Override
            public void set(String value) {
                if (value.equals(CREATE_NEW_SCREEN)) {
                    setTree(NUIEditorTemplateUtils.newTree());
                    selection = value;
                } else if (selection == null || !selection.toString().equals(value)) {
                    selectFile(new ResourceUrn(value));
                }
            }
        });

        // When the tree view is updated, update the widget accordingly.
        editorTreeView.subscribeTreeViewUpdate(() -> {
            JsonTree tree = (JsonTree) (editorTreeView.getModel().getNode(0).getRoot());
            if (editorHistoryPosition < editorHistory.size() - 1) {
                editorHistory = editorHistory.subList(0, editorHistoryPosition + 1);
            }
            editorHistory.add(tree);
            editorHistoryPosition++;

            updateWidget(tree);
            updateConfig();
        });

        // When the node is right-clicked, construct the context menu according to its' value.
        editorTreeView.subscribeNodeClick((event, node) -> {
            if (event.getMouseButton() == MouseInput.MOUSE_RIGHT) {
                editorTreeView.setSelectedIndex(editorTreeView.getModel().indexOf(node));
                editorTreeView.setAlternativeWidget(null);

                ContextMenuBuilder contextMenuBuilder = new ContextMenuBuilder();
                buildContextMenu((JsonTree) node, contextMenuBuilder);

                contextMenuBuilder.subscribeClose(() -> {
                    editorTreeView.setAlternativeWidget(null);
                    editorTreeView.setSelectedIndex(null);
                });

                contextMenuBuilder.subscribeScreenClosed(() -> {
                    if (inlineEditorEntry != null) {
                        focusInlineEditor((JsonTree) node);
                    }
                });

                contextMenuBuilder.show(getManager(), event.getMouse().getPosition());
            }
        });

        editorTreeView.subscribeNodeDoubleClick((event, node) -> {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                JsonTreeValue.Type type = ((JsonTree) node).getValue().getType();

                // Call the relevant edit handler on double click.
                if (type == JsonTreeValue.Type.KEY_VALUE_PAIR) {
                    editKeyValuePair((JsonTree) node);
                    focusInlineEditor((JsonTree) node);
                } else if (type == JsonTreeValue.Type.OBJECT &&
                           !(!node.isRoot() && ((JsonTree) node).getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
                    editObject((JsonTree) node);
                    focusInlineEditor((JsonTree) node);
                } else if (type == JsonTreeValue.Type.ARRAY) {
                    editArray((JsonTree) node);
                    focusInlineEditor((JsonTree) node);
                } else if (type == JsonTreeValue.Type.VALUE) {
                    editValue((JsonTree) node);
                    focusInlineEditor((JsonTree) node);
                }
            }
        });

        editorTreeView.subscribeKeyEvent(event -> {
            if (event.isDown() && event.getKey() == Keyboard.Key.F2) {
                Integer selectedIndex = editorTreeView.getSelectedIndex();
                if (selectedIndex != null) {
                    JsonTree node = (JsonTree) editorTreeView.getModel().getNode(selectedIndex);
                    JsonTreeValue.Type type = node.getValue().getType();

                    // Call the relevant edit handler on F2.
                    if (type == JsonTreeValue.Type.KEY_VALUE_PAIR) {
                        editKeyValuePair(node);
                        focusInlineEditor(node);
                    } else if (type == JsonTreeValue.Type.OBJECT &&
                               !(!node.isRoot() && node.getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
                        editObject(node);
                        focusInlineEditor(node);
                    } else if (type == JsonTreeValue.Type.ARRAY) {
                        editArray(node);
                        focusInlineEditor(node);
                    } else if (type == JsonTreeValue.Type.VALUE) {
                        editValue(node);
                        focusInlineEditor(node);
                    }
                }
            }
        });

        // Set the handlers for the editor buttons.
        WidgetUtil.trySubscribe(this, "settings", button -> getManager().pushScreen(NUIEditorSettingsScreen.ASSET_URI, NUIEditorSettingsScreen.class));
        WidgetUtil.trySubscribe(this, "copy", button -> copyJson());
        WidgetUtil.trySubscribe(this, "paste", button -> pasteJson());
        WidgetUtil.trySubscribe(this, "undo", button -> undo());
        WidgetUtil.trySubscribe(this, "redo", button -> redo());

        // Apply the config parameters.
        updateConfig();
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            int id = event.getKey().getId();
            KeyboardDevice keyboard = event.getKeyboard();
            boolean ctrlDown = keyboard.isKeyDown(Keyboard.KeyId.RIGHT_CTRL) || keyboard.isKeyDown(Keyboard.KeyId.LEFT_CTRL);

            if (id == Keyboard.KeyId.ESCAPE) {
                getAnimationSystem().stop();
                nuiEditorSystem.toggleEditor();
                return true;
            } else if (ctrlDown && id == Keyboard.KeyId.Z) {
                undo();
                return true;
            } else if (ctrlDown && id == Keyboard.KeyId.Y) {
                redo();
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean isEscapeToCloseAllowed() {
        // Escape to close is handled in onKeyEvent() to pass the editor's state to NUIEditorSystem.
        return false;
    }

    public void updateConfig() {
        NUIEditorConfig nuiEditorConfig = config.getNuiEditor();
        editorTreeView.setItemRenderer(nuiEditorConfig.isDisableIcons() ?
            new ToStringTextRenderer<>() : new NUIEditorItemRenderer(editorTreeView.getModel()));
    }

    /**
     * @param urn The Urn of the file to be edited.
     */
    public void selectFile(ResourceUrn urn) {
        Optional<UIElement> asset = assetManager.getAsset(urn, UIElement.class);
        if (asset.isPresent()) {
            UIElement element = asset.get();

            // Fetch the file from the asset Urn.
            AssetDataFile source = element.getSource();

            String content = null;
            try (JsonReader reader = new JsonReader(new InputStreamReader(source.openStream(), Charsets.UTF_8))) {
                reader.setLenient(true);
                content = new JsonParser().parse(reader).toString();
            } catch (IOException e) {
                logger.error(String.format("Could not load asset source file for %s", urn.toString()), e);
            }

            if (content != null) {
                // Serialize the JSON string into a JsonTree and expand its' nodes.

                JsonTree tree = JsonTreeConverter.serialize(new JsonParser().parse(content));
                setTree(tree);
                selection = urn.toString();
            }
        }
    }

    private void setTree(JsonTree tree) {
        updateTreeView(tree, true);
        updateWidget(tree);

        editorHistory.clear();
        editorHistory.add(tree);
        editorHistoryPosition = 0;

        if (selection != null) {
            // Dispose of the assets so that further calls use a new, initialised, instance.

            Optional<UIElement> selectedAsset = assetManager.getAsset(selection, UIElement.class);
            if (selectedAsset.isPresent()) {
                selectedAsset.get().dispose();
            }
        }

        updateConfig();
    }

    private ContextMenuLevel buildContextMenu(JsonTree node, ContextMenuBuilder contextMenuBuilder) {
        ContextMenuLevel primaryLevel = contextMenuBuilder.addLevel(LEVEL_PRIMARY);
        primaryLevel.setVisible(true);

        primaryLevel.addOption(OPTION_COPY, this::copy, node, true);
        primaryLevel.addOption(OPTION_PASTE, this::paste, node, true);

        JsonTreeValue.Type type = node.getValue().getType();

        if (type == JsonTreeValue.Type.ARRAY || type == JsonTreeValue.Type.OBJECT) {
            ContextMenuLevel addLevel = contextMenuBuilder.addLevel(LEVEL_ADD_EXTENDED);

            primaryLevel.addOption(OPTION_ADD_EXTENDED, jsonTree -> {
                contextMenuBuilder.getLevel(LEVEL_ADD_EXTENDED).setVisible(!addLevel.isVisible());
            }, node, false);

            if (type == JsonTreeValue.Type.ARRAY) {
                addLevel.addOption("Boolean attribute", jsonTree -> {
                    jsonTree.addChild(new JsonTreeValue(null, false, JsonTreeValue.Type.VALUE));
                    editorTreeView.fireUpdateListeners();
                    updateTreeView((JsonTree) jsonTree.getRoot(), false);
                }, node, true);
                addLevel.addOption("Number attribute", jsonTree -> {
                    jsonTree.addChild(new JsonTreeValue(null, 0.0f, JsonTreeValue.Type.VALUE));
                    editorTreeView.fireUpdateListeners();
                    updateTreeView((JsonTree) jsonTree.getRoot(), false);
                }, node, true);
                addLevel.addOption("String attribute", jsonTree -> {
                    jsonTree.addChild(new JsonTreeValue(null, "", JsonTreeValue.Type.VALUE));
                    editorTreeView.fireUpdateListeners();
                    updateTreeView((JsonTree) jsonTree.getRoot(), false);
                }, node, true);
            } else {
                addLevel.addOption("Key/value pair", jsonTree -> {
                    jsonTree.addChild(new JsonTreeValue("", "", JsonTreeValue.Type.KEY_VALUE_PAIR));
                    editorTreeView.fireUpdateListeners();
                    updateTreeView((JsonTree) jsonTree.getRoot(), false);
                }, node, true);
                if (node.hasChildWithKey("type")) {
                    populateWidgetContextMenu(node, contextMenuBuilder);
                } else if (node.getValue().getKey().equalsIgnoreCase("layoutInfo")) {
                    populateLayoutContextMenu(node, contextMenuBuilder);
                } else {
                    populateGenericContextMenu(node, contextMenuBuilder);
                }
            }
        }

        // "Add Widget" only allowed for "contents" ARRAY nodes.
        if (type == JsonTreeValue.Type.ARRAY && node.getValue().getKey().equals("contents")) {
            primaryLevel.addOption(OPTION_ADD_WIDGET, this::addWidget, node, true);
        }

        // "Edit" node handlers vary depending on the node's type.
        if (type == JsonTreeValue.Type.KEY_VALUE_PAIR) {
            primaryLevel.addOption(OPTION_EDIT, this::editKeyValuePair, node, true);
        } else if (type == JsonTreeValue.Type.OBJECT &&
                   !(!node.isRoot() && node.getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
            primaryLevel.addOption(OPTION_EDIT, this::editObject, node, true);
        } else if (type == JsonTreeValue.Type.ARRAY) {
            primaryLevel.addOption(OPTION_EDIT, this::editArray, node, true);
        } else if (type == JsonTreeValue.Type.VALUE) {
            primaryLevel.addOption(OPTION_EDIT, this::editValue, node, true);
        }

        return primaryLevel;
    }

    private void copy(JsonTree node) {
        editorTreeView.copy(node);
        editorTreeView.setSelectedIndex(null);
    }

    private void paste(JsonTree node) {
        editorTreeView.paste(node);
        editorTreeView.setSelectedIndex(null);
    }

    private void addWidget(JsonTree node) {
        getManager().pushScreen(WidgetSelectionScreen.ASSET_URI, WidgetSelectionScreen.class);

        // Push and configure WidgetSelectionScreen.
        WidgetSelectionScreen widgetSelectionScreen = (WidgetSelectionScreen) getManager().getScreen(WidgetSelectionScreen.ASSET_URI);
        widgetSelectionScreen.setNode(node);
        widgetSelectionScreen.subscribeClose(() -> {
            editorTreeView.fireUpdateListeners();
            updateTreeView((JsonTree) node.getRoot(), false);
        });
    }

    private void populateWidgetContextMenu(JsonTree node, ContextMenuBuilder contextMenuBuilder) {
        ContextMenuLevel addLevel = contextMenuBuilder.addLevel(LEVEL_ADD_EXTENDED);
        String type = node.getChildWithKey("type").getValue().getValue().toString();
        ClassMetadata<? extends UIWidget, ?> elementMetadata =
            getManager().getWidgetMetadataLibrary().resolve(type, ModuleContext.getContext());
        if (elementMetadata != null) {
            for (FieldMetadata field : elementMetadata.getFields()) {
                Field internalField = field.getField();
                String name;
                if (internalField.isAnnotationPresent(SerializedName.class)) {
                    name = internalField.getAnnotation(SerializedName.class).value();
                } else {
                    name = internalField.getName();
                }
                if (internalField.isAnnotationPresent(LayoutConfig.class)
                    && !node.hasChildWithKey(internalField.getName())) {
                    addLevel.addOption(name, jsonTree -> {
                        if (List.class.isAssignableFrom(field.getType())) {
                            jsonTree.addChild(new JsonTreeValue(name, null, JsonTreeValue.Type.ARRAY));
                        } else {
                            internalField.setAccessible(true);
                            try {
                                jsonTree.addChild(new JsonTreeValue(name,
                                    internalField.get(elementMetadata.newInstance()), JsonTreeValue.Type.KEY_VALUE_PAIR));
                                editorTreeView.fireUpdateListeners();
                                updateTreeView((JsonTree) jsonTree.getRoot(), false);
                            } catch (IllegalAccessException e) {
                                // Should not be triggered as the setAccessible flag is set to true.
                            }
                        }
                    }, node, true);
                }
            }
        } else {
            logger.warn("Unknown UIWidget type {}", type);
        }
    }

    private void populateLayoutContextMenu(JsonTree node, ContextMenuBuilder contextMenuBuilder) {
        ContextMenuLevel addLevel = contextMenuBuilder.addLevel(LEVEL_ADD_EXTENDED);
        String type = node.getSiblingWithKey("type").getValue().getKey();
        ClassMetadata<? extends UIWidget, ?> elementMetadata =
            getManager().getWidgetMetadataLibrary().resolve(type, ModuleContext.getContext());

        Class<? extends LayoutHint> layoutHintType;
        try {
            layoutHintType = (Class<? extends LayoutHint>)
                ReflectionUtil.getTypeParameter(elementMetadata.getType().getGenericSuperclass(), 0);
        } catch (NullPointerException e) {
            return;
        }

        if (layoutHintType != null) {
            for (Field field : layoutHintType.getDeclaredFields()) {
                String name;
                if (field.isAnnotationPresent(SerializedName.class)) {
                    name = field.getAnnotation(SerializedName.class).value();
                } else {
                    name = field.getName();
                }
                if (!node.hasChildWithKey(name)) {
                    addLevel.addOption(name, jsonTree -> {
                        try {
                            jsonTree.addChild(new JsonTreeValue(name,
                                field.get(elementMetadata.newInstance()), JsonTreeValue.Type.KEY_VALUE_PAIR));
                            editorTreeView.fireUpdateListeners();
                            updateTreeView((JsonTree) jsonTree.getRoot(), false);
                        } catch (IllegalAccessException e) {
                            // Should not be triggered as the setAccessible flag is set to true.
                        }
                    }, node, true);
                }
            }
        } else {
            logger.warn("Unknown layout type {}", type);
        }
    }

    private void populateGenericContextMenu(JsonTree node, ContextMenuBuilder contextMenuBuilder) {
        ContextMenuLevel addLevel = contextMenuBuilder.addLevel(LEVEL_ADD_EXTENDED);

        if (node.hasSiblingWithKey("type")) {
            String type = node.getSiblingWithKey("type").getValue().getKey();
            ClassMetadata<? extends UIWidget, ?> elementMetadata =
                getManager().getWidgetMetadataLibrary().resolve(type, ModuleContext.getContext());
            Class clazz = elementMetadata.getField(node.getValue().getKey()).getType();

            for (Field field : clazz.getDeclaredFields()) {
                String name;
                if (field.isAnnotationPresent(SerializedName.class)) {
                    name = field.getAnnotation(SerializedName.class).value();
                } else {
                    name = field.getName();
                }
                if (!node.hasChildWithKey(name)) {
                    addLevel.addOption(name, jsonTree -> {
                        jsonTree.addChild(new JsonTreeValue(name, "", JsonTreeValue.Type.KEY_VALUE_PAIR));
                        editorTreeView.fireUpdateListeners();
                        updateTreeView((JsonTree) jsonTree.getRoot(), false);
                    }, node, true);
                }
            }
        }
    }

    private void edit(JsonTree node, UITextEntry.Formatter<JsonTree> formatter, UITextEntry.Parser<JsonTree> parser) {
        inlineEditorEntry = new UITextEntry<>();
        inlineEditorEntry.bindValue(new Binding<JsonTree>() {
            @Override
            public JsonTree get() {
                return node;
            }

            @Override
            public void set(JsonTree value) {
                if (value != null) {
                    node.setValue(value.getValue());
                    editorTreeView.fireUpdateListeners();
                    updateTreeView((JsonTree) node.getRoot(), false);
                    updateWidget((JsonTree) node.getRoot());
                }
            }
        });
        inlineEditorEntry.setFormatter(formatter);
        inlineEditorEntry.setParser(parser);
        inlineEditorEntry.subscribe(widget -> {
            // Required so that ENTER/NUMPAD_ENTER saves the changes.
            inlineEditorEntry.onLoseFocus();
        });
        editorTreeView.setAlternativeWidget(inlineEditorEntry);
    }

    private void editKeyValuePair(JsonTree node) {
        UITextEntry.Formatter<JsonTree> formatter = value -> {
            JsonObject jsonObject = new JsonObject();

            String jsonKey = value.getValue().getKey();
            Object jsonValue = value.getValue().getValue();

            if (jsonValue instanceof Boolean) {
                jsonObject.addProperty(jsonKey, (Boolean) jsonValue);
            } else if (jsonValue instanceof Number) {
                jsonObject.addProperty(jsonKey, (Number) jsonValue);
            } else if (jsonValue instanceof String) {
                jsonObject.addProperty(jsonKey, (String) jsonValue);
            } else {
                jsonObject.addProperty(jsonKey, (Character) jsonValue);
            }

            String jsonString = new Gson().toJson(jsonObject);
            return jsonString.substring(1, jsonString.length() - 1);
        };

        UITextEntry.Parser<JsonTree> parser = value -> {
            String jsonString = String.format("{%s}", value);
            try {
                JsonElement jsonElement = new JsonParser().parse(jsonString);
                Map.Entry keyValuePair = jsonElement.getAsJsonObject().entrySet().iterator().next();

                String jsonKey = (String) keyValuePair.getKey();
                JsonTreeValue parsedNode;
                if (keyValuePair.getValue() == null) {
                    parsedNode = new JsonTreeValue(jsonKey, null, JsonTreeValue.Type.KEY_VALUE_PAIR);
                } else {
                    JsonPrimitive jsonValue = (JsonPrimitive) keyValuePair.getValue();
                    if (jsonValue.isBoolean()) {
                        parsedNode = new JsonTreeValue(jsonKey, jsonValue.getAsBoolean(), JsonTreeValue.Type.KEY_VALUE_PAIR);
                    } else if (jsonValue.isNumber()) {
                        parsedNode = new JsonTreeValue(jsonKey, jsonValue.getAsNumber(), JsonTreeValue.Type.KEY_VALUE_PAIR);
                    } else {
                        parsedNode = new JsonTreeValue(jsonKey, jsonValue.getAsString(), JsonTreeValue.Type.KEY_VALUE_PAIR);
                    }
                }
                return new JsonTree(parsedNode);
            } catch (JsonSyntaxException e) {
                return null;
            }
        };

        edit(node, formatter, parser);
    }

    private void editObject(JsonTree node) {
        UITextEntry.Formatter<JsonTree> formatter = value -> value.getValue().getKey();

        UITextEntry.Parser<JsonTree> parser = value -> new JsonTree(new JsonTreeValue(value, null, JsonTreeValue.Type.OBJECT));

        edit(node, formatter, parser);
    }

    private void editArray(JsonTree node) {
        UITextEntry.Formatter<JsonTree> formatter = value -> value.getValue().getKey();

        UITextEntry.Parser<JsonTree> parser = value -> new JsonTree(new JsonTreeValue(value, null, JsonTreeValue.Type.ARRAY));

        edit(node, formatter, parser);
    }

    private void editValue(JsonTree node) {
        UITextEntry.Formatter<JsonTree> formatter = value -> value.getValue().toString();

        UITextEntry.Parser<JsonTree> parser = value -> {
            try {
                Double valueDouble = Double.parseDouble(value);
                return new JsonTree(new JsonTreeValue(null, valueDouble, JsonTreeValue.Type.VALUE));
            } catch (NumberFormatException e) {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    return new JsonTree(new JsonTreeValue(null, Boolean.parseBoolean(value), JsonTreeValue.Type.VALUE));
                } else {
                    return new JsonTree(new JsonTreeValue(null, value, JsonTreeValue.Type.VALUE));
                }
            }
        };

        edit(node, formatter, parser);
    }

    private void focusInlineEditor(JsonTree node) {
        getManager().setFocus(inlineEditorEntry);
        inlineEditorEntry.resetValue();

        if (node.getValue().getType() == JsonTreeValue.Type.KEY_VALUE_PAIR) {
            // Select the value string only. Account for the key quotes + colon.
            if (node.getValue().getValue() instanceof String) {
                inlineEditorEntry.setCursorPosition(node.getValue().getKey().length() + "\"\":\"".length(), true);
                inlineEditorEntry.setCursorPosition(inlineEditorEntry.getText().length() - "\"".length(), false);
            } else {
                inlineEditorEntry.setCursorPosition(node.getValue().getKey().length() + "\"\":".length(), true);
                inlineEditorEntry.setCursorPosition(inlineEditorEntry.getText().length(), false);
            }
        } else {
            // Select the entire editor string.
            inlineEditorEntry.setCursorPosition(0, true);
            inlineEditorEntry.setCursorPosition(inlineEditorEntry.getText().length(), false);
        }
    }

    private void copyJson() {
        if (editorTreeView.getModel() != null) {
            // Deserialize the tree view's internal JsonTree into the original JSON string.
            JsonElement json = JsonTreeConverter.deserialize(editorTreeView.getModel().getNode(0).getRoot());
            String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);

            // Set the clipboard contents to the JSON string.
            // ClipboardManager not used here to make the editor accessible within the main menu.
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                clipboard.setContents(new StringSelection(jsonString), null);
            } catch (IllegalStateException e) {
                logger.warn("Clipboard inaccessible.", e);
            }
        }
    }

    private void pasteJson() {
        // Fetch the clipboard contents.
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable t = clipboard.getContents(null);

        String clipboardContents = null;
        try {
            if (t != null) {
                clipboardContents = (String) t.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            logger.warn("Could not fetch clipboard contents.", e);
        }

        if (clipboardContents != null) {
            try {
                // If the clipboard contents are a valid JSON string, serialize them into a JsonTree.
                JsonElement json = new JsonParser().parse(clipboardContents);
                JsonTree tree = JsonTreeConverter.serialize(json);
                updateTreeView(tree, true);
                updateWidget(tree);
            } catch (JsonSyntaxException | NullPointerException e) {
                logger.warn("Could not construct a valid tree from clipboard contents.", e);
            }
        }
    }

    private void undo() {
        if (editorHistoryPosition > 0) {
            editorHistoryPosition--;
            JsonTree tree = editorHistory.get(editorHistoryPosition);
            updateTreeView(tree, false);
            updateWidget(tree);
        }
    }

    private void redo() {
        if (editorHistoryPosition < editorHistory.size() - 1) {
            editorHistoryPosition++;
            JsonTree tree = editorHistory.get(editorHistoryPosition);
            updateTreeView(tree, false);
            updateWidget(tree);
        }
    }

    private void updateTreeView(JsonTree tree, boolean expandNodes) {
        if (expandNodes) {
            // Expand all the tree nodes. Used for deserialized trees (from asset, clipboard etc.)
            Iterator it = tree.getDepthFirstIterator(false);
            if (it.hasNext()) {
                expandTree(tree);
            }
        }

        editorTreeView.setModel(tree.copy());
        updateConfig();
    }

    private void expandTree(JsonTree tree) {
        // Do not expand OBJECT children of ARRAY parents (generally this concerns widget lists).
        if (!(tree.getValue().getType() == JsonTreeValue.Type.OBJECT
              && !tree.isRoot() && tree.getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
            tree.setExpanded(true);
        }

        for (Tree node : tree.getChildren()) {
            expandTree((JsonTree) node);
        }
    }

    private void updateWidget(JsonTree tree) {
        UIWidget widget;
        try {
            JsonElement element = JsonTreeConverter.deserialize(tree);
            widget = new UIFormat().load(element).getRootWidget();
            selectedScreenContainer.setContent(widget);
        } catch (Exception e) {
            selectedScreenContainer.setContent(new UILabel(ExceptionUtils.getStackTrace(e)));
        }
    }
}

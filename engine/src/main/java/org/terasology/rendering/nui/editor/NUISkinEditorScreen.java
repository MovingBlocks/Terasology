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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.Config;
import org.terasology.config.NUIEditorConfig;
import org.terasology.input.Keyboard;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.contextMenu.ContextMenuBuilder;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.widgets.JsonEditorTreeView;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UITextEntry;
import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeConverter;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeValue;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class NUISkinEditorScreen extends CoreScreenLayer {

    private Logger logger = LoggerFactory.getLogger(NUIEditorScreen.class);

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nuiSkinEditorScreen");

    private static final String AVAILABLE_ASSETS_ID = "availableAssets";
    private static final String EDITOR_TREE_VIEW_ID = "editor";
    private static final String CREATE_NEW_SKIN = "New Skin";

    /**
     * Used to retrieve {@link UISkin} assets.
     */
    @In
    private AssetManager assetManager;
    /**
     * Used to read from and write to {@link NUIEditorConfig}
     */
    @In
    private Config config;
    /**
     * Used to toggle the editor screen on ESCAPE.
     */
    @In
    private NUISkinEditorSystem nuiSkinEditorSystem;

    /**
     * The main editor widget used to display and edit NUI skins.
     */
    private JsonEditorTreeView editor;
    /**
     * The Urn of the currently edited asset.
     */
    private String selectedAsset;
    /**
     * The widget used as an inline node editor.
     */
    private UITextEntry<JsonTree> inlineEditorEntry;

    @Override
    public void initialise() {
        UIDropdownScrollable<String> availableAssetDropdown = find(AVAILABLE_ASSETS_ID, UIDropdownScrollable.class);
        editor = find(EDITOR_TREE_VIEW_ID, JsonEditorTreeView.class);

        // Populate the list of screens.
        List<String> availableAssetList = Lists.newArrayList();
        availableAssetList.add(CREATE_NEW_SKIN);
        availableAssetList.addAll(assetManager.getAvailableAssets(UISkin.class).stream().map(Object::toString).collect(Collectors.toList()));

        Collections.sort(availableAssetList);

        if (availableAssetDropdown != null) {
            availableAssetDropdown.setOptions(availableAssetList);
            availableAssetDropdown.bindSelection(new Binding<String>() {
                @Override
                public String get() {
                    return selectedAsset;
                }

                @Override
                public void set(String value) {
                    if (value.equals(CREATE_NEW_SKIN)) {
                        resetState(NUIEditorNodeUtils.createNewSkin());
                        selectedAsset = value;
                    } else {
                        selectAsset(new ResourceUrn(value));
                    }
                }
            });
        }

        // TODO: skin editor-specific context menu
        editor.setContextMenuProducer(node -> new ContextMenuBuilder());

        editor.setEditor(this::editNode, getManager());

        editor.subscribeTreeViewUpdate(() -> {
            editor.addToHistory();
            updateConfig();
        });

        // Set the handlers for the editor buttons.
        WidgetUtil.trySubscribe(this, "settings", button ->
            getManager().pushScreen(NUIEditorSettingsScreen.ASSET_URI, NUIEditorSettingsScreen.class));
        WidgetUtil.trySubscribe(this, "copy", button -> copyJson());
        WidgetUtil.trySubscribe(this, "paste", button -> pasteJson());
        WidgetUtil.trySubscribe(this, "undo", button -> {
            if (editor.undo()) {
                updateConfig();
            }
        });
        WidgetUtil.trySubscribe(this, "redo", button -> {
            if (editor.redo()) {
                updateConfig();
            }
        });

        updateConfig();
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            int id = event.getKey().getId();
            KeyboardDevice keyboard = event.getKeyboard();
            boolean ctrlDown = keyboard.isKeyDown(Keyboard.KeyId.RIGHT_CTRL) || keyboard.isKeyDown(Keyboard.KeyId.LEFT_CTRL);

            if (id == Keyboard.KeyId.ESCAPE) {
                nuiSkinEditorSystem.toggleEditor();
                return true;
            } else if (ctrlDown && id == Keyboard.KeyId.Z) {
                if (editor.undo()) {
                    updateConfig();
                }
                return true;
            } else if (ctrlDown && id == Keyboard.KeyId.Y) {
                if (editor.redo()) {
                    updateConfig();
                }
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

    /**
     * Resets the editor's state to a tree representation of a specified {@link UISkin}.
     *
     * @param urn The Urn of the UI asset.
     */
    public void selectAsset(ResourceUrn urn) {
        Optional<UISkin> asset = assetManager.getAsset(urn, UISkin.class);
        if (asset.isPresent()) {
            UISkin skin = asset.get();

            AssetDataFile source = skin.getSource();

            String content = null;
            try (JsonReader reader = new JsonReader(new InputStreamReader(source.openStream(), Charsets.UTF_8))) {
                reader.setLenient(true);
                content = new JsonParser().parse(reader).toString();
            } catch (IOException e) {
                logger.error(String.format("Could not load asset source file for %s", urn.toString()), e);
            }

            if (content != null) {
                JsonTree node = JsonTreeConverter.serialize(new JsonParser().parse(content));
                resetState(node);
                selectedAsset = urn.toString();
            }
        }
    }

    /**
     * Updates the editor's state based on config changes.
     * <p>
     * Should also be called to update the {@link NUIEditorItemRenderer} when the model is changed.
     */
    public void updateConfig() {
        NUIEditorConfig nuiEditorConfig = config.getNuiEditor();
        editor.setItemRenderer(nuiEditorConfig.isDisableIcons()
            ? new ToStringTextRenderer<>() : new NUIEditorItemRenderer(editor.getModel()));
    }

    /**
     * Creates an inline editor widget for the specified node, activates and focuses it.
     *
     * @param node The node an editor widget is to be created for.
     */
    private void editNode(JsonTree node) {
        JsonTreeValue.Type type = node.getValue().getType();

        // Create the inline editor depending on the node's type.
        inlineEditorEntry = null;
        if (type == JsonTreeValue.Type.VALUE) {
            inlineEditorEntry = NUIEditorTextEntryBuilder.createValueEditor();
        } else if (type == JsonTreeValue.Type.KEY_VALUE_PAIR) {
            inlineEditorEntry = NUIEditorTextEntryBuilder.createKeyValueEditor();
        } else if (type == JsonTreeValue.Type.OBJECT &&
                   !(!node.isRoot() && node.getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
            inlineEditorEntry = NUIEditorTextEntryBuilder.createObjectEditor();
        } else if (type == JsonTreeValue.Type.ARRAY) {
            inlineEditorEntry = NUIEditorTextEntryBuilder.createArrayEditor();
        }

        if (inlineEditorEntry != null) {
            inlineEditorEntry.bindValue(new Binding<JsonTree>() {
                @Override
                public JsonTree get() {
                    return node;
                }

                @Override
                public void set(JsonTree value) {
                    if (value != null) {
                        node.setValue(value.getValue());
                        editor.fireUpdateListeners();
                    }
                }
            });
            editor.setAlternativeWidget(inlineEditorEntry);
            focusInlineEditor(node);
        }
    }

    /**
     * Sets the {@link NUIManager}'s focus to the inline editor widget and select a subset of its' contents.
     *
     * @param node The node that is currently being edited.
     */
    private void focusInlineEditor(JsonTree node) {
        getManager().setFocus(inlineEditorEntry);
        inlineEditorEntry.resetValue();

        if (node.getValue().getType() == JsonTreeValue.Type.KEY_VALUE_PAIR) {
            // If the node is a key/value pair, select the value of the node.
            if (node.getValue().getValue() instanceof String) {
                inlineEditorEntry.setCursorPosition(node.getValue().getKey().length() + "\"\":\"".length(), true);
                inlineEditorEntry.setCursorPosition(inlineEditorEntry.getText().length() - "\"".length(), false);
            } else {
                inlineEditorEntry.setCursorPosition(node.getValue().getKey().length() + "\"\":".length(), true);
                inlineEditorEntry.setCursorPosition(inlineEditorEntry.getText().length(), false);
            }
        } else {
            // Otherwise fully select the contents of the node.
            inlineEditorEntry.setCursorPosition(0, true);
            inlineEditorEntry.setCursorPosition(inlineEditorEntry.getText().length(), false);
        }
    }

    /**
     * Fully resets the editor state and updates it from a specified {@link JsonTree}.
     *
     * @param node The node the editor's state is to be reset to.
     */
    private void resetState(JsonTree node) {
        editor.setTreeViewModel(node, true);

        editor.clearHistory();

        // Dispose of the previously loaded asset so that any other copies of it
        // are properly initialized.
        if (selectedAsset != null) {
            Optional<UIElement> asset = assetManager.getAsset(selectedAsset, UIElement.class);
            if (asset.isPresent()) {
                asset.get().dispose();
            }
        }

        updateConfig();
    }

    /**
     * Copies the current state of the editor to the system clipboard as a JSON string.
     * <p>
     * {@link ClipboardManager} is not used here as it is unavailable within the main menu.
     */
    private void copyJson() {
        if (editor.getModel() != null) {
            // Deserialize the state of the editor to a JSON string.
            JsonElement json = JsonTreeConverter.deserialize(editor.getModel().getNode(0).getRoot());
            String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);

            // Set the contents of the system clipboard to it.
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                clipboard.setContents(new StringSelection(jsonString), null);
            } catch (IllegalStateException e) {
                logger.warn("Clipboard inaccessible.", e);
            }
        }
    }

    /**
     * Attempts to serialize the system clipboard's contents to a JSON string -
     * if successful, sets the current state of the editor to it.
     * <p>
     * {@link ClipboardManager} is not used here as it is unavailable within the main menu.
     */
    private void pasteJson() {
        // Get the clipboard contents.
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable t = clipboard.getContents(null);

        // Attempt to convert them to a string.
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
                // Attempt to serialize them to a JsonTree and reset the editor state.
                JsonElement json = new JsonParser().parse(clipboardContents);
                JsonTree node = JsonTreeConverter.serialize(json);
                resetState(node);
            } catch (JsonSyntaxException | NullPointerException e) {
                logger.warn("Could not construct a valid tree from clipboard contents.", e);
            }
        }
    }
}

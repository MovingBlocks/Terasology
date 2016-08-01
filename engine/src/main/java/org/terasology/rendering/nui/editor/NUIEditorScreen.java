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
import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.asset.UIFormat;
import org.terasology.rendering.nui.contextMenu.ContextMenuBuilder;
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

/**
 * The main NUI editor screen.
 * Contains file selection, editing & preview widgets.
 */
@SuppressWarnings("unchecked")
public class NUIEditorScreen extends CoreScreenLayer {

    private Logger logger = LoggerFactory.getLogger(NUIEditorScreen.class);

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nuiEditorScreen");

    // Editor widget identifiers.
    private static final String AVAILABLE_ASSETS_ID = "availableAssets";
    private static final String EDITOR_TREE_VIEW_ID = "editor";
    private static final String SELECTED_SCREEN_ID = "selectedScreen";

    private static final String CREATE_NEW_SCREEN = "New Screen";

    /**
     * Used to retrieve & dispose of {@link UIElement} assets.
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
    private NUIEditorSystem nuiEditorSystem;

    /**
     * The main editor widget used to display and edit NUI screens.
     */
    private UITreeView editor;
    /**
     * The box used to preview a NUI screen modified by the editor.
     */
    private UIBox selectedScreenBox;
    /**
     * The Urn of the currently edited asset.
     */
    private String selectedAsset;
    /**
     * The list of the editor's states.
     */
    private List<JsonTree> history = Lists.newArrayList();
    /**
     * The current position in the list of the editor's states.
     */
    private int historyPosition;
    /**
     * The widget used as an inline node editor.
     */
    private UITextEntry<JsonTree> inlineEditorEntry;

    @Override
    public void initialise() {
        // Retrieve the widgets based on their identifiers.
        UIDropdownScrollable<String> availableAssetDropdown = find(AVAILABLE_ASSETS_ID, UIDropdownScrollable.class);
        editor = find(EDITOR_TREE_VIEW_ID, UITreeView.class);
        selectedScreenBox = find(SELECTED_SCREEN_ID, UIBox.class);

        // Populate the list of screens.
        List<String> availableAssetList = Lists.newArrayList();
        availableAssetList.add(CREATE_NEW_SCREEN);
        availableAssetList.addAll(assetManager.getAvailableAssets(UIElement.class).stream().map(Object::toString).collect(Collectors.toList()));

        // Remove the screens used by the editor to prevent initialization issues.
        availableAssetList.removeIf(asset -> asset.equals(ASSET_URI.toString()));
        availableAssetList.removeIf(asset -> asset.equals(ContextMenuScreen.ASSET_URI.toString()));
        availableAssetList.removeIf(asset -> asset.equals(NUIEditorSettingsScreen.ASSET_URI.toString()));
        availableAssetList.removeIf(asset -> asset.equals(WidgetSelectionScreen.ASSET_URI.toString()));
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
                    if (value.equals(CREATE_NEW_SCREEN)) {
                        resetState(NUIEditorNodeUtils.createNewScreen());
                        selectedAsset = value;
                    } else {
                        selectAsset(new ResourceUrn(value));
                    }
                }
            });
        }

        editor.subscribeTreeViewUpdate(() -> {
            JsonTree rootNode = (JsonTree) editor.getModel().getNode(0).getRoot();
            addToHistory(rootNode);
            setPreviewWidget(rootNode);
            updateConfig();
        });

        // Create and display a context menu on RMB.
        editor.subscribeNodeClick((event, node) -> {
            if (event.getMouseButton() == MouseInput.MOUSE_RIGHT) {
                editor.setSelectedIndex(editor.getModel().indexOf(node));
                editor.setAlternativeWidget(null);

                NUIEditorContextMenuBuilder contextMenuBuilder = new NUIEditorContextMenuBuilder();
                contextMenuBuilder.setManager(getManager());
                contextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_COPY, this::copyNode);
                contextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_PASTE, this::pasteNode);
                contextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_EDIT, this::editNode);
                contextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_ADD_WIDGET, this::addWidget);
                contextMenuBuilder.subscribeAddContextMenu(() -> editor.fireUpdateListeners());
                ContextMenuBuilder contextMenu = contextMenuBuilder.createPrimaryContextMenu((JsonTree) node);

                contextMenu.subscribeClose(() -> {
                    editor.setAlternativeWidget(null);
                    editor.setSelectedIndex(null);
                });

                contextMenu.subscribeScreenClosed(() -> {
                    if (editor.getAlternativeWidget() != null) {
                        focusInlineEditor((JsonTree) node);
                    }
                });

                contextMenu.show(getManager(), event.getMouse().getPosition());
            }
        });

        // Edit a node on double click.
        editor.subscribeNodeDoubleClick((event, node) -> {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                editNode((JsonTree) node);
            }
        });

        // Edit the currently selected node on F2.
        editor.subscribeKeyEvent(event -> {
            if (event.isDown() && event.getKey() == Keyboard.Key.F2) {
                Integer selectedIndex = editor.getSelectedIndex();

                if (selectedIndex != null) {
                    editNode((JsonTree) editor.getModel().getNode(selectedIndex));
                }
            }
        });

        // Set the handlers for the editor buttons.
        WidgetUtil.trySubscribe(this, "settings", button ->
            getManager().pushScreen(NUIEditorSettingsScreen.ASSET_URI, NUIEditorSettingsScreen.class));
        WidgetUtil.trySubscribe(this, "copy", button -> copyJson());
        WidgetUtil.trySubscribe(this, "paste", button -> pasteJson());
        WidgetUtil.trySubscribe(this, "undo", button -> undo());
        WidgetUtil.trySubscribe(this, "redo", button -> redo());

        updateConfig();
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            int id = event.getKey().getId();
            KeyboardDevice keyboard = event.getKeyboard();
            boolean ctrlDown = keyboard.isKeyDown(Keyboard.KeyId.RIGHT_CTRL) || keyboard.isKeyDown(Keyboard.KeyId.LEFT_CTRL);

            if (id == Keyboard.KeyId.ESCAPE) {
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

    /**
     * Resets the editor's state to a tree representation of a specified {@link UIElement}.
     *
     * @param urn The Urn of the UI asset.
     */
    public void selectAsset(ResourceUrn urn) {
        Optional<UIElement> asset = assetManager.getAsset(urn, UIElement.class);
        if (asset.isPresent()) {
            UIElement element = asset.get();

            AssetDataFile source = element.getSource();

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
        Class nodeClass = null;
        try {
            nodeClass = NUIEditorNodeUtils
                .getNodeClass((JsonTree) node.getParent(), getManager())
                .getDeclaredField(node.getValue().getKey())
                .getType();
        } catch (NullPointerException | NoSuchFieldException ignored) {
        }

        if (nodeClass != null && Enum.class.isAssignableFrom(nodeClass)) {
            getManager().pushScreen(EnumEditorScreen.ASSET_URI, EnumEditorScreen.class);
            EnumEditorScreen enumEditorScreen = (EnumEditorScreen) getManager()
                .getScreen(EnumEditorScreen.ASSET_URI);
            enumEditorScreen.setNode(node);
            enumEditorScreen.setEnumClass(nodeClass);
            enumEditorScreen.subscribeClose(() -> {
                editor.fireUpdateListeners();
            });
        } else {
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
        setTreeViewModel(node, true);
        setPreviewWidget(node);

        history.clear();
        history.add(node);
        historyPosition = 0;

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
     * Sets the editor widget's state to a copy of a specified {@link JsonTree}.
     *
     * @param node   The node the widget's state is to be set to.
     * @param expand Whether the node should be expanded.
     */
    private void setTreeViewModel(JsonTree node, boolean expand) {
        if (expand) {
            expandNode(node);
        }

        editor.setModel(node.copy());
    }

    /**
     * Expands a {@link JsonTree} meeting specific conditions; repeat recursively for its' children.
     *
     * @param node The node to be expanded.
     */
    private void expandNode(JsonTree node) {
        // Do not expand OBJECT children of ARRAY parents. Generally concerns widget lists.
        if (!(node.getValue().getType() == JsonTreeValue.Type.OBJECT
              && !node.isRoot() && node.getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
            node.setExpanded(true);
        }

        for (Tree child : node.getChildren()) {
            expandNode((JsonTree) child);
        }
    }

    /**
     * Sets the contents of the preview widget to a widget deserialized from a {@link JsonTree}.
     *
     * @param node The node containing the preview widget.
     */
    private void setPreviewWidget(JsonTree node) {
        try {
            JsonElement element = JsonTreeConverter.deserialize(node);
            UIWidget widget = new UIFormat().load(element).getRootWidget();
            selectedScreenBox.setContent(widget);
        } catch (Throwable t) {
            selectedScreenBox.setContent(new UILabel(ExceptionUtils.getStackTrace(t)));
        }
    }

    /**
     * Adds a {@link JsonTree} to the editor's state history.
     *
     * @param node The node to be added to the editor's state history.
     */
    private void addToHistory(JsonTree node) {
        if (historyPosition < history.size() - 1) {
            history = history.subList(0, historyPosition + 1);
        }
        history.add(node);
        historyPosition++;
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

    /**
     * Sets the editor's state to the previous item in the history.
     */
    private void undo() {
        if (historyPosition > 0) {
            historyPosition--;
            JsonTree node = (JsonTree) history.get(historyPosition).copy();
            setTreeViewModel(node, false);
            setPreviewWidget(node);
            updateConfig();
        }
    }

    /**
     * Sets the editor's state to the next item in the history.
     */
    private void redo() {
        if (historyPosition < history.size() - 1) {
            historyPosition++;
            JsonTree node = (JsonTree) history.get(historyPosition).copy();
            setTreeViewModel(node, false);
            setPreviewWidget(node);
            updateConfig();
        }
    }

    /**
     * Copies the specified node to the editor's clipboard,
     * then deselects it.
     *
     * @param node The node to copy.
     */
    private void copyNode(JsonTree node) {
        editor.copy(node);
        editor.setSelectedIndex(null);
    }

    /**
     * Pastes the currently copied node as a child of the specified node,
     * then deselects it.
     *
     * @param node The node to paste the copied node to.
     */
    private void pasteNode(JsonTree node) {
        editor.paste(node);
        editor.setSelectedIndex(null);
    }

    /**
     * Set up and display {@link WidgetSelectionScreen}. When a widget is selected, add it as a child
     * of the specified node.
     *
     * @param node The node to add the widget to.
     */
    private void addWidget(JsonTree node) {
        getManager().pushScreen(WidgetSelectionScreen.ASSET_URI, WidgetSelectionScreen.class);
        WidgetSelectionScreen widgetSelectionScreen = (WidgetSelectionScreen) getManager()
            .getScreen(WidgetSelectionScreen.ASSET_URI);
        widgetSelectionScreen.setNode(node);
        widgetSelectionScreen.subscribeClose(() -> {
            editor.fireUpdateListeners();
        });
    }
}

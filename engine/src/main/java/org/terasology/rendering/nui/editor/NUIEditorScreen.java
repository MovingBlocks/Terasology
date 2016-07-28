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
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
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

    private static final String AVAILABLE_ASSETS_ID = "availableAssets";
    private static final String EDITOR_TREE_VIEW_ID = "editor";
    private static final String SELECTED_SCREEN_ID = "selectedScreen";

    private static final String CREATE_NEW_SCREEN = "New Screen";

    @In
    private AssetManager assetManager;

    @In
    private Config config;

    @In
    private NUIEditorSystem nuiEditorSystem;

    /**
     *
     */
    private UITreeView editor;
    /**
     *
     */
    private UIBox selectedScreenBox;
    /**
     *
     */
    private String selectedAsset;
    /**
     *
     */
    private List<JsonTree> history = Lists.newArrayList();
    /**
     *
     */
    private int historyPosition;
    /**
     *
     */
    private UITextEntry<JsonTree> inlineEditorEntry;

    @Override
    public void initialise() {
        UIDropdownScrollable<String> availableAssetDropdown = find(AVAILABLE_ASSETS_ID, UIDropdownScrollable.class);
        editor = find(EDITOR_TREE_VIEW_ID, UITreeView.class);
        selectedScreenBox = find(SELECTED_SCREEN_ID, UIBox.class);

        List<String> availableAssetList = Lists.newArrayList();

        availableAssetList.add(CREATE_NEW_SCREEN);
        availableAssetList.addAll(assetManager.getAvailableAssets(UIElement.class).stream().map(Object::toString).collect(Collectors.toList()));

        availableAssetList.removeIf(asset -> asset.equals(ASSET_URI.toString()));
        availableAssetList.removeIf(asset -> asset.equals(ContextMenuScreen.ASSET_URI.toString()));
        availableAssetList.removeIf(asset -> asset.equals(NUIEditorSettingsScreen.ASSET_URI.toString()));
        availableAssetList.removeIf(asset -> asset.equals(WidgetSelectionScreen.ASSET_URI.toString()));
        Collections.sort(availableAssetList);

        availableAssetDropdown.setOptions(availableAssetList);
        availableAssetDropdown.bindSelection(new Binding<String>() {
            @Override
            public String get() {
                return selectedAsset;
            }

            @Override
            public void set(String value) {
                if (value.equals(CREATE_NEW_SCREEN)) {
                    resetState(NUIEditorNodeBuilder.createNewScreen());
                    selectedAsset = value;
                } else {
                    selectFile(new ResourceUrn(value));
                }
            }
        });

        editor.subscribeTreeViewUpdate(() -> {
            JsonTree rootNode = (JsonTree) editor.getModel().getNode(0).getRoot();
            addToHistory(rootNode);
            setPreviewWidget(rootNode);
            updateConfig();
        });

        editor.subscribeNodeClick((event, node) -> {
            if (event.getMouseButton() == MouseInput.MOUSE_RIGHT) {
                editor.setSelectedIndex(editor.getModel().indexOf(node));
                editor.setAlternativeWidget(null);

                NUIEditorContextMenuBuilder contextMenuBuilder = new NUIEditorContextMenuBuilder();
                contextMenuBuilder.setManager(getManager());
                contextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_COPY, this::copyNode);
                contextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_PASTE, this::pasteNode);
                contextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_EDIT, this::editNode);
                contextMenuBuilder.subscribeAddContextMenu(() -> {
                    editor.fireUpdateListeners();
                });
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

        editor.subscribeNodeDoubleClick((event, node) -> {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                editNode((JsonTree) node);
            }
        });

        editor.subscribeKeyEvent(event -> {
            if (event.isDown() && event.getKey() == Keyboard.Key.F2) {
                Integer selectedIndex = editor.getSelectedIndex();

                if (selectedIndex != null) {
                    editNode((JsonTree) editor.getModel().getNode(selectedIndex));
                }
            }
        });

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
     * @param urn
     */
    public void selectFile(ResourceUrn urn) {
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
     *
     */
    public void updateConfig() {
        NUIEditorConfig nuiEditorConfig = config.getNuiEditor();
        editor.setItemRenderer(nuiEditorConfig.isDisableIcons()
            ? new ToStringTextRenderer<>() : new NUIEditorItemRenderer(editor.getModel()));
    }

    /**
     *
     * @param node
     */
    private void editNode(JsonTree node) {
        JsonTreeValue.Type type = node.getValue().getType();

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
     * @param node
     */
    private void focusInlineEditor(JsonTree node) {
        getManager().setFocus(inlineEditorEntry);
        inlineEditorEntry.resetValue();

        if (node.getValue().getType() == JsonTreeValue.Type.KEY_VALUE_PAIR) {
            if (node.getValue().getValue() instanceof String) {
                inlineEditorEntry.setCursorPosition(node.getValue().getKey().length() + "\"\":\"".length(), true);
                inlineEditorEntry.setCursorPosition(inlineEditorEntry.getText().length() - "\"".length(), false);
            } else {
                inlineEditorEntry.setCursorPosition(node.getValue().getKey().length() + "\"\":".length(), true);
                inlineEditorEntry.setCursorPosition(inlineEditorEntry.getText().length(), false);
            }
        } else {
            inlineEditorEntry.setCursorPosition(0, true);
            inlineEditorEntry.setCursorPosition(inlineEditorEntry.getText().length(), false);
        }
    }

    /**
     * @param node
     */
    private void resetState(JsonTree node) {
        setTreeViewModel(node, true);
        setPreviewWidget(node);

        history.clear();
        history.add(node);
        historyPosition = 0;

        if (selectedAsset != null) {
            Optional<UIElement> asset = assetManager.getAsset(selectedAsset, UIElement.class);
            if (asset.isPresent()) {
                asset.get().dispose();
            }
        }

        updateConfig();
    }

    /**
     * @param node
     * @param expand
     */
    private void setTreeViewModel(JsonTree node, boolean expand) {
        if (expand) {
            expandNode(node);
        }

        editor.setModel(node.copy());
    }

    /**
     * @param node
     */
    private void expandNode(JsonTree node) {
        if (!(node.getValue().getType() == JsonTreeValue.Type.OBJECT
              && !node.isRoot() && node.getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
            node.setExpanded(true);
        }

        for (Tree child : node.getChildren()) {
            expandNode((JsonTree) child);
        }
    }

    /**
     * @param node
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
     * @param node
     */
    private void addToHistory(JsonTree node) {
        if (historyPosition < history.size() - 1) {
            history = history.subList(0, historyPosition + 1);
        }
        history.add(node);
        historyPosition++;
    }

    /**
     *
     */
    private void copyJson() {
        if (editor.getModel() != null) {
            JsonElement json = JsonTreeConverter.deserialize(editor.getModel().getNode(0).getRoot());
            String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                clipboard.setContents(new StringSelection(jsonString), null);
            } catch (IllegalStateException e) {
                logger.warn("Clipboard inaccessible.", e);
            }
        }
    }

    /**
     *
     */
    private void pasteJson() {
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
                JsonElement json = new JsonParser().parse(clipboardContents);
                JsonTree node = JsonTreeConverter.serialize(json);
                resetState(node);
            } catch (JsonSyntaxException | NullPointerException e) {
                logger.warn("Could not construct a valid tree from clipboard contents.", e);
            }
        }
    }

    /**
     *
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
     *
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

    private void copyNode(JsonTree node) {
        editor.copy(node);
        editor.setSelectedIndex(null);
    }

    private void pasteNode(JsonTree node) {
        editor.paste(node);
        editor.setSelectedIndex(null);
    }
}

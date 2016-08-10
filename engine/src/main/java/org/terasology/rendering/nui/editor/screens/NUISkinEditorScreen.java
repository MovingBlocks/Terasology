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
package org.terasology.rendering.nui.editor.screens;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.Config;
import org.terasology.config.NUIEditorConfig;
import org.terasology.registry.In;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.contextMenu.ContextMenuBuilder;
import org.terasology.rendering.nui.contextMenu.ContextMenuTree;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.editor.systems.NUISkinEditorSystem;
import org.terasology.rendering.nui.editor.utils.NUIEditorContextMenuBuilder;
import org.terasology.rendering.nui.editor.utils.NUIEditorItemRenderer;
import org.terasology.rendering.nui.editor.utils.NUIEditorNodeUtils;
import org.terasology.rendering.nui.editor.utils.NUIEditorTextEntryBuilder;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UISkinData;
import org.terasology.rendering.nui.skin.UISkinFormat;
import org.terasology.rendering.nui.widgets.JsonEditorTreeView;
import org.terasology.rendering.nui.widgets.UIBox;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UITextEntry;
import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeConverter;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeValue;
import org.terasology.utilities.Assets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class NUISkinEditorScreen extends AbstractEditorScreen {

    private Logger logger = LoggerFactory.getLogger(NUIEditorScreen.class);

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nuiSkinEditorScreen");

    private static final String AVAILABLE_ASSETS_ID = "availableAssets";
    private static final String AVAILABLE_SCREENS_ID = "availableScreens";
    private static final String EDITOR_TREE_VIEW_ID = "editor";
    private static final String SELECTED_SCREEN_ID = "selectedScreen";
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

    private UIBox selectedScreenBox;

    /**
     * The Urn of the currently edited asset.
     */
    private String selectedAsset;
    /**
     * The Urn of the currently selected preview screen.
     */
    private ResourceUrn selectedScreen;
    /**
     * The widget used as an inline node editor.
     */
    private UITextEntry<JsonTree> inlineEditorEntry;

    @Override
    public void initialise() {
        // Retrieve the widgets based on their identifiers.
        UIDropdownScrollable<String> availableAssetDropdown = find(AVAILABLE_ASSETS_ID, UIDropdownScrollable.class);
        UIDropdownScrollable<ResourceUrn> availableScreenDropdown = find(AVAILABLE_SCREENS_ID, UIDropdownScrollable.class);
        JsonEditorTreeView editor = find(EDITOR_TREE_VIEW_ID, JsonEditorTreeView.class);
        selectedScreenBox = find(SELECTED_SCREEN_ID, UIBox.class);

        super.setEditorSystem(nuiSkinEditorSystem);
        super.setEditor(editor);

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

        if (availableScreenDropdown != null) {
            List<ResourceUrn> availableScreenList = Lists.newArrayList(assetManager.getAvailableAssets(UIElement.class));
            Collections.sort(availableScreenList);
            availableScreenDropdown.setOptions(availableScreenList);
            availableScreenDropdown.bindEnabled(new ReadOnlyBinding<Boolean>() {
                @Override
                public Boolean get() {
                    return selectedAsset != null;
                }
            });
            availableScreenDropdown.bindSelection(new Binding<ResourceUrn>() {
                @Override
                public ResourceUrn get() {
                    return selectedScreen;
                }

                @Override
                public void set(ResourceUrn value) {
                    selectedScreen = value;
                    resetPreviewWidget();
                }
            });
        }

        editor.setContextMenuProducer(node -> {
            NUIEditorContextMenuBuilder nuiEditorContextMenuBuilder = new NUIEditorContextMenuBuilder();
            nuiEditorContextMenuBuilder.setManager(getManager());
            nuiEditorContextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_COPY, getEditor()::copyNode);
            nuiEditorContextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_PASTE, getEditor()::pasteNode);
            nuiEditorContextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_EDIT, this::editNode);
            nuiEditorContextMenuBuilder.putConsumer(NUIEditorContextMenuBuilder.OPTION_ADD_WIDGET, this::addWidget);
            nuiEditorContextMenuBuilder.subscribeAddContextMenu(() -> getEditor().fireUpdateListeners());
            ContextMenuTree contextMenuTree = nuiEditorContextMenuBuilder.createPrimarySkinContextMenu(node);

            ContextMenuBuilder builder = new ContextMenuBuilder(getManager());

            builder.setTree(contextMenuTree);

            builder.subscribeClose(() -> {
                getEditor().setAlternativeWidget(null);
                getEditor().setSelectedIndex(null);
            });

            builder.subscribeScreenClosed(() -> {
                if (getEditor().getAlternativeWidget() != null) {
                    focusInlineEditor(node, inlineEditorEntry);
                }
            });

            return builder;
        });

        editor.setEditor(this::editNode, getManager());

        editor.subscribeTreeViewUpdate(() -> {
            editor.addToHistory();
            resetPreviewWidget();
            updateConfig();
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

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    protected void resetState(JsonTree node) {
        getEditor().setTreeViewModel(node, true);
        resetPreviewWidget();
        getEditor().clearHistory();
        updateConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetPreviewWidget() {
        if (selectedScreen != null) {
            try {
                JsonElement element = JsonTreeConverter.deserialize(getEditor().getRoot());
                UISkinData data = new UISkinFormat().load(element);
                UIWidget widget = assetManager.getAsset(selectedScreen, UIElement.class).get().getRootWidget();
                widget.setSkin(Assets.generateAsset(data, UISkin.class));
                selectedScreenBox.setContent(widget);
            } catch (Throwable t) {
                selectedScreenBox.setContent(new UILabel(ExceptionUtils.getStackTrace(t)));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateConfig() {
        NUIEditorConfig nuiEditorConfig = config.getNuiEditor();
        getEditor().setItemRenderer(nuiEditorConfig.isDisableIcons()
            ? new ToStringTextRenderer<>() : new NUIEditorItemRenderer(getEditor().getModel()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void editNode(JsonTree node) {
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
                        getEditor().fireUpdateListeners();
                    }
                }
            });
            getEditor().setAlternativeWidget(inlineEditorEntry);
            focusInlineEditor(node, inlineEditorEntry);
        }
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.editor.layers;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.codehaus.plexus.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.NUIEditorConfig;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.asset.UIFormat;
import org.terasology.engine.rendering.nui.editor.systems.NUISkinEditorSystem;
import org.terasology.engine.rendering.nui.editor.utils.NUIEditorItemRenderer;
import org.terasology.engine.rendering.nui.editor.utils.NUIEditorMenuTreeBuilder;
import org.terasology.engine.rendering.nui.editor.utils.NUIEditorNodeUtils;
import org.terasology.engine.rendering.nui.editor.utils.NUIEditorTextEntryBuilder;
import org.terasology.engine.rendering.nui.skin.UISkinFormat;
import org.terasology.engine.rendering.nui.widgets.JsonEditorTreeView;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.exceptions.InvalidUrnException;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.ToStringTextRenderer;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.skin.UISkinAsset;
import org.terasology.nui.skin.UISkinData;
import org.terasology.nui.widgets.UIBox;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UITextEntry;
import org.terasology.nui.widgets.treeView.JsonTree;
import org.terasology.nui.widgets.treeView.JsonTreeConverter;
import org.terasology.nui.widgets.treeView.JsonTreeValue;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class NUISkinEditorScreen extends AbstractEditorScreen {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nuiSkinEditorScreen");

    private static final String AVAILABLE_ASSETS_ID = "availableAssets";
    private static final String AVAILABLE_SCREENS_ID = "availableScreens";
    private static final String EDITOR_TREE_VIEW_ID = "editor";
    private static final String SELECTED_SCREEN_ID = "selectedScreen";
    private static final String CREATE_NEW_SKIN = "New Skin";

    private Logger logger = LoggerFactory.getLogger(NUIEditorScreen.class);

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
     * The box used to preview a NUI screen with a skin modified by the editor.
     */
    private UIBox selectedScreenBox;
    /**
     * A dropdown containing available asset identifiers.
     */
    private UIDropdownScrollable<String> availableAssetDropdown;
    /**
     * The Urn of the currently edited asset.
     */
    private String selectedAsset;
    /**
     * The Urn of the asset that will be selected after a response to a user prompt.
     */
    private String selectedAssetPending;
    /**
     * The path to the currently selected asset. Null if no path for the asset exists.
     */
    private Path selectedAssetPath;
    /**
     * The Urn of the currently selected preview screen.
     */
    private ResourceUrn selectedScreen;
    /**
     * The widget used as an inline node editor.
     */
    private UITextEntry<JsonTree> inlineEditorEntry;
    /**
     * An alternative locale to be used for screen rendering.
     */
    private Locale alternativeLocale;

    @Override
    public void initialise() {
        // Retrieve the widgets based on their identifiers.
        availableAssetDropdown = find(AVAILABLE_ASSETS_ID, UIDropdownScrollable.class);
        UIDropdownScrollable<ResourceUrn> availableScreenDropdown = find(AVAILABLE_SCREENS_ID, UIDropdownScrollable.class);
        JsonEditorTreeView editor = find(EDITOR_TREE_VIEW_ID, JsonEditorTreeView.class);
        selectedScreenBox = find(SELECTED_SCREEN_ID, UIBox.class);

        super.setEditorSystem(nuiSkinEditorSystem);
        super.setEditor(editor);

        // Populate the list of screens.
        List<String> availableAssetList = Lists.newArrayList();
        availableAssetList.add(CREATE_NEW_SKIN);
        availableAssetList.addAll(assetManager.getAvailableAssets(UISkinAsset.class).stream().map(Object::toString).collect(Collectors.toList()));

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
                    // Construct a new skin tree (or de-serialize from an existing asset)
                    selectedAssetPending = value;
                    if (CREATE_NEW_SKIN.equals(value)) {
                        selectedAssetPath = null;
                        resetState(NUIEditorNodeUtils.createNewSkin());
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

        if (editor != null) {
            editor.setContextMenuTreeProducer(node -> {
                NUIEditorMenuTreeBuilder nuiEditorMenuTreeBuilder = new NUIEditorMenuTreeBuilder();
                nuiEditorMenuTreeBuilder.setManager(getManager());
                nuiEditorMenuTreeBuilder.putConsumer(NUIEditorMenuTreeBuilder.OPTION_COPY, getEditor()::copyNode);
                nuiEditorMenuTreeBuilder.putConsumer(NUIEditorMenuTreeBuilder.OPTION_PASTE, getEditor()::pasteNode);
                nuiEditorMenuTreeBuilder.putConsumer(NUIEditorMenuTreeBuilder.OPTION_EDIT, this::editNode);
                nuiEditorMenuTreeBuilder.putConsumer(NUIEditorMenuTreeBuilder.OPTION_DELETE, getEditor()::deleteNode);
                nuiEditorMenuTreeBuilder.putConsumer(NUIEditorMenuTreeBuilder.OPTION_ADD_WIDGET, this::addWidget);
                nuiEditorMenuTreeBuilder.subscribeAddContextMenu(n -> {
                    getEditor().fireUpdateListeners();

                    // Automatically edit a node that's been added.
                    if (n.getValue().getType() == JsonTreeValue.Type.KEY_VALUE_PAIR) {
                        getEditor().getModel().getNode(getEditor().getSelectedIndex()).setExpanded(true);

                        getEditor().getModel().resetNodes();
                        getEditor().setSelectedIndex(getEditor().getModel().indexOf(n));
                        editNode(n);
                    }
                });
                return nuiEditorMenuTreeBuilder.createPrimarySkinContextMenu(node);
            });

            editor.setEditor(this::editNode, getManager());

            editor.subscribeTreeViewUpdate(() -> {
                getEditor().addToHistory();
                resetPreviewWidget();
                updateConfig();
                setUnsavedChangesPresent(true);
                updateAutosave();
            });
        }


        UIButton save = find("save", UIButton.class);
        save.bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return CREATE_NEW_SKIN.equals(selectedAsset) || areUnsavedChangesPresent();
            }
        });
        save.subscribe(button -> {
            // Save the current look and feel.
            LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();

            // (Temporarily) set the look and feel to the system default.
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {
            }

            // Configure the file chooser.
            JFileChooser fileChooser = new JFileChooser() {
                @Override
                protected JDialog createDialog(Component parent) {
                    JDialog dialog = super.createDialog(parent);
                    dialog.setLocationByPlatform(true);
                    dialog.setAlwaysOnTop(true);
                    return dialog;
                }
            };

            fileChooser.setSelectedFile(new File(CREATE_NEW_SKIN.equals(selectedAsset)
                ? "untitled.skin" : selectedAsset.split(":")[1] + ".skin"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Skin asset file (*.skin)", "skin"));

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                saveToFile(fileChooser.getSelectedFile());
                deleteAutosave();
            }

            // Reload the look and feel.
            try {
                UIManager.setLookAndFeel(currentLookAndFeel);
            } catch (UnsupportedLookAndFeelException ignored) {
            }
        });
        UIButton override = find("override", UIButton.class);
        override.bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return selectedAssetPath != null && areUnsavedChangesPresent();
            }
        });
        override.subscribe(button -> {
            saveToFile(selectedAssetPath);
            deleteAutosave();
        });

        // Set the handlers for the editor buttons.
        WidgetUtil.trySubscribe(this, "settings", button ->
            getManager().pushScreen(NUIEditorSettingsScreen.ASSET_URI, NUIEditorSettingsScreen.class));
        WidgetUtil.trySubscribe(this, "copy", button -> copyJson());
        WidgetUtil.trySubscribe(this, "paste", button -> pasteJson());
        WidgetUtil.trySubscribe(this, "undo", button -> undo());
        WidgetUtil.trySubscribe(this, "redo", button -> redo());
        WidgetUtil.trySubscribe(this, "close", button -> nuiSkinEditorSystem.toggleEditor());

        updateConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectAsset(ResourceUrn urn) {
        boolean isLoaded = assetManager.isLoaded(urn, UISkinAsset.class);
        Optional<UISkinAsset> asset = assetManager.getAsset(urn, UISkinAsset.class);
        if (asset.isPresent()) {
            UISkinAsset skin = asset.get();
            if (!isLoaded) {
                asset.get().dispose();
            }

            AssetDataFile source = skin.getSource();
            String content = null;
            try (JsonReader reader = new JsonReader(new InputStreamReader(source.openStream(), Charsets.UTF_8))) {
                reader.setLenient(true);
                content = new JsonParser().parse(reader).toString();
            } catch (IOException e) {
                logger.error("Could not load asset source file for {}", urn, e);
            }

            if (content != null) {
                JsonTree node = JsonTreeConverter.serialize(new JsonParser().parse(content));
                selectedAssetPending = urn.toString();
                resetState(node);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void resetStateInternal(JsonTree node) {
        getEditor().setTreeViewModel(node, true);
        resetPreviewWidget();

        getEditor().clearHistory();
        updateConfig();
        selectedAsset = selectedAssetPending;
        try {
            ResourceUrn urn = new ResourceUrn(selectedAsset);
            setSelectedAssetPath(urn);
        } catch (InvalidUrnException ignored) {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetPreviewWidget() {
        if (selectedScreen != null) {
            try {
                // Construct a UISkinData instance.
                JsonElement skinElement = JsonTreeConverter.deserialize(getEditor().getRoot());
                UISkinData data = new UISkinFormat().load(skinElement);

                // Get the selected screen asset.
                Optional<UIElement> sourceAsset = assetManager
                    .getAsset(selectedScreen, UIElement.class);

                if (!sourceAsset.isPresent()) {
                    throw new FileNotFoundException(String.format("Asset %s not found", selectedScreen));
                }

                AssetDataFile source = sourceAsset.get().getSource();
                String content;
                try (JsonReader reader = new JsonReader(new InputStreamReader(source.openStream(), Charsets.UTF_8))) {
                    reader.setLenient(true);
                    content = new JsonParser().parse(reader).toString();
                }
                if (content != null) {
                    JsonTree node = JsonTreeConverter.serialize(new JsonParser().parse(content));
                    JsonElement screenElement = JsonTreeConverter.deserialize(node);
                    UIWidget widget = new UIFormat().load(screenElement, alternativeLocale).getRootWidget();

                    // Set the screen's skin using the previously generated UISkinData.
                    widget.setSkin(Assets.generateAsset(data, UISkinAsset.class).getSkin());
                    selectedScreenBox.setContent(widget);
                }

            } catch (Throwable t) {
                String truncatedStackTrace = Joiner.on(System.lineSeparator())
                    .join(Arrays.copyOfRange(ExceptionUtils.getStackFrames(t), 0, 10));
                selectedScreenBox.setContent(new UILabel(truncatedStackTrace));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateConfig() {
        NUIEditorConfig nuiEditorConfig = config.getNuiEditor();

        setDisableAutosave(nuiEditorConfig.isDisableAutosave());

        // Update the editor's item renderer.
        getEditor().setItemRenderer(nuiEditorConfig.isDisableIcons()
            ? new ToStringTextRenderer<>() : new NUIEditorItemRenderer(getEditor().getModel()));
        if (nuiEditorConfig.getAlternativeLocale() != null
            && !nuiEditorConfig.getAlternativeLocale().equals(alternativeLocale)) {
            alternativeLocale = nuiEditorConfig.getAlternativeLocale();
            if (selectedScreen != null) {
                resetPreviewWidget();
            }
        }
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
        } else if (type == JsonTreeValue.Type.OBJECT
            && !(!node.isRoot() && node.getParent().getValue().getType() == JsonTreeValue.Type.ARRAY)) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addWidget(JsonTree node) {
        getManager().pushScreen(WidgetSelectionScreen.ASSET_URI, WidgetSelectionScreen.class);

        // Initialise and show the widget selection screen.
        WidgetSelectionScreen widgetSelectionScreen = (WidgetSelectionScreen) getManager()
            .getScreen(WidgetSelectionScreen.ASSET_URI);
        widgetSelectionScreen.setNode(node);
        widgetSelectionScreen.subscribeClose(() -> {
            JsonTree widget = node.getChildAt(node.getChildren().size() - 1);
            widget.setExpanded(true);
            getEditor().fireUpdateListeners();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Path getAutosaveFile() {
        return PathManager.getInstance().getHomePath().resolve("nuiSkinEditorAutosave.json");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSelectedAsset() {
        return selectedAsset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setSelectedAsset(String selectedAsset) {
        this.selectedAsset = selectedAsset;

        // Also prevent the asset being reset.
        this.selectedAssetPending = selectedAsset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setSelectedAssetPath(ResourceUrn urn) {
        boolean isLoaded = assetManager.isLoaded(urn, UISkinAsset.class);
        Optional<UISkinAsset> asset = assetManager.getAsset(urn, UISkinAsset.class);
        if (asset.isPresent()) {
            UISkinAsset skin = asset.get();
            if (!isLoaded) {
                asset.get().dispose();
            }

            AssetDataFile source = skin.getSource();
            selectedAssetPath = getPath(source);
        }
    }
}

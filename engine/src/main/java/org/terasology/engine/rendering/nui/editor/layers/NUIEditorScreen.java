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
import org.terasology.engine.rendering.nui.editor.systems.NUIEditorSystem;
import org.terasology.engine.rendering.nui.editor.utils.NUIEditorItemRenderer;
import org.terasology.engine.rendering.nui.editor.utils.NUIEditorMenuTreeBuilder;
import org.terasology.engine.rendering.nui.editor.utils.NUIEditorNodeUtils;
import org.terasology.engine.rendering.nui.editor.utils.NUIEditorTextEntryBuilder;
import org.terasology.engine.rendering.nui.widgets.JsonEditorTreeView;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The main NUI editor screen.
 * Contains file selection, editing &amp; preview widgets.
 */
@SuppressWarnings("unchecked")
public final class NUIEditorScreen extends AbstractEditorScreen {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nuiEditorScreen");

    // Editor widget identifiers.
    private static final String AVAILABLE_ASSETS_ID = "availableAssets";
    private static final String EDITOR_TREE_VIEW_ID = "editor";
    private static final String SELECTED_SCREEN_ID = "selectedScreen";
    private static final String CREATE_NEW_SCREEN = "New Screen";

    private Logger logger = LoggerFactory.getLogger(NUIEditorScreen.class);

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
     * The box used to preview a NUI screen modified by the editor.
     */
    private UIBox selectedScreenBox;
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
        UIDropdownScrollable<String> availableAssetDropdown = find(AVAILABLE_ASSETS_ID, UIDropdownScrollable.class);
        JsonEditorTreeView editor = find(EDITOR_TREE_VIEW_ID, JsonEditorTreeView.class);
        selectedScreenBox = find(SELECTED_SCREEN_ID, UIBox.class);

        super.setEditorSystem(nuiEditorSystem);
        super.setEditor(editor);

        // Populate the list of screens.
        List<String> availableAssetList = Lists.newArrayList();
        availableAssetList.add(CREATE_NEW_SCREEN);
        availableAssetList.addAll(assetManager.getAvailableAssets(UIElement.class).stream().map(Object::toString).collect(Collectors.toList()));
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
                    // Construct a new screen tree (or de-serialize from an existing asset)
                    selectedAssetPending = value;
                    if (CREATE_NEW_SCREEN.equals(value)) {
                        selectedAssetPath = null;
                        resetState(NUIEditorNodeUtils.createNewScreen());
                    } else {
                        selectAsset(new ResourceUrn(value));
                    }
                }
            });
        }

        if (editor != null) {
            editor.subscribeTreeViewUpdate(() -> {
                getEditor().addToHistory();
                resetPreviewWidget();
                updateConfig();
                setUnsavedChangesPresent(true);
                updateAutosave();
            });

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
                return nuiEditorMenuTreeBuilder.createPrimaryContextMenu(node);
            });

            editor.setEditor(this::editNode, getManager());
        }

        UIButton save = find("save", UIButton.class);
        save.bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return CREATE_NEW_SCREEN.equals(selectedAsset) || areUnsavedChangesPresent();
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

            fileChooser.setSelectedFile(new File(CREATE_NEW_SCREEN.equals(selectedAsset)
                ? "untitled.ui" : selectedAsset.split(":")[1] + ".ui"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("UI asset file (*.ui)", "ui"));

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
        WidgetUtil.trySubscribe(this, "close", button -> nuiEditorSystem.toggleEditor());

        updateConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectAsset(ResourceUrn urn) {
        boolean isLoaded = assetManager.isLoaded(urn, UIElement.class);
        Optional<UIElement> asset = assetManager.getAsset(urn, UIElement.class);
        if (asset.isPresent()) {
            UIElement element = asset.get();
            if (!isLoaded) {
                asset.get().dispose();
            }

            AssetDataFile source = element.getSource();
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
        try {
            // Serialize the editor's contents and update the widget.
            JsonElement element = JsonTreeConverter.deserialize(getEditor().getRoot());
            UIWidget widget = new UIFormat().load(element, alternativeLocale).getRootWidget();
            selectedScreenBox.setContent(widget);
        } catch (Throwable t) {
            String truncatedStackTrace = Joiner.on(System.lineSeparator())
                .join(Arrays.copyOfRange(ExceptionUtils.getStackFrames(t), 0, 10));
            selectedScreenBox.setContent(new UILabel(truncatedStackTrace));
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

        // Update the alternative locale. If it has been updated, change the preview widget's locale.
        if (nuiEditorConfig.getAlternativeLocale() != null
            && !nuiEditorConfig.getAlternativeLocale().equals(alternativeLocale)) {
            alternativeLocale = nuiEditorConfig.getAlternativeLocale();
            if (selectedAsset != null) {
                resetPreviewWidget();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void editNode(JsonTree node) {
        Class nodeClass = null;

        try {
            Class parentClass = NUIEditorNodeUtils.getNodeInfo((JsonTree) node.getParent(), getManager()).getNodeClass();

            if (parentClass != null) {
                nodeClass = parentClass.getDeclaredField(node.getValue().getKey()).getType();
            }
        } catch (NullPointerException | NoSuchFieldException ignored) {
        }

        if (nodeClass != null && Enum.class.isAssignableFrom(nodeClass)) {
            // If the node is an enum, initialize and show the enum editor screen.
            getManager().pushScreen(EnumEditorScreen.ASSET_URI, EnumEditorScreen.class);
            EnumEditorScreen enumEditorScreen = (EnumEditorScreen) getManager()
                .getScreen(EnumEditorScreen.ASSET_URI);
            enumEditorScreen.setNode(node);
            enumEditorScreen.setEnumClass(nodeClass);
            enumEditorScreen.subscribeClose(() -> getEditor().fireUpdateListeners());
        } else {
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
            node.setExpanded(true);
            JsonTree widget = node.getChildAt(node.getChildren().size() - 1);
            widget.setExpanded(true);
            getEditor().fireUpdateListeners();

            // Automatically edit the id of a newly added widget.
            getEditor().getModel().resetNodes();
            getEditor().setSelectedIndex(getEditor().getModel().indexOf(widget.getChildWithKey("id")));
            editNode(widget.getChildWithKey("id"));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Path getAutosaveFile() {
        return PathManager.getInstance().getHomePath().resolve("nuiEditorAutosave.json");
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
        boolean isLoaded = assetManager.isLoaded(urn, UIElement.class);
        Optional<UIElement> asset = assetManager.getAsset(urn, UIElement.class);
        if (asset.isPresent()) {
            UIElement element = asset.get();
            if (!isLoaded) {
                asset.get().dispose();
            }

            AssetDataFile source = element.getSource();
            selectedAssetPath = getPath(source);
        }
    }
}

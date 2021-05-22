// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.editor.layers;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.editor.systems.AbstractEditorSystem;
import org.terasology.engine.rendering.nui.layers.mainMenu.ConfirmPopup;
import org.terasology.engine.rendering.nui.widgets.JsonEditorTreeView;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.exceptions.InvalidUrnException;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.resources.DirectoryFileSource;
import org.terasology.gestalt.naming.Name;
import org.terasology.input.Keyboard;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.nui.Canvas;
import org.terasology.nui.events.NUIKeyEvent;
import org.terasology.nui.widgets.UITextEntry;
import org.terasology.nui.widgets.treeView.JsonTree;
import org.terasology.nui.widgets.treeView.JsonTreeConverter;
import org.terasology.nui.widgets.treeView.JsonTreeValue;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * A base screen for the NUI screen/skin editors.
 */
public abstract class AbstractEditorScreen extends CoreScreenLayer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Used to get the {@link Path} of an asset.
     */
    @In
    private ModuleManager moduleManager;

    /**
     * The editor system.
     */
    private AbstractEditorSystem editorSystem;
    /**
     * The editor widget.
     */
    private JsonEditorTreeView editor;
    /**
     * Whether unsaved changes in the editor are present.
     */
    private boolean unsavedChangesPresent;
    /**
     * Whether the autosave has been loaded.
     */
    private boolean autosaveLoaded;
    /**
     * Whether autosaving (&loading autosaved files) should be disabled.
     */
    private boolean disableAutosave;

    /**
     * Selects the current asset to be edited.
     *
     * @param urn The urn of the asset.
     */
    public abstract void selectAsset(ResourceUrn urn);

    /**
     * Resets the editor's state based on a tree representation of an asset.
     *
     * @param node The node based on which the editor's state is to be reset.
     */
    protected abstract void resetStateInternal(JsonTree node);

    /**
     * Resets the preview widget based on the editor's current state.
     */
    protected abstract void resetPreviewWidget();

    /**
     * Updates the editor after state or configuration changes.
     */
    protected abstract void updateConfig();

    /**
     * Initialises the widgets, screens etc. used to edit a specified node.
     *
     * @param node The node to be edited.
     */
    protected abstract void editNode(JsonTree node);

    /**
     * Adds a widget selected by the user to the specified node.
     *
     * @param node The node to add a new widget to.
     */
    protected abstract void addWidget(JsonTree node);

    /**
     * @return The path to the backup autosave file.
     */
    protected abstract Path getAutosaveFile();

    /**
     * @return The currently selected asset (or alternative state, i.e. new screen)
     */
    protected abstract String getSelectedAsset();

    /**
     * Sets the selected asset to a specific value.
     *
     * @param selectedAsset The value to set the selected asset to.
     */
    protected abstract void setSelectedAsset(String selectedAsset);

    /**
     * Reset the stored path to an asset file based on an asset urn.
     *
     * @param urn The asset urn.
     */
    protected abstract void setSelectedAssetPath(ResourceUrn urn);

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // If the autosave is loaded in initialise(), the preview widget is updated before interface component
        // sizes are set, which breaks the editor's layout.
        // Therefore, the autosave is loaded after the first onDraw() call.
        if (!autosaveLoaded) {
            loadAutosave();
            autosaveLoaded = true;
        }
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            int id = event.getKey().getId();
            KeyboardDevice keyboard = event.getKeyboard();
            boolean ctrlDown = keyboard.isKeyDown(Keyboard.KeyId.RIGHT_CTRL) || keyboard.isKeyDown(Keyboard.KeyId.LEFT_CTRL);

            if (id == Keyboard.KeyId.ESCAPE) {
                editorSystem.toggleEditor();
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
     * Sets the editor's state based on the previous item in the editor widget's history.
     *
     * @see JsonEditorTreeView#undo()
     */
    protected void undo() {
        if (editor.undo()) {
            resetPreviewWidget();
            updateConfig();
        }
    }

    /**
     * Sets the editor's state based on the next item in the editor widget's history.
     *
     * @see JsonEditorTreeView#redo()
     */
    protected void redo() {
        if (editor.redo()) {
            resetPreviewWidget();
            updateConfig();
        }
    }

    /**
     * Resets the editor's state based on a specified {@link JsonTree}.
     *
     * @param node The {@link JsonTree} to reset the state from.
     */
    protected void resetState(JsonTree node) {
        if (unsavedChangesPresent) {
            ConfirmPopup confirmPopup = getManager().pushScreen(ConfirmPopup.ASSET_URI, ConfirmPopup.class);
            confirmPopup.setMessage("Unsaved changes!", "It looks like you've been editing something!" +
                "\r\nAll unsaved changes will be lost. Continue anyway?");
            confirmPopup.setOkHandler(() -> {
                setUnsavedChangesPresent(false);
                deleteAutosave();
                resetStateInternal(node);
            });
        } else {
            resetStateInternal(node);
        }
    }

    /**
     * Saves the contents of the editor as a JSON string to a specified file.
     *
     * @param file The file to save to.
     */
    protected void saveToFile(File file) {
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            saveToFile(outputStream);
            setUnsavedChangesPresent(false);
        } catch (IOException e) {
            logger.warn("Could not save asset", e);
        }
    }

    /**
     * Saves the contents of the editor as a JSON string to a specified file.
     *
     * @param path The path to save to.
     */
    protected void saveToFile(Path path) {
        try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path))) {
            saveToFile(outputStream);
            setUnsavedChangesPresent(false);
        } catch (IOException e) {
            logger.warn("Could not save asset", e);
        }
    }

    /**
     * Updates the autosave file with the current state of the tree.
     */
    protected void updateAutosave() {
        if (!disableAutosave) {
            try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(getAutosaveFile()))) {
                JsonElement editorContents = JsonTreeConverter.deserialize(getEditor().getModel().getNode(0).getRoot());

                JsonObject autosaveObject = new JsonObject();
                autosaveObject.addProperty("selectedAsset", getSelectedAsset());
                autosaveObject.add("editorContents", editorContents);

                String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(autosaveObject);
                outputStream.write(jsonString.getBytes());
            } catch (IOException e) {
                logger.warn("Could not save to autosave file", e);
            }
        }
    }

    /**
     * Resets the editor based on the state of the autosave file.
     */
    protected void loadAutosave() {
        if (!disableAutosave) {
            try (JsonReader reader = new JsonReader(new InputStreamReader(Files.newInputStream(getAutosaveFile())))) {
                reader.setLenient(true);
                String autosaveString = new JsonParser().parse(reader).toString();

                JsonObject autosaveObject = new JsonParser().parse(autosaveString).getAsJsonObject();
                String selectedAsset = autosaveObject.get("selectedAsset").getAsString();
                setSelectedAsset(selectedAsset);

                try {
                    ResourceUrn urn = new ResourceUrn(selectedAsset);
                    setSelectedAssetPath(urn);
                } catch (InvalidUrnException ignored) {
                }

                JsonTree editorContents = JsonTreeConverter.serialize(autosaveObject.get("editorContents"));
                resetState(editorContents);

                setUnsavedChangesPresent(true);
            } catch (NoSuchFileException ignored) {
            } catch (IOException e) {
                logger.warn("Could not load autosaved info", e);
            }
        }
    }

    /**
     * Deletes the autosave file.
     */
    protected void deleteAutosave() {
        try {
            Files.delete(getAutosaveFile());
        } catch (NoSuchFileException ignored) {
        } catch (IOException e) {
            logger.warn("Could not delete autosave file", e);
        }
    }

    private void saveToFile(BufferedOutputStream outputStream) throws IOException {
        // Serialize tree contents and save to selected file.
        JsonElement json = JsonTreeConverter.deserialize(getEditor().getModel().getNode(0).getRoot());
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);
        outputStream.write(jsonString.getBytes());
    }

    /**
     * Returns the file path to a given asset if the module providing the asset is present as source. The path will for
     * instance be used by the editor to edit the asset. Editing files within module jars is not possible. Hence, we
     * return `null` in case the module is not present as source.
     *
     * @param source asset file object to get the file path to
     * @return path to asset or null if module not present as source
     */
    protected Path getPath(AssetDataFile source) {
        List<String> path = source.getPath();
        Name moduleName = new Name(path.get(0));
        Module module = verifyNotNull(moduleManager.getEnvironment().get(moduleName),
                "Module \"%s\" not found in current module environment.", moduleName);
        // TODO: Checking whether the module is present as source should not be done in `getPath()` as this has no
        //  knowledge about what the path will be used for (read vs write access)
        if (module.getResources() instanceof DirectoryFileSource) {
            path.add(source.getFilename());
            String[] pathArray = path.toArray(new String[path.size()]);

            // Copy all the elements after the first to a separate array for getPath().
            String first = pathArray[0];
            String[] more = Arrays.copyOfRange(pathArray, 1, pathArray.length);
            return Paths.get("", moduleManager.getEnvironment().getResources()
                    .getFile(first, more)
                    .orElseThrow(()-> new RuntimeException("Cannot get path for "+source.getFilename())).getPath().stream().toArray(String[]::new));
        }
        return null;
    }

    /**
     * De-serializes the current state of the editor and copies it to the system clipboard.
     */
    protected void copyJson() {
        if (getEditor().getModel() != null) {
            // Deserialize the state of the editor to a JSON string.
            JsonElement json = JsonTreeConverter.deserialize(getEditor().getModel().getNode(0).getRoot());
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
     * Attempts to serialize the system clipboard's contents - if successful,
     * sets the current state of the editor to the serialized {@link JsonTree}.
     */
    protected void pasteJson() {
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
     * Sets the interface focus to the inline editor widget and selects a subset of its' contents.
     *
     * @param node              The node that is currently being edited.
     * @param inlineEditorEntry The inline editor widget.
     */
    protected void focusInlineEditor(JsonTree node, UITextEntry inlineEditorEntry) {
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

    protected void setEditorSystem(AbstractEditorSystem editorSystem) {
        this.editorSystem = editorSystem;
    }

    protected JsonEditorTreeView getEditor() {
        return this.editor;
    }

    protected void setEditor(JsonEditorTreeView editor) {
        this.editor = editor;
    }

    protected boolean areUnsavedChangesPresent() {
        return unsavedChangesPresent;
    }

    protected void setUnsavedChangesPresent(boolean unsavedChangesPresent) {
        this.unsavedChangesPresent = unsavedChangesPresent;
    }

    protected void setDisableAutosave(boolean disableAutosave) {
        this.disableAutosave = disableAutosave;
        if (disableAutosave) {
            deleteAutosave();
        }
    }
}

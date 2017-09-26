/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.filePicker;

import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.rendering.nui.widgets.UIText;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FilePickerPopup extends CoreScreenLayer {


    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:filePickerPopup!instance");
    // credit: https://stackoverflow.com/a/894133
    private static final char[] ILLEGAL_FILENAME_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

    private String fileName = "";
    private Path currentPath;
    private UIList<String> directoryContentsList;
    private Consumer<Path> okHandler = (path) -> { };

    public void setOkHandler(Consumer<Path> okHandler) {
        this.okHandler = okHandler;
    }

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "gotoParent", button -> setCurrentDirectoryParent());
        WidgetUtil.trySubscribe(this, "gotoRoot", button -> setCurrentDirectoryRoot());
        WidgetUtil.trySubscribe(this, "gotoHome", button -> setCurrentDirectory(Paths.get(System.getProperty("user.home"))));

        find("gotoParent", UIButton.class).bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return currentPath != null;
            }
        });
        find("currentPath", UILabel.class).bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return currentPath == null ? "File system roots" : pathToString(currentPath, false);
            }
        });
        find("fileName", UIText.class).bindText(new Binding<String>() {
            @Override
            public String get() {
                return fileName;
            }

            @Override
            public void set(String value) {
                fileName = value;
            }
        });
        find("filePath", UILabel.class).bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return currentPath == null ? "Invalid location"
                        : isValidFilename(fileName) ? "Full path to file: " + getPathToFile().toString() : "Invalid file name";
            }
        });
        directoryContentsList = find("directoryContentsList", UIList.class);
        directoryContentsList.subscribeSelection((widget, item) -> handleItemSelection(item));
        setCurrentDirectoryRoot();

        UIButton ok = find("ok", UIButton.class);
        ok.bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return isValid();
            }
        });
        ok.subscribe(button -> {
            if (isValid()) {
                getManager().popScreen();
                okHandler.accept(getPathToFile());
            }
        });
        WidgetUtil.trySubscribe(this, "cancel", button -> getManager().popScreen());
    }

    private boolean isValid() {
        return currentPath != null && isValidFilename(fileName);
    }

    private Path getPathToFile() {
        return currentPath.resolve(fileName);
    }

    private boolean isValidFilename(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        for (char c: ILLEGAL_FILENAME_CHARACTERS) {
            if (s.indexOf(c) != -1) {
                return false;
            }
        }
        return true;
    }

    private String pathToString(Path value, boolean nameOnly) {
        String pathAsString = (nameOnly && value.getNameCount() != 0 ? value.getFileName() : value).toString();
        String separator = value.getFileSystem().getSeparator();
        return !pathAsString.endsWith(separator) && Files.isDirectory(value) ? pathAsString + separator : pathAsString;
    }

    private void loadDirectoryContents(Stream<Path> contents) {
        directoryContentsList.setList(contents
                .map(path -> pathToString(path, true))
                .collect(Collectors.toList()));
    }

    private void setCurrentDirectoryRoot() {
        currentPath = null;
        loadDirectoryContents(StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), true));
    }

    private void setCurrentDirectoryParent() {
        if (currentPath != null) {
            if (currentPath.getParent() != null) {
                setCurrentDirectory(currentPath.getParent());
            } else {
                setCurrentDirectoryRoot();
            }
        }
    }

    private void handleItemSelection(String item) {
        Path path = currentPath == null ? Paths.get(item) : currentPath.resolve(item);
        if (Files.isDirectory(path)) {
            setCurrentDirectory(path);
        } else {
            fileName = item;
        }
    }

    private void setCurrentDirectory(Path newPath) {
        try {
            loadDirectoryContents(Files.list(newPath));
            currentPath = newPath;
        } catch (AccessDeniedException ex) {
            showDirectoryAccessErrorMessage("Access denied to " + newPath);
        } catch (IOException ex) {
            showDirectoryAccessErrorMessage(ex.toString());
        }
    }

    private void showDirectoryAccessErrorMessage(String message) {
        getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Failed to change directory", message);
    }
}

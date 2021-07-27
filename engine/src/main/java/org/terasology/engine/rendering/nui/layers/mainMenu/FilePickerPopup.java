// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.layouts.ScrollableArea;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIList;
import org.terasology.nui.widgets.UIText;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FilePickerPopup extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:filePickerPopup!instance");
    // credit: https://stackoverflow.com/a/894133
    private static final char[] ILLEGAL_FILENAME_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

    @In
    private TranslationSystem translationSystem;

    private String fileName = "";
    private Path currentPath;
    private ScrollableArea directoryContentsScroller;
    private UIList<String> directoryContentsList;
    private Consumer<Path> okHandler = (path) -> {
    };

    public void setOkHandler(Consumer<Path> okHandler) {
        this.okHandler = okHandler;
    }

    public void setTitle(String title) {
        find("title", UILabel.class).setText(title);
    }

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "gotoParent", button -> setCurrentDirectoryParent());
        WidgetUtil.trySubscribe(this, "gotoRoot", button -> setCurrentDirectoryRoot());
        WidgetUtil.trySubscribe(this, "gotoHome", button -> setCurrentDirectoryHome());

        find("gotoParent", UIButton.class).bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return currentPath != null;
            }
        });
        find("currentPath", UILabel.class).bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return currentPath == null
                        ? translationSystem.translate("${engine:menu#file-picker-roots-title}")
                        : pathToString(currentPath, false);
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
                if (currentPath == null) {
                    return translationSystem.translate("${engine:menu#file-picker-invalid-location}");
                } else {
                    return isValidFilename(fileName)
                            ? translationSystem.translate("") + getPathToFile().toString()
                            : translationSystem.translate("${engine:menu#file-picker-invalid-file-name}");
                }
            }
        });
        directoryContentsScroller = find("directoryContentsScroller", ScrollableArea.class);
        directoryContentsList = find("directoryContentsList", UIList.class);
        directoryContentsList.subscribeSelection((widget, item) -> handleItemSelection(item));
        setCurrentDirectoryHome();

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
        for (char c : ILLEGAL_FILENAME_CHARACTERS) {
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
                .sorted(Comparator.comparing(String::toLowerCase)) // sort by name (case insensitive string natural order)
                .collect(Collectors.toList()));
        directoryContentsScroller.moveToTop();
    }

    private void setCurrentDirectoryRoot() {
        currentPath = null;
        loadDirectoryContents(StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), true));
    }

    private void setCurrentDirectoryHome() {
        setCurrentDirectory(Paths.get(System.getProperty("user.home")));
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

    // Set the stream path in a try with resources construct first in order to close the stream.
    private void setCurrentDirectory(Path newPath) {
        try (Stream<Path> stream = Files.list(newPath)) {
            loadDirectoryContents(stream);
            currentPath = newPath;
        } catch (AccessDeniedException ex) {
            showDirectoryAccessErrorMessage(translationSystem.translate("${engine:menu#file-picker-access-denied-to}") + newPath);
        } catch (IOException ex) {
            showDirectoryAccessErrorMessage(ex.toString());
        }
    }

    private void showDirectoryAccessErrorMessage(String message) {
        getManager()
                .pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                .setMessage(translationSystem.translate("${engine:menu#file-picker-cant-change-dir}"), message);
    }
}

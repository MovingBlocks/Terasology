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

import com.google.common.collect.Lists;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UIList;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FilePickerPopup extends CoreScreenLayer {

    private Path currentPath;
    private List<Path> directoryContentsList;

    @Override
    public void initialise() {
        setCurrentDirectoryRoot();
        WidgetUtil.trySubscribe(this, "gotoParent", button -> setCurrentDirectory(currentPath.getParent()));
        WidgetUtil.trySubscribe(this, "gotoRoot", button -> setCurrentDirectoryRoot());
        WidgetUtil.trySubscribe(this, "gotoHome", button -> setCurrentDirectory(Paths.get(System.getProperty("user.home"))));
        UIList<Path> directoryContentsListWidget = find("directoryContentsList", UIList.class);
        directoryContentsListWidget.bindList(new ReadOnlyBinding<List<Path>>() {
            @Override
            public List<Path> get() {
                return directoryContentsList;
            }
        });
        directoryContentsListWidget.subscribe((widget, item) -> setCurrentDirectory(item));
    }

    private void setCurrentDirectoryRoot() {
        currentPath = null;
        directoryContentsList = Lists.newArrayList(FileSystems.getDefault().getRootDirectories());
    }

    private void setCurrentDirectory(Path newPath) {
        currentPath = newPath;
        try {
            directoryContentsList = Files.list(currentPath).collect(Collectors.toList());
        } catch (IOException ex) {
            setCurrentDirectoryRoot();
        }
    }
}

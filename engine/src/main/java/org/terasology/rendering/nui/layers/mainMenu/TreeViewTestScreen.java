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
package org.terasology.rendering.nui.layers.mainMenu;

import com.google.common.base.Charsets;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.widgets.UITreeView;
import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeConverter;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeNode;
import org.terasology.rendering.nui.widgets.treeView.Tree;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class TreeViewTestScreen extends CoreScreenLayer {
    private static final String ASSET_URN = "engine:treeViewTestScreen";

    private Logger logger = LoggerFactory.getLogger(TreeViewTestScreen.class);

    @In
    private AssetManager assetManager;

    @Override
    public void initialise() {
        // Load our own asset file.
        AssetDataFile source = assetManager.getAsset(ASSET_URN, UIElement.class).get().getSource();

        String content = null;
        try (JsonReader reader = new JsonReader(new InputStreamReader(source.openStream(), Charsets.UTF_8))) {
            reader.setLenient(true);
            content = new JsonParser().parse(reader).toString();
        } catch (IOException e) {
            logger.error("Could not load tree view source file", e);
        }

        // Deserialize the file, then expand every node.
        JsonTree tree = JsonTreeConverter.serialize(new JsonParser().parse(content));
        Iterator it = tree.getDepthFirstIterator(false);
        while (it.hasNext()) {
            ((Tree<JsonTreeNode>) it.next()).setExpanded(true);
        }

        for (String id : new String[]{"treeViewDisabled", "treeViewEnabled"}) {
            find(id, UITreeView.class).setModel(tree.copy());
            find(id, UITreeView.class).setDefaultValue("New Item");
        }
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
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
import com.google.common.io.Files;
import com.google.gson.JsonParser;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.widgets.UITreeView;
import org.terasology.rendering.nui.widgets.models.JsonTreeAdapter;
import org.terasology.rendering.nui.widgets.models.Tree;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class TreeViewTestScreen extends CoreScreenLayer {
    @Override
    public void initialise() {
        // Load our own asset file for the demo.
        File file = new File(getClass().getClassLoader().getResource("assets/ui/treeViewTestScreen.ui").getFile());
        String content = null;
        try {
            content = Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Deserialize the file, then expand every node.
        Tree<JsonTreeAdapter.JsonNode> tree = JsonTreeAdapter.serialize(new JsonParser().parse(content));
        Iterator it = tree.getDepthFirstIterator(false);
        while (it.hasNext()) {
            ((Tree<JsonTreeAdapter.JsonNode>) it.next()).setExpanded(true);
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

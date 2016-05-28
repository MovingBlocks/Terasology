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

import com.google.common.collect.Lists;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.widgets.UITreeView;
import org.terasology.rendering.nui.widgets.models.Tree;

import java.util.List;

public class TreeViewTestScreen extends CoreScreenLayer {
    @Override
    public void initialise() {
        List<Tree<String>> nodes = Lists.newArrayList();
        for (int i = 0; i <= 10; i++) {
            nodes.add(new Tree<>("Item " + i));
        }

        /**
         * 0
         * | \
         * |  \
         * |\  \
         * | \  \
         * 1  4  5
         * |  |  |\
         * |  |  | \
         * 2  8  6  9
         * |\       |
         * | \      |
         * 3  7     10
         */

        nodes.get(0).addChild(nodes.get(1));
        nodes.get(0).addChild(nodes.get(4));
        nodes.get(0).addChild(nodes.get(5));
        nodes.get(1).addChild(nodes.get(2));
        nodes.get(2).addChild(nodes.get(3));
        nodes.get(2).addChild(nodes.get(7));
        nodes.get(4).addChild(nodes.get(8));
        nodes.get(5).addChild(nodes.get(6));
        nodes.get(5).addChild(nodes.get(9));
        nodes.get(9).addChild(nodes.get(10));

        nodes.get(0).setExpanded(true);
        nodes.get(1).setExpanded(true);
        nodes.get(5).setExpanded(true);

        for (String id : new String[]{"treeView1", "treeView2", "treeView3", "treeView4"}) {
            find(id, UITreeView.class).setModel(nodes.get(0));
        }
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}

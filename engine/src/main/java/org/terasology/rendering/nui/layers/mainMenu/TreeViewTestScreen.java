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
        List<Tree<String>> treeList = Lists.newArrayList();
        for (int i = 0; i <= 10; i++) {
            treeList.add(new Tree<>("Item " + i));
            treeList.get(i).setExpanded(true);
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

        treeList.get(0).addChild(treeList.get(1));
        treeList.get(0).addChild(treeList.get(4));
        treeList.get(0).addChild(treeList.get(5));
        treeList.get(1).addChild(treeList.get(2));
        treeList.get(2).addChild(treeList.get(3));
        treeList.get(2).addChild(treeList.get(7));
        treeList.get(4).addChild(treeList.get(8));
        treeList.get(5).addChild(treeList.get(6));
        treeList.get(5).addChild(treeList.get(9));
        treeList.get(9).addChild(treeList.get(10));

        for (String id : new String[]{"treeView1", "treeView2", "treeView3", "treeView4"}) {
            find(id, UITreeView.class).setModel(treeList.get(0).copy());
            find(id, UITreeView.class).setDefaultValue("New Item");
        }
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}

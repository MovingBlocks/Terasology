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
package org.terasology.rendering.nui.widgets.models;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExpandableTreeTest {
    private List<ExpandableTree<Integer>> nodes = Lists.newArrayList();

    @Before
    public void setup() {
        for (int i = 0; i <= 10; i++) {
            nodes.add(new ExpandableTree<>(i));
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
    }

    @Test
    public void getParent() {
        assertNull(nodes.get(0).getParent());
        assertEquals(nodes.get(0), nodes.get(4).getParent());
        assertEquals(nodes.get(9), nodes.get(10).getParent());
    }

    @Test
    public void getChildren() {
        assertEquals(Lists.newArrayList(), nodes.get(10).getChildren());
        assertEquals(Arrays.asList(nodes.get(6), nodes.get(9)), nodes.get(5).getChildren());
    }

    @Test
    public void getBreadthFirstEnumeration() {
        List<ExpandableTree<Integer>> expected = Arrays.asList(nodes.get(0),
                nodes.get(1), nodes.get(4), nodes.get(5),
                nodes.get(2), nodes.get(8), nodes.get(6), nodes.get(9),
                nodes.get(3), nodes.get(7), nodes.get(10)
        );

        List<ExpandableTree<Integer>> actual = Lists.newArrayList();
        Iterator i = nodes.get(0).getBreadthFirstIterator();
        while (i.hasNext()) {
            actual.add((ExpandableTree<Integer>) i.next());
        }
        assertEquals(expected, actual);
    }

    @Test
    public void getBreadthFirstEnumerationExpanded() {
        List<ExpandableTree<Integer>> expandedNodes = this.nodes;
        expandedNodes.get(0).setExpanded(true);
        expandedNodes.get(1).setExpanded(true);
        expandedNodes.get(5).setExpanded(true);

        List<ExpandableTree<Integer>> expected = Arrays.asList(expandedNodes.get(0),
                expandedNodes.get(1), expandedNodes.get(4), expandedNodes.get(5),
                expandedNodes.get(2), expandedNodes.get(6), expandedNodes.get(9)
        );

        List<ExpandableTree<Integer>> actual = Lists.newArrayList();
        Iterator i = expandedNodes.get(0).getBreadthFirstIterator(true);
        while (i.hasNext()) {
            actual.add((ExpandableTree<Integer>) i.next());
        }
        assertEquals(expected, actual);
    }
}

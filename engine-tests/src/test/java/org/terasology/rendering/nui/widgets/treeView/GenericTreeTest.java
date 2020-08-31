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
package org.terasology.rendering.nui.widgets.treeView;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.nui.widgets.treeView.GenericTree;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenericTreeTest {
    private List<GenericTree<Integer>> nodes = Lists.newArrayList();

    @BeforeEach
    public void setup() {
        for (int i = 0; i <= 10; i++) {
            nodes.add(new GenericTree<>(i));
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
    }

    @Test
    public void testGetParent() {
        assertNull(nodes.get(0).getParent());
        assertEquals(nodes.get(0), nodes.get(4).getParent());
        assertEquals(nodes.get(9), nodes.get(10).getParent());
    }

    @Test
    public void testGetChildren() {
        assertEquals(Lists.newArrayList(), nodes.get(10).getChildren());
        assertEquals(Arrays.asList(nodes.get(6), nodes.get(9)), nodes.get(5).getChildren());
    }

    @Test
    public void testContainsChild() {
        assertTrue(nodes.get(0).containsChild(nodes.get(1)));
        assertTrue(nodes.get(0).containsChild(nodes.get(4)));
        assertTrue(nodes.get(9).containsChild(nodes.get(10)));
        assertFalse(nodes.get(7).containsChild(nodes.get(3)));
    }

    @Test
    public void testDepthFirstIterator() {
        List<GenericTree<Integer>> expected = Arrays.asList(
                nodes.get(0),
                nodes.get(1),
                nodes.get(2),
                nodes.get(3),
                nodes.get(7),
                nodes.get(4),
                nodes.get(8),
                nodes.get(5),
                nodes.get(6),
                nodes.get(9),
                nodes.get(10)
        );

        List<GenericTree<Integer>> actual = Lists.newArrayList();
        Iterator i = nodes.get(0).getDepthFirstIterator(false);
        while (i.hasNext()) {
            actual.add((GenericTree<Integer>) i.next());
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testDepthFirstIteratorIterateExpandedOnly() {
        List<GenericTree<Integer>> expected = Arrays.asList(nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(4), nodes.get(5), nodes.get(6), nodes.get(9));

        List<GenericTree<Integer>> actual = Lists.newArrayList();
        Iterator i = nodes.get(0).getDepthFirstIterator(true);
        while (i.hasNext()) {
            actual.add((GenericTree<Integer>) i.next());
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testNodeDepth() {
        assertEquals(0, nodes.get(0).getDepth());
        assertEquals(1, nodes.get(1).getDepth());
        assertEquals(2, nodes.get(8).getDepth());
        assertEquals(3, nodes.get(10).getDepth());
    }

    @Test
    public void testGetRoot() {
        for (GenericTree<Integer> node : nodes) {
            assertEquals(nodes.get(0), node.getRoot());
        }
    }
}

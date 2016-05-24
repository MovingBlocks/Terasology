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
package org.terasology.utilities.collection;


import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DirectedAcyclicGraphTest {

    @Test
    public void addTest() {
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>();

        dag.addVertex("A");
        dag.addVertex("B");
        dag.addVertex("C");
    }

    @Test
    public void addPrecessorTest() {
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>();

        assertTrue(dag.addVertex("A"));
        assertTrue(dag.addVertex("B"));
        assertTrue(dag.addVertex("C"));

        dag.addEdge("B", "A");
        dag.addEdge("C", "A");
    }

    @Test
    public void addPrecessorTest2() {
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>();

        assertTrue(dag.addVertex("A"));
        assertTrue(dag.addVertex("B"));
        assertTrue(dag.addVertex("C"));

        dag.addEdge("A", "B");
        dag.addEdge("A", "C");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addUnknownEdgeTest() {
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>();

        dag.addEdge("C", "A");
    }


    @Test
    public void cyclicCheckTest() {
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>();

        dag.addVertex("A");
        dag.addVertex("B");

        assertTrue(dag.addEdge("A", "B"));
        assertFalse(dag.addEdge("B", "A"));
    }

    @Test
    public void topologicalOrderTest() {
        DirectedAcyclicGraph<Integer> dag = new DirectedAcyclicGraph<>();
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4);
        for (Integer i : list) {
            assertTrue(dag.addVertex(i));
        }

        assertTrue(dag.addEdge(1, 2));
        assertTrue(dag.addEdge(2, 3));
        assertTrue(dag.addEdge(3, 4));
        assertTrue(dag.addEdge(1, 3));
        assertTrue(dag.addEdge(1, 4));

        List<Integer> order = dag.getTopologicalOrder();

        assertEquals(Arrays.toString(list.toArray()), Arrays.toString(order.toArray()));
    }

    @Test
    public void topologicalOrderComplexCyclicTest() {
        DirectedAcyclicGraph<Integer> dag = new DirectedAcyclicGraph<>();
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4);
        for (Integer i : list) {
            assertTrue(dag.addVertex(i));
        }

        assertTrue(dag.addEdge(1, 2));
        assertTrue(dag.addEdge(2, 3));
        assertTrue(dag.addEdge(3, 4));
        assertTrue(dag.addEdge(1, 3));
        assertTrue(dag.addEdge(1, 4));
        assertFalse(dag.addEdge(4, 2));

        List<Integer> order = dag.getTopologicalOrder();

        assertNotNull(order);
    }

}
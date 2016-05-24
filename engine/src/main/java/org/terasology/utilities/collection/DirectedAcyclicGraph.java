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


import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sets;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class DirectedAcyclicGraph<T> {

    private Map<T, Node> objectNodeMap;
    private Set<Node> nodes;

    public DirectedAcyclicGraph() {
        nodes = new LinkedHashSet<>();
        objectNodeMap = Maps.newHashMap();
    }

    public boolean addNode(T o) {
        Preconditions.checkNotNull(o);
        if (objectNodeMap.containsKey(o)) {
            return false;
        }

        Node v = new Node(o);
        objectNodeMap.put(o, v);
        nodes.add(v);

        return true;
    }

    public boolean addEdge(T sourceObject, T targetObject) {
        assertNodeExists(sourceObject);
        assertNodeExists(targetObject);

        Node sourceNode = objectNodeMap.get(sourceObject);
        Node targetNode = objectNodeMap.get(targetObject);

        if (sourceNode.hasPrecessor(targetNode)) {
            throw new IllegalArgumentException("Node " + targetNode.toString() +
                    " is already precessor of " + sourceNode.toString());
        }

        sourceNode.addPrecessor(targetNode);

        if (getTopologicalOrder() == null) {
            sourceNode.removePrecessor(targetNode);
            return false;
        } else {
            return true;
        }
    }

    public List<T> getTopologicalOrder() {

        // depth first search based cyclic checking
        try {
            TopologicalSorter topologicalSorter = new TopologicalSorter();
            List<T> order = Lists.newArrayList();
            order.addAll(topologicalSorter.sort().stream().map(vertex -> vertex.object).collect(Collectors.toList()));
            return order;
        } catch (CycleFoundException e) {
            return null;
        }
    }


    private boolean assertNodeExists(T o) {
        Preconditions.checkNotNull(o);
        Node v = objectNodeMap.get(o);

        if (v != null && nodes.contains(v)) {
            return true;
        } else {
            throw new IllegalArgumentException("No such vertex in graph: " + o.toString());
        }
    }

    public static class CycleFoundException extends Exception {
    }

    private final class Node {
        Set<Node> precessors;
        private T object;

        private Node(T object) {
            this.object = object;
            precessors = new LinkedHashSet<>();
        }

        private boolean hasPrecessor(Node precessor) {
            return precessors.contains(precessor);
        }

        private boolean addPrecessor(Node precessor) {
            return precessors.add(precessor);
        }

        private boolean removePrecessor(Node precessor) {
            return precessors.remove(precessor);
        }

        @Override
        public String toString() {
            return object.toString();
        }
    }

    private final class TopologicalSorter {

        private Set<Node> unexploredSet;
        private LinkedList<Node> sorted;
        private Set<Node> temporarilyVisited;

        private TopologicalSorter() {
            unexploredSet = Sets.newHashSet();
            temporarilyVisited = Sets.newHashSet();
            unexploredSet.addAll(nodes);
            sorted = new LinkedList<>();
        }

        private LinkedList<Node> sort() throws CycleFoundException {
            for (Node v : nodes) {
                if (unexploredSet.contains(v)) {
                    depthFirstSearch(v);
                }
            }
            return sorted;
        }

        private void depthFirstSearch(Node v) throws CycleFoundException {
            if (temporarilyVisited.contains(v)) {
                throw new CycleFoundException();
            } else {

                temporarilyVisited.add(v);

                for (Node p : v.precessors) {
                    if (unexploredSet.contains(p)) {
                        depthFirstSearch(p);
                    }
                }

                // mark node permanently as explored
                unexploredSet.remove(v);

                // remove mark temporarily
                temporarilyVisited.remove(v);

                // add to head
                sorted.addFirst(v);
            }
        }


    }

}

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

    private Map<T, Vertex> objectVertexMap;
    private Set<Vertex> vertices;

    public DirectedAcyclicGraph() {
        vertices = new LinkedHashSet<>();
        objectVertexMap = Maps.newHashMap();
    }

    public boolean addVertex(T o) {
        Preconditions.checkNotNull(o);
        if (objectVertexMap.containsKey(o)) {
            return false;
        }

        Vertex v = new Vertex(o);
        objectVertexMap.put(o, v);
        vertices.add(v);

        return true;
    }

    public boolean addEdge(T sourceObject, T targetObject) {
        assertVertexExists(sourceObject);
        assertVertexExists(targetObject);

        Vertex sourceVertex = objectVertexMap.get(sourceObject);
        Vertex targetVertex = objectVertexMap.get(targetObject);

        if (sourceVertex.hasPrecessor(targetVertex)) {
            throw new IllegalArgumentException("Vertex " + targetVertex.toString() +
                    " is already precessor of " + sourceVertex.toString());
        }

        sourceVertex.addPrecessor(targetVertex);

        if (getTopologicalOrder() == null) {
            sourceVertex.removePrecessor(targetVertex);
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


    private boolean assertVertexExists(T o) {
        Preconditions.checkNotNull(o);
        Vertex v = objectVertexMap.get(o);

        if (v != null && vertices.contains(v)) {
            return true;
        } else {
            throw new IllegalArgumentException("No such vertex in graph: " + o.toString());
        }
    }

    public static class CycleFoundException extends Exception {
    }

    private final class Vertex {
        Set<Vertex> precessors;
        private T object;

        private Vertex(T object) {
            this.object = object;
            precessors = new LinkedHashSet<>();
        }

        private boolean hasPrecessor(Vertex precessor) {
            return precessors.contains(precessor);
        }

        private boolean addPrecessor(Vertex precessor) {
            return precessors.add(precessor);
        }

        private boolean removePrecessor(Vertex precessor) {
            return precessors.remove(precessor);
        }

        @Override
        public String toString() {
            return object.toString();
        }
    }

    private final class TopologicalSorter {

        private Set<Vertex> unexploredSet;
        private LinkedList<Vertex> sorted;
        private Set<Vertex> temporarilyVisited;

        private TopologicalSorter() {
            unexploredSet = Sets.newHashSet();
            temporarilyVisited = Sets.newHashSet();
            unexploredSet.addAll(vertices);
            sorted = new LinkedList<>();
        }

        private LinkedList<Vertex> sort() throws CycleFoundException {
            for (Vertex v : vertices) {
                if (unexploredSet.contains(v)) {
                    depthFirstSearch(v);
                }
            }
            return sorted;
        }

        private void depthFirstSearch(Vertex v) throws CycleFoundException {
            if (temporarilyVisited.contains(v)) {
                throw new CycleFoundException();
            } else {

                temporarilyVisited.add(v);

                for (Vertex p : v.precessors) {
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
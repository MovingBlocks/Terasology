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
package org.terasology.rendering.dag;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * TODO: Add javadocs
 */
public class RenderGraph {
    private static final Logger logger = LoggerFactory.getLogger(RenderGraph.class);

    private Map<SimpleUri, Node> nodeMap = Maps.newHashMap();
    // TODO: Convert to biMap
    private Multimap<Node, Node> edgeMap = HashMultimap.create();
    private Multimap<Node, Node> reverseEdgeMap = HashMultimap.create();

    public SimpleUri addNode(Node node, String suggestedUri) {
        Preconditions.checkNotNull(node, "node cannot be null!");
        Preconditions.checkNotNull(suggestedUri, "suggestedUri cannot be null!");

        SimpleUri nodeUri = new SimpleUri("engine:" + suggestedUri);

        if (nodeMap.containsKey(nodeUri)) {
            int i = 2;
            while (nodeMap.containsKey(new SimpleUri(nodeUri.toString() + i))) {
                i++;
            }
            nodeUri = new SimpleUri(nodeUri.toString() + i);
        }

        node.setUri(nodeUri);
        nodeMap.put(nodeUri, node);

        return nodeUri;
    }

    public Node removeNode(SimpleUri nodeUri) {
        Preconditions.checkNotNull(nodeUri, "nodeUri cannot be null!");

        if (edgeMap.containsKey(nodeUri)) {
            throw new RuntimeException("The node you are trying to remove is still connected to other nodes in the graph!");
        }

        return nodeMap.remove(nodeUri);
    }

    public Node findNode(SimpleUri nodeUri) {
        Preconditions.checkNotNull(nodeUri, "nodeUri cannot be null!");

        return nodeMap.get(nodeUri);
    }

    public Node findNode(String simpleUri) {
        return findNode(new SimpleUri(simpleUri));
    }

    public boolean connect(Node ... nodeList) {
        boolean returnValue = true;

        assert(nodeList.length > 1);

        Node fromNode = null;

        for (Node toNode : nodeList) {
            Preconditions.checkNotNull(toNode, "toNode cannot be null!");

            if (fromNode != null) {
                boolean success = edgeMap.put(fromNode, toNode);
                if (success) {
                    reverseEdgeMap.put(toNode, fromNode);
                } else {
                    logger.warn("Trying to connect two already connected nodes, " + fromNode.getClass() + " and " + toNode.getClass());
                }

                returnValue = returnValue && success;
            }

            fromNode = toNode;
        }

        return returnValue;
    }

    public boolean disconnect(Node fromNode, Node toNode) {
        Preconditions.checkNotNull(fromNode, "fromNode cannot be null!");
        Preconditions.checkNotNull(toNode, "toNode cannot be null!");

        reverseEdgeMap.remove(toNode, fromNode);
        return edgeMap.remove(fromNode, toNode);
    }

    // TODO: Add `boolean isFullyFunctional(Node node)`

    // TODO: Add handler methods which the graph uses to communicate changes to a node.

    public List<Node> getNodesInTopologicalOrder() {
        // This implementation of Kahn's Algorithm is adapted from the algorithm described at
        // https://www.geeksforgeeks.org/topological-sorting-indegree-based-solution/

        List<Node> topologicalList = new ArrayList<>();

        // In-degree (or incoming-degree) is the number of incoming edges of a particular node.
        Map<Node, Integer> inDegreeMap = Maps.newHashMap();
        List<Node> nodesToExamine = Lists.newArrayList();
        int visitedNodes = 0;

        // Calculate the in-degree for each node.
        for (Entry<Node, Collection<Node>> nodeEdgeList : reverseEdgeMap.asMap().entrySet()) {
            inDegreeMap.put(nodeEdgeList.getKey(), nodeEdgeList.getValue().size());
        }

        // Add the missing nodes that did not have any edges, and mark them for examination
        for (Node node : nodeMap.values()) {
            if (!inDegreeMap.containsKey(node)) {
                inDegreeMap.put(node, 0);
                nodesToExamine.add(node);
            }
        }

        while (!nodesToExamine.isEmpty()) {
            Node currentNode = nodesToExamine.remove(0);

            for (Node adjacentNode : edgeMap.get(currentNode)) {
                int updatedInDegree = inDegreeMap.get(adjacentNode) - 1;
                inDegreeMap.put(adjacentNode, updatedInDegree);

                if (updatedInDegree == 0) {
                    nodesToExamine.add(adjacentNode);
                }
            }

            topologicalList.add(currentNode);

            visitedNodes++;
        }

        if (visitedNodes != nodeMap.size()) {
            throw new RuntimeException("Cycle detected in the DAG: topological sorting not possible!");
        }

        return topologicalList;
    }

    public void dispose() {
        nodeMap.clear();
        edgeMap.clear();
        reverseEdgeMap.clear();
    }
}

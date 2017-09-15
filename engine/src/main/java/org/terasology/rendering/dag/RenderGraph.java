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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.logstash.logback.encoder.org.apache.commons.lang.Validate;
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
    private Multimap<Node, Node> edgeMap = HashMultimap.create();
    private Multimap<Node, Node> reverseEdgeMap = HashMultimap.create();

    public SimpleUri addNode(Node node, String suggestedUri) {
        Validate.notNull(node, "node cannot be null!");
        Validate.notNull(suggestedUri, "suggestedUri cannot be null!");

        SimpleUri nodeUri = new SimpleUri("engine:" + suggestedUri);

        if (nodeMap.containsKey(nodeUri)) {
            int i = 2;
            while (nodeMap.containsKey(new SimpleUri(nodeUri.toString() + i))) {
                i++;
            }
            nodeUri = new SimpleUri(nodeUri.toString() + i);
        }

        nodeMap.put(nodeUri, node);
        node.setUri(nodeUri);

        return nodeUri;
    }

    public Node removeNode(SimpleUri nodeUri) {
        Validate.notNull(nodeUri, "nodeUri cannot be null!");

        if (edgeMap.containsKey(nodeUri)) {
            throw new RuntimeException("The node you are trying to remove is still connected to other nodes in the graph!");
        }

        return nodeMap.remove(nodeUri);
    }

    public Node findNode(SimpleUri nodeUri) {
        Validate.notNull(nodeUri, "nodeUri cannot be null!");

        return nodeMap.get(nodeUri);
    }

    public boolean connect(Node fromNode, Node toNode) {
        Validate.notNull(fromNode, "fromNode cannot be null!");
        Validate.notNull(toNode, "toNode cannot be null!");

        reverseEdgeMap.put(toNode, fromNode);
        boolean success = edgeMap.put(fromNode, toNode);
        if (!success) {
            logger.warn("Trying to connect two already connected nodes, " + fromNode.getClass() + " and " + toNode.getClass());
        }

        return success;
    }

    public boolean disconnect(Node fromNode, Node toNode) {
        Validate.notNull(fromNode, "fromNode cannot be null!");
        Validate.notNull(toNode, "toNode cannot be null!");

        reverseEdgeMap.remove(toNode, fromNode);
        return edgeMap.remove(fromNode, toNode);
    }

    // TODO: Add `boolean isFullyFunctional(Node node)`

    // TODO: Add handler methods which the graph uses to communicate changes to a node.

    public List<Node> getNodesInTopologicalOrder() {
        List<Node> topologicalList = new ArrayList<>();

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
            throw new RuntimeException("Topological sorting not possible for the DAG!");
        }

        return topologicalList;
    }

    public void dispose() {
        nodeMap.clear();
        edgeMap.clear();
    }
}

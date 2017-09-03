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
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.logstash.logback.encoder.org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Add javadocs
 */
public class RenderGraph {
    private static final Logger logger = LoggerFactory.getLogger(RenderGraph.class);

    private Map<SimpleUri, Node> nodeMap = Maps.newHashMap();
    private Multimap<Node, Node> edgeMap = HashMultimap.create();

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

        boolean success = edgeMap.put(fromNode, toNode);
        if (!success) {
            logger.warn("Trying to connect two already connected nodes, " + fromNode.getClass() + " and " + toNode.getClass());
        }

        return success;
    }

    public boolean disconnect(Node fromNode, Node toNode) {
        Validate.notNull(fromNode, "fromNode cannot be null!");
        Validate.notNull(toNode, "toNode cannot be null!");

        return edgeMap.remove(fromNode, toNode);
    }

    // TODO: Add `boolean isFullyFunctional(Node node)`

    // TODO: Add handler methods which the graph uses to communicate changes to a node.

    public List<Node> getNodesInTopologicalOrder() {
        List<Node> topologicalList = new ArrayList<>();
        List<Node> nodesToExamine = new ArrayList<>();

        nodesToExamine.add(findNode(new SimpleUri("engine:shadowMapClearingNode"))); // TODO: Find a better way to set first Node.
        while (!nodesToExamine.isEmpty()) {
            Node currentNode = nodesToExamine.remove(0);
            topologicalList.add(currentNode);
            nodesToExamine.addAll(edgeMap.get(currentNode));
        }

        return topologicalList;
    }
}

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
import org.terasology.engine.SimpleUri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Add javadocs
 */
public class RenderGraph { // TODO: add extends DirectedAcyclicGraph<Node>
    private Map<SimpleUri, Node> nodes = Maps.newHashMap();
    private Multimap<Node, Node> nodeEdges = HashMultimap.create();

    public SimpleUri addNode(Node node, String suggestedUri) {
        // TODO: make sure URIs are actually unique: if "myModule:blur" is present the node gets the uri "myModule:blur2" instead.
        // TODO: make sure the namespace in the uri is engine-assigned, so that only engine nodes can have the "engine:" namespace - everything else gets the namespace of the module.
        SimpleUri nodeUri = new SimpleUri("engine:" + suggestedUri);

        nodes.put(nodeUri, node);

        return nodeUri;
    }

    public boolean removeNode(SimpleUri nodeUri) {
        if (nodeEdges.containsKey(nodeUri)) {
            return false;
        }

        nodes.remove(nodeUri);

        return true; // TODO: What if nodeUri does not refer to a Node?
    }

    public Node findNode(SimpleUri nodeUri) {
        return nodes.get(nodeUri);
    }

    public void addEdge(Node node1, Node node2) {
        nodeEdges.put(node1, node2);
    }

    public boolean removeEdge(Node node1, Node node2) {
        return nodeEdges.remove(node1, node2);
    }

    public List<Node> getNodesInTopologicalOrder() {
        List<Node> topologicalList = new ArrayList<>();
        List<Node> nodesToExamine = new ArrayList<>();

        nodesToExamine.add(findNode(new SimpleUri("engine:shadowMapClearingNode"))); // TODO: Find a better way to set first Node.
        while (!nodesToExamine.isEmpty()) {
            Node currentNode = nodesToExamine.get(0);
            topologicalList.add(currentNode);
            nodesToExamine.addAll(nodeEdges.get(currentNode));
            nodesToExamine.remove(0);
        }

        return topologicalList;
    }
}

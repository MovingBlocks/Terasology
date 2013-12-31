/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.behavior.tree.Node;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Factory to create node instances from node entities.
 *
 * @author synopia
 */
public class BehaviorNodeFactory {
    private final Logger logger = LoggerFactory.getLogger(BehaviorNodeFactory.class);

    private Map<Class<? extends Node>, BehaviorNodeComponent> nodes = Maps.newHashMap();

    public BehaviorNodeFactory(List<BehaviorNodeComponent> components) {
        for (BehaviorNodeComponent component : components) {
            try {
                Class<? extends Node> type = (Class<Node>) Class.forName(component.type);
                nodes.put(type, component);
            } catch (ClassNotFoundException e) {
                logger.warn("Cannot find behavior node for class " + component.type + " name=" + component.name);
            }
        }
    }

    public BehaviorNodeComponent getNodeComponent(Node node) {
        BehaviorNodeComponent nodeComponent = nodes.get(node.getClass());
        if (nodeComponent == null) {
            return BehaviorNodeComponent.DEFAULT;
        }
        return nodeComponent;
    }

    public Node getNode(BehaviorNodeComponent nodeComponent) {
        for (Map.Entry<Class<? extends Node>, BehaviorNodeComponent> entry : nodes.entrySet()) {
            if (nodeComponent == entry.getValue()) {
                try {
                    return entry.getKey().newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    public Collection<BehaviorNodeComponent> getNodeComponents() {
        return nodes.values();
    }
}

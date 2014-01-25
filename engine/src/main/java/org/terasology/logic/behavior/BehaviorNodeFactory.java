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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleManager;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.registry.CoreRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
    private Map<String, List<BehaviorNodeComponent>> categoryComponents = Maps.newHashMap();
    private final List<String> categories;

    public BehaviorNodeFactory(List<BehaviorNodeComponent> components) {
        for (BehaviorNodeComponent component : components) {
            ClassLoader[] classLoaders = CoreRegistry.get(ModuleManager.class).getActiveModuleReflections().getConfiguration().getClassLoaders();
            for (ClassLoader classLoader : classLoaders) {
                try {
                    Class<? extends Node> type = (Class<? extends Node>) classLoader.loadClass(component.type);
                    nodes.put(type, component);
                    logger.warn("Found behavior node for class " + component.type + " name=" + component.name);

                    List<BehaviorNodeComponent> list = categoryComponents.get(component.category);
                    if (list == null) {
                        list = Lists.newArrayList();
                        categoryComponents.put(component.category, list);
                    }
                    list.add(component);
                    break;
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        categories = Lists.newArrayList(categoryComponents.keySet());
        Collections.sort(categories);
        for (String category : categories) {
            Collections.sort(categoryComponents.get(category), new Comparator<BehaviorNodeComponent>() {
                @Override
                public int compare(BehaviorNodeComponent o1, BehaviorNodeComponent o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
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

    public List<String> getCategories() {
        return categories;
    }

    public List<BehaviorNodeComponent> getNodesComponents(String category) {
        return categoryComponents.get(category);
    }
}

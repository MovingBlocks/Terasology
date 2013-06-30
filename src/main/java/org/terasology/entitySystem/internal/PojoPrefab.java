/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.entitySystem.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetData;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.AbstractPrefab;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;

import java.util.List;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoPrefab extends AbstractPrefab {
    private static final Logger logger = LoggerFactory.getLogger(PojoPrefab.class);

    private Prefab parent;
    private Map<Class<? extends Component>, Component> componentMap;
    private List<Prefab> children = Lists.newArrayList();
    private boolean persisted;

    public PojoPrefab(AssetUri uri, PrefabData data) {
        super(uri);
        reload(data);
    }

    @Override
    public Prefab getParent() {
        return parent;
    }

    @Override
    public List<Prefab> getChildren() {
        return ImmutableList.copyOf(children);
    }

    @Override
    public boolean isPersisted() {
        return persisted;
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return componentMap.containsKey(component);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(componentMap.get(componentClass));
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return ImmutableList.copyOf(componentMap.values());
    }

    @Override
    public void dispose() {
    }

    @Override
    public void reload(PrefabData data) {
        this.componentMap = ImmutableMap.copyOf(data.getComponents());
        this.persisted = data.isPersisted();
        this.parent = data.getParent();
        if (parent != null && parent instanceof PojoPrefab) {
            ((PojoPrefab) parent).children.add(this);
        }
    }

    @Override
    public boolean isDisposed() {
        return false;
    }
}

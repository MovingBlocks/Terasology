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
package org.terasology.entitySystem.prefab;

import com.google.common.collect.Maps;
import org.terasology.asset.AssetData;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.MutableComponentContainer;

import java.util.Map;

/**
 * @author Immortius
 */
public class PrefabData implements AssetData, MutableComponentContainer {

    private Map<Class<? extends Component>, Component> components = Maps.newHashMap();
    private boolean persisted = true;
    private Prefab parent;
    private boolean alwaysRelevant;

    @Override
    public <T extends Component> T addComponent(T component) {
        components.put(component.getClass(), component);
        return component;
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        components.remove(componentClass);
    }

    @Override
    public void saveComponent(Component component) {
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return components.containsKey(component);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return components.values();
    }

    public Map<Class<? extends Component>, Component> getComponents() {
        return components;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    public void setParent(Prefab parent) {
        this.parent = parent;
    }

    public Prefab getParent() {
        return parent;
    }

    public boolean isAlwaysRelevant() {
        return alwaysRelevant;
    }

    public void setAlwaysRelevant(boolean alwaysRelevant) {
        this.alwaysRelevant = alwaysRelevant;
    }
}
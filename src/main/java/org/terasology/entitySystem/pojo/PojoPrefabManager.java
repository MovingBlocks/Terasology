/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.entitySystem.pojo;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;

import java.util.*;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoPrefabManager implements PrefabManager {

    protected final static String OBSERVER_EVENT_DESTROYED = "destroyed";
    protected final static String OBSERVER_EVENT_RENAMED = "rename";

    ComponentLibrary componentLibrary;
    Map<String, Prefab> prefabTable = Maps.newHashMap();

    public PojoPrefabManager(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
    }

    public Prefab createPrefab(String name) {
        String normalisedName = normalizeName(name);
        if (exists(normalisedName)) {
            return getPrefab(normalisedName);
        }

        return this.registerPrefab(new PojoPrefab(name, componentLibrary));
    }

    public Prefab getPrefab(String name) {
        String normalisedName = normalizeName(name);
        return exists(normalisedName) ? prefabTable.get(normalisedName) : null;
    }

    public boolean exists(String name) {
        String normalisedName = normalizeName(name);
        return prefabTable.containsKey(normalisedName);
    }

    public Prefab registerPrefab(Prefab prefab) {
        String normalisedName = normalizeName(prefab.getName());
        if (prefabTable.containsKey(normalisedName)) {
            throw new IllegalArgumentException("Prefab '" + prefab.getName() + "' already registered!");
        }

        prefabTable.put(normalisedName, prefab);

        return prefab;
    }

    public Iterable<Prefab> listPrefabs() {
        return Collections.unmodifiableCollection(prefabTable.values());
    }

    public void removePrefab(String name) {
        String normalisedName = normalizeName(name);
        prefabTable.remove(normalisedName);
    }

    public <T extends Component> T getComponent(String name, Class<T> componentClass) {
        if (!exists(name)) {
            return null;
        }

        String normalisedName = normalizeName(name);
        return getPrefab(normalisedName).getComponent(componentClass);
    }

    public <T extends Component> T setComponent(String name, T component) {
        if (!exists(name)) {
            throw new IllegalArgumentException("No prefab exists with name: " + name);
        }

        String normalisedName = normalizeName(name);
        return getPrefab(normalisedName).setComponent(component);
    }

    public <T extends Component> void removeComponent(String name, Class<T> componentClass) {
        if (!exists(name)) {
            throw new IllegalArgumentException("No prefab exists with name: " + name);
        }

        String normalisedName = normalizeName(name);
        getPrefab(normalisedName).removeComponent(componentClass);
    }

    protected class PrefabObserver implements Observer {

        public void update(Observable o, Object arg) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private String normalizeName(String name) {
        return name.toLowerCase(Locale.ENGLISH);
    }
}

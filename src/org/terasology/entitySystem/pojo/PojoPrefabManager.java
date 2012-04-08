package org.terasology.entitySystem.pojo;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;

import java.util.Collections;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoPrefabManager implements PrefabManager {

    protected final static String OBSERVER_EVENT_DESTROYED = "destroyed";
    protected final static String OBSERVER_EVENT_RENAMED = "rename";

    Map<String, Prefab> prefabTable = Maps.newHashMap();

    public Prefab createPrefab(String name) {
        if (exists(name)) {
            return getPrefab(name);
        }

        return this.registerPrefab(new PojoPrefab(name));
    }

    public Prefab getPrefab(String name) {
        return exists(name) ? prefabTable.get(name) : null;
    }

    public boolean exists(String name) {
        return prefabTable.containsKey(name);
    }

    public Prefab registerPrefab(Prefab prefab) {
        if (prefabTable.containsKey(prefab.getName())) {
            throw new IllegalArgumentException("Prefab '" + prefab.getName() + "' already registered!");
        }

        prefabTable.put(prefab.getName(), prefab);

        return prefab;
    }

    public Iterable<Prefab> listPrefabs() {
        return Collections.unmodifiableCollection(prefabTable.values());
    }

    public void removePrefab(String name) {
        prefabTable.remove(name);
    }

    public <T extends Component> T getComponent(String name, Class<T> componentClass) {
        if (!exists(name)) {
            return null;
        }

        return getPrefab(name).getComponent(componentClass);
    }

    public <T extends Component> T setComponent(String name, T component) {
        if (!exists(name)) {
            throw new IllegalArgumentException("No prefab exists with name: " + name);
        }

        return getPrefab(name).setComponent(component);
    }

    public <T extends Component> void removeComponent(String name, Class<T> componentClass) {
        if (!exists(name)) {
            throw new IllegalArgumentException("No prefab exists with name: " + name);
        }

        getPrefab(name).removeComponent(componentClass);
    }

    protected class PrefabObserver implements Observer {

        public void update(Observable o, Object arg) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}

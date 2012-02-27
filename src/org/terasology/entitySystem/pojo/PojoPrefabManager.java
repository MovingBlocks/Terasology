package org.terasology.entitySystem.pojo;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.PrefabRef;
import org.terasology.entitySystem.PrefabManager;

import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoPrefabManager implements PrefabManager {

    Map<String, Map<Class<? extends Component>, Component>> prefabTable = Maps.newHashMap();
    
    public PrefabRef create(String name) {
        if (prefabTable.containsKey(name)) {
            throw new IllegalArgumentException("Prefab name already in use: " + name);
        }
        Map<Class<? extends Component>, Component> map = Maps.newHashMap();
        prefabTable.put(name, map);
        return new PojoPrefabRef(this, name, map);
    }

    public PrefabRef get(String name) {
        if (prefabTable.containsKey(name)) {
            return new PojoPrefabRef(this, name, prefabTable.get(name));
        }
        return null;
    }

    public boolean exists(String name) {
        return prefabTable.containsKey(name);
    }

    public PrefabRef rename(String oldName, String name) {
        if (prefabTable.containsKey(name)) {
            throw new IllegalArgumentException("Prefab name already in use: " + name);
        }
        Map<Class<? extends Component>, Component> map = prefabTable.get(oldName);
        if (map == null) {
            throw new IllegalArgumentException("No prefab exists with name: " + oldName);
        }
        prefabTable.put(name, map);
        prefabTable.remove(oldName);
        return new PojoPrefabRef(this, name, map);
    }

    public void destroy(String name) {
        prefabTable.remove(name);
    }

    public <T extends Component> T getComponent(String name, Class<T> componentClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addComponent(String name, Component component) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T extends Component> void removeComponent(String name, Class<T> componentClass) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void saveComponent(String name, Component component) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

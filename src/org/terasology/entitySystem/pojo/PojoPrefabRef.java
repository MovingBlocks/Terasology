package org.terasology.entitySystem.pojo;

import com.google.common.base.Objects;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.PrefabRef;
import org.terasology.entitySystem.PrefabManager;

import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoPrefabRef implements PrefabRef {
    private PrefabManager manager;
    private String name;
    private Map<Class<? extends Component>, Component> componentMap;
    
    PojoPrefabRef(PrefabManager manager, String name, Map<Class<? extends Component>, Component> componentMap) {
        this.manager = manager;
        this.name = name;
        this.componentMap = componentMap;
    }
    
    public String getName() {
        return name;
    }

    public void rename(String newName) {
        manager.rename(name, newName);
        name = newName;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(componentMap.get(componentClass));
    }

    public <T extends Component> T addComponent(T component) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeComponent(Class<? extends Component> componentClass) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void saveComponent(Component component) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterable<Component> iterateComponents() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void destroy() {
        manager.destroy(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof PojoPrefabRef) {
            return Objects.equal(name, ((PojoPrefabRef)o).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
    
    @Override
    public String toString() {
        return "Prefab:" + name;
    }
}

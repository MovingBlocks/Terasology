package org.terasology.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EventHandlerSystem;

import java.util.List;
import java.util.Map;

/**
 * Simple manager for component systems.
 * This is an initial, rough implementation to be improved later.
 * @author Immortius <immortius@gmail.com>
 */
public class ComponentSystemManager {
    private Map<Class<? extends ComponentSystem>, ComponentSystem> store = Maps.newHashMap();
    private List<UpdateSubscriberSystem> updateSubscribers = Lists.newArrayList();
    private List<RenderSystem> renderSubscribers = Lists.newArrayList();

    public ComponentSystemManager() {}

    public <T extends ComponentSystem> void register(T object) {
        register(object, (Class<T>)object.getClass());
    }
    
    public <T extends ComponentSystem> void register(T object, Class<T> asType) {
        store.put(asType, object);
        if (object instanceof UpdateSubscriberSystem) {
            updateSubscribers.add((UpdateSubscriberSystem)object);
        }
        if (object instanceof RenderSystem) {
            renderSubscribers.add((RenderSystem)object);
        }
        if (object instanceof EventHandlerSystem) {
            CoreRegistry.get(EntityManager.class).getEventSystem().registerEventHandler((EventHandlerSystem)object);
        }
    }
    
    public <T> T get(Class<T> type) {
        return type.cast(store.get(type));
    }

    public void clear() {
        store.clear();
        updateSubscribers.clear();
        renderSubscribers.clear();
    }

    public Iterable<ComponentSystem> iterateAll() {
        return store.values();
    }

    public Iterable<UpdateSubscriberSystem> iterateUpdateSubscribers() {
        return updateSubscribers;
    }
    
    public Iterable<RenderSystem> iterateRenderSubscribers() {
        return renderSubscribers;
    }
}

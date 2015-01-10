/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.engine.bootstrap;

import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.event.internal.EventSystemImpl;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.MetadataUtil;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.entitySystem.systems.internal.DoNotAutoRegister;
import org.terasology.logic.behavior.asset.NodesClassLibrary;

import org.terasology.module.ModuleEnvironment;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.properties.OneOfProviderFactory;

/**
 * @author Immortius
 */
// TODO: Review - This could be a static class but its existence is also questionable.
public class EntitySystemBuilder {

    public EngineEntityManager build(ModuleEnvironment environment, NetworkSystem networkSystem, ReflectFactory reflectFactory) {
        return build(environment, networkSystem, reflectFactory, new CopyStrategyLibrary(reflectFactory));
    }

    public EngineEntityManager build(ModuleEnvironment environment, NetworkSystem networkSystem, ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategyLibrary) {
        // Entity Manager
        PojoEntityManager entityManager = CoreRegistry.put(EntityManager.class, new PojoEntityManager());
        CoreRegistry.put(EngineEntityManager.class, entityManager);

        // Standard serialization library
        TypeSerializationLibrary typeSerializationLibrary = TypeSerializationLibrary.createDefaultLibrary(entityManager,
                reflectFactory, copyStrategyLibrary);
        entityManager.setTypeSerializerLibrary(typeSerializationLibrary);

        // Entity System Library
        EntitySystemLibrary library = CoreRegistry.put(EntitySystemLibrary.class, new EntitySystemLibrary(reflectFactory, copyStrategyLibrary, typeSerializationLibrary));
        entityManager.setComponentLibrary(library.getComponentLibrary());
        CoreRegistry.put(ComponentLibrary.class, library.getComponentLibrary());
        CoreRegistry.put(EventLibrary.class, library.getEventLibrary());

        // Prefab Manager
        PrefabManager prefabManager = new PojoPrefabManager();
        entityManager.setPrefabManager(prefabManager);
        CoreRegistry.put(PrefabManager.class, prefabManager);

        // Event System
        EventSystem eventSystem = new EventSystemImpl(library.getEventLibrary(), networkSystem);
        entityManager.setEventSystem(eventSystem);
        CoreRegistry.put(EventSystem.class, eventSystem);

        // TODO: Review - NodeClassLibrary related to the UI for behaviours. Should not be here and probably not even in the CoreRegistry
        CoreRegistry.put(OneOfProviderFactory.class, new OneOfProviderFactory());

        // Behaviour Trees Node Library
        NodesClassLibrary nodesClassLibrary = new NodesClassLibrary(reflectFactory, copyStrategyLibrary);
        CoreRegistry.put(NodesClassLibrary.class, nodesClassLibrary);
        nodesClassLibrary.scan(environment);

        registerComponents(library.getComponentLibrary(), environment);
        registerEvents(entityManager.getEventSystem(), environment);
        return entityManager;
    }



    private void registerComponents(ComponentLibrary library, ModuleEnvironment environment) {
        for (Class<? extends Component> componentType : environment.getSubtypesOf(Component.class)) {
            if (componentType.getAnnotation(DoNotAutoRegister.class) == null) {
                String componentName = MetadataUtil.getComponentClassName(componentType);
                library.register(new SimpleUri(environment.getModuleProviding(componentType), componentName), componentType);
            }
        }
    }

    private void registerEvents(EventSystem eventSystem, ModuleEnvironment environment) {
        for (Class<? extends Event> type : environment.getSubtypesOf(Event.class)) {
            if (type.getAnnotation(DoNotAutoRegister.class) == null) {
                eventSystem.registerEvent(new SimpleUri(environment.getModuleProviding(type), type.getSimpleName()), type);
            }
        }
    }

}

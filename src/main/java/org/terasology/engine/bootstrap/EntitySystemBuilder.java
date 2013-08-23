/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.engine.bootstrap;

import org.reflections.Reflections;
import org.terasology.asset.AssetType;
import org.terasology.audio.Sound;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.DoNotAutoRegister;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.EventSystem;
import org.terasology.entitySystem.internal.EventSystemImpl;
import org.terasology.entitySystem.internal.PojoEntityManager;
import org.terasology.entitySystem.internal.PojoPrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibraryBuilder;
import org.terasology.entitySystem.metadata.typeHandlers.extension.AssetTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.BlockFamilyTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.BlockTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.CollisionGroupTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.Color4fTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.EntityRefTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.PrefabTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.Quat4fTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.Region3iTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.Vector2fTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.Vector3iTypeHandler;
import org.terasology.entitySystem.metadata.typeHandlers.extension.Vector4fTypeHandler;
import org.terasology.entitySystem.metadata.internal.EntitySystemLibraryImpl;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.CollisionGroup;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Color4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Set;

/**
 * @author Immortius
 */
public class EntitySystemBuilder {

    public EngineEntityManager build(ModuleManager moduleManager, NetworkSystem networkSystem) {
        PojoEntityManager entityManager = new PojoEntityManager();
        TypeHandlerLibrary typeHandlerLibrary = buildTypeLibrary(entityManager);
        EntitySystemLibrary library = new EntitySystemLibraryImpl(typeHandlerLibrary);
        CoreRegistry.put(EntitySystemLibrary.class, library);
        CoreRegistry.put(ComponentLibrary.class, library.getComponentLibrary());
        CoreRegistry.put(EventLibrary.class, library.getEventLibrary());
        entityManager.setEntitySystemLibrary(library);

        PrefabManager prefabManager = new PojoPrefabManager();
        entityManager.setPrefabManager(prefabManager);
        CoreRegistry.put(PrefabManager.class, prefabManager);

        entityManager.setEventSystem(new EventSystemImpl(library.getEventLibrary(), networkSystem));
        CoreRegistry.put(EntityManager.class, entityManager);
        CoreRegistry.put(EventSystem.class, entityManager.getEventSystem());

        registerComponents(library.getComponentLibrary(), moduleManager);
        registerEvents(entityManager.getEventSystem(), moduleManager);
        return entityManager;
    }

    private TypeHandlerLibrary buildTypeLibrary(PojoEntityManager entityManager) {
        Vector3iTypeHandler vector3iHandler = new Vector3iTypeHandler();
        return new TypeHandlerLibraryBuilder()
                .add(BlockFamily.class, new BlockFamilyTypeHandler())
                .add(Block.class, new BlockTypeHandler())
                .add(Color4f.class, new Color4fTypeHandler())
                .add(Quat4f.class, new Quat4fTypeHandler())
                .add(Texture.class, new AssetTypeHandler<>(AssetType.TEXTURE, Texture.class))
                .add(Mesh.class, new AssetTypeHandler<>(AssetType.MESH, Mesh.class))
                .add(Sound.class, new AssetTypeHandler<>(AssetType.SOUND, Sound.class))
                .add(Material.class, new AssetTypeHandler<>(AssetType.MATERIAL, Material.class))
                .add(SkeletalMesh.class, new AssetTypeHandler<>(AssetType.SKELETON_MESH, SkeletalMesh.class))
                .add(MeshAnimation.class, new AssetTypeHandler<>(AssetType.ANIMATION, MeshAnimation.class))
                .add(Vector4f.class, new Vector4fTypeHandler())
                .add(Vector3f.class, new Vector3fTypeHandler())
                .add(Vector2f.class, new Vector2fTypeHandler())
                .add(Vector3i.class, vector3iHandler)
                .add(CollisionGroup.class, new CollisionGroupTypeHandler())
                .add(Region3i.class, new Region3iTypeHandler(vector3iHandler))
                .add(EntityRef.class, new EntityRefTypeHandler(entityManager))
                .add(Prefab.class, new PrefabTypeHandler())
                .build();
    }

    private void registerComponents(ComponentLibrary library, ModuleManager moduleManager) {
        Reflections reflections = moduleManager.getActiveModuleReflections();

        Set<Class<? extends Component>> componentTypes = reflections.getSubTypesOf(Component.class);
        for (Class<? extends Component> componentType : componentTypes) {
            if (componentType.getAnnotation(DoNotAutoRegister.class) == null) {
                library.register(componentType);
            }
        }
    }

    private void registerEvents(EventSystem eventSystem, ModuleManager moduleManager) {
        for (Module module : moduleManager.getActiveModules()) {
            if (module.isCodeModule()) {
                registerEvents(module.getModuleInfo().getId(), eventSystem, module.getReflections());
            }
        }
    }

    private void registerEvents(String packageName, EventSystem eventSystem, Reflections reflections) {
        Set<Class<? extends Event>> eventTypes = reflections.getSubTypesOf(Event.class);
        for (Class<? extends Event> eventType : eventTypes) {
            if (eventType.getAnnotation(DoNotAutoRegister.class) == null) {
                eventSystem.registerEvent(packageName + ":" + eventType.getSimpleName(), eventType);
            }
        }
    }
}

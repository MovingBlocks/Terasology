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

package org.terasology.game.bootstrap;

import org.reflections.Reflections;
import org.terasology.asset.AssetType;
import org.terasology.audio.Sound;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibraryBuilder;
import org.terasology.entitySystem.metadata.extension.AssetTypeHandler;
import org.terasology.entitySystem.metadata.extension.BlockFamilyTypeHandler;
import org.terasology.entitySystem.metadata.extension.CollisionGroupTypeHandler;
import org.terasology.entitySystem.metadata.extension.Color4fTypeHandler;
import org.terasology.entitySystem.metadata.extension.EntityRefTypeHandler;
import org.terasology.entitySystem.metadata.extension.PrefabTypeHandler;
import org.terasology.entitySystem.metadata.extension.Quat4fTypeHandler;
import org.terasology.entitySystem.metadata.extension.Region3iTypeHandler;
import org.terasology.entitySystem.metadata.extension.Vector2fTypeHandler;
import org.terasology.entitySystem.metadata.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.metadata.extension.Vector3iTypeHandler;
import org.terasology.entitySystem.metadata.internal.EntitySystemLibraryImpl;
import org.terasology.entitySystem.pojo.PojoEntityManager;
import org.terasology.entitySystem.pojo.PojoEventSystem;
import org.terasology.entitySystem.pojo.PojoPrefabManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.CollisionGroup;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Color4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.util.Set;

/**
 * @author Immortius
 */
public class EntitySystemBuilder {

    public PersistableEntityManager build(ModManager modManager) {
        PojoEntityManager entityManager = new PojoEntityManager();
        TypeHandlerLibrary typeHandlerLibrary = buildTypeLibrary(entityManager);
        EntitySystemLibrary library = new EntitySystemLibraryImpl(typeHandlerLibrary);
        CoreRegistry.put(EntitySystemLibrary.class, library);
        CoreRegistry.put(ComponentLibrary.class, library.getComponentLibrary());
        CoreRegistry.put(EventLibrary.class, library.getEventLibrary());
        entityManager.setEntitySystemLibrary(library);

        PrefabManager prefabManager = new PojoPrefabManager(library.getComponentLibrary());
        entityManager.setPrefabManager(prefabManager);
        CoreRegistry.put(PrefabManager.class, prefabManager);

        entityManager.setEventSystem(new PojoEventSystem(library.getEventLibrary(), CoreRegistry.get(NetworkSystem.class)));
        CoreRegistry.put(EntityManager.class, entityManager);
        CoreRegistry.put(EventSystem.class, entityManager.getEventSystem());

        registerComponents(library.getComponentLibrary(), modManager);
        registerEvents(entityManager.getEventSystem(), modManager);
        return entityManager;
    }

    private TypeHandlerLibrary buildTypeLibrary(PojoEntityManager entityManager) {
        Vector3iTypeHandler vector3iHandler = new Vector3iTypeHandler();
        return new TypeHandlerLibraryBuilder()
                .add(BlockFamily.class, new BlockFamilyTypeHandler())
                .add(Color4f.class, new Color4fTypeHandler())
                .add(Quat4f.class, new Quat4fTypeHandler())
                .add(Mesh.class, new AssetTypeHandler<Mesh>(AssetType.MESH, Mesh.class))
                .add(Sound.class, new AssetTypeHandler<Sound>(AssetType.SOUND, Sound.class))
                .add(Material.class, new AssetTypeHandler<Material>(AssetType.MATERIAL, Material.class))
                .add(SkeletalMesh.class, new AssetTypeHandler<SkeletalMesh>(AssetType.SKELETON_MESH, SkeletalMesh.class))
                .add(MeshAnimation.class, new AssetTypeHandler<MeshAnimation>(AssetType.ANIMATION, MeshAnimation.class))
                .add(Vector3f.class, new Vector3fTypeHandler())
                .add(Vector2f.class, new Vector2fTypeHandler())
                .add(Vector3i.class, vector3iHandler)
                .add(CollisionGroup.class, new CollisionGroupTypeHandler())
                .add(Region3i.class, new Region3iTypeHandler(vector3iHandler))
                .add(EntityRef.class, new EntityRefTypeHandler(entityManager))
                .add(Prefab.class, new PrefabTypeHandler())
                .build();
    }

    private void registerComponents(ComponentLibrary library, ModManager modManager) {
        Reflections reflections = modManager.getActiveModReflections();

        Set<Class<? extends Component>> componentTypes = reflections.getSubTypesOf(Component.class);
        for (Class<? extends Component> componentType : componentTypes) {
            library.register(componentType);
        }
    }

    private void registerEvents(EventSystem eventSystem, ModManager modManager) {
        registerEvents(ModManager.ENGINE_PACKAGE, eventSystem, modManager.getEngineReflections());
        for (Mod mod : modManager.getActiveMods()) {
            if (mod.isCodeMod()) {
                registerEvents(mod.getModInfo().getId(), eventSystem, mod.getReflections());
            }
        }
    }

    private void registerEvents(String packageName, EventSystem eventSystem, Reflections reflections) {
        Set<Class<? extends Event>> eventTypes = reflections.getSubTypesOf(Event.class);
        for (Class<? extends Event> eventType : eventTypes) {
            eventSystem.registerEvent(packageName + ":" + eventType.getSimpleName(), eventType);
        }
    }
}

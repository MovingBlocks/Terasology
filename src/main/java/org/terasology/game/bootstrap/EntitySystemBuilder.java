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

import java.util.Set;

import javax.vecmath.Color4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.terasology.asset.AssetType;
import org.terasology.audio.Sound;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentLibraryImpl;
import org.terasology.entitySystem.metadata.extension.AssetTypeHandler;
import org.terasology.entitySystem.metadata.extension.BlockFamilyTypeHandler;
import org.terasology.entitySystem.metadata.extension.CollisionGroupTypeHandler;
import org.terasology.entitySystem.metadata.extension.Color4fTypeHandler;
import org.terasology.entitySystem.metadata.extension.Quat4fTypeHandler;
import org.terasology.entitySystem.metadata.extension.Vector2fTypeHandler;
import org.terasology.entitySystem.metadata.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.metadata.extension.Vector3iTypeHandler;
import org.terasology.entitySystem.pojo.PojoEntityManager;
import org.terasology.entitySystem.pojo.PojoEventSystem;
import org.terasology.entitySystem.pojo.PojoPrefabManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.math.Vector3i;
import org.terasology.physics.CollisionGroup;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.world.block.family.BlockFamily;

/**
 * @author Immortius
 */
public class EntitySystemBuilder {

    public PersistableEntityManager build(ModManager modManager) {
        ComponentLibrary library = new ComponentLibraryImpl();
        registerTypeHandlers(library);
        CoreRegistry.put(ComponentLibrary.class, library);

        PrefabManager prefabManager = new PojoPrefabManager(library);
        CoreRegistry.put(PrefabManager.class, prefabManager);

        PersistableEntityManager entityManager = new PojoEntityManager(library, prefabManager);
        entityManager.setEventSystem(new PojoEventSystem(entityManager));
        CoreRegistry.put(EntityManager.class, entityManager);
        CoreRegistry.put(EventSystem.class, entityManager.getEventSystem());

        registerComponents(library, modManager);
        registerEvents(entityManager.getEventSystem(), modManager);
        return entityManager;
    }

    private void registerTypeHandlers(ComponentLibrary library) {
        library.registerTypeHandler(BlockFamily.class, new BlockFamilyTypeHandler());
        library.registerTypeHandler(Color4f.class, new Color4fTypeHandler());
        library.registerTypeHandler(Quat4f.class, new Quat4fTypeHandler());
        library.registerTypeHandler(Mesh.class, new AssetTypeHandler(AssetType.MESH, Mesh.class));
        library.registerTypeHandler(Sound.class, new AssetTypeHandler(AssetType.SOUND, Sound.class));
        library.registerTypeHandler(Material.class, new AssetTypeHandler(AssetType.MATERIAL, Material.class));
        library.registerTypeHandler(SkeletalMesh.class, new AssetTypeHandler(AssetType.SKELETON_MESH, SkeletalMesh.class));
        library.registerTypeHandler(MeshAnimation.class, new AssetTypeHandler(AssetType.ANIMATION, MeshAnimation.class));
        library.registerTypeHandler(Vector3f.class, new Vector3fTypeHandler());
        library.registerTypeHandler(Vector2f.class, new Vector2fTypeHandler());
        library.registerTypeHandler(Vector3i.class, new Vector3iTypeHandler());
        library.registerTypeHandler(CollisionGroup.class, new CollisionGroupTypeHandler());
    }

    private void registerComponents(ComponentLibrary library, ModManager modManager) {
        Reflections reflections = modManager.getActiveModReflections();

        Set<Class<? extends Component>> componentTypes = reflections.getSubTypesOf(Component.class);
        for (Class<? extends Component> componentType : componentTypes) {
            library.registerComponentClass(componentType);
        }
    }

    private void registerEvents(EventSystem eventSystem, ModManager modManager) {
        registerEvents(ModManager.ENGINE_PACKAGE, eventSystem, modManager.getEngineReflections());
        for (Mod mod : modManager.getActiveMods()) {
            registerEvents(mod.getModInfo().getId(), eventSystem, mod.getReflections());
        }
    }

    private void registerEvents(String packageName, EventSystem eventSystem, Reflections reflections) {
        Set<Class<? extends Event>> eventTypes = reflections.getSubTypesOf(Event.class);
        for (Class<? extends Event> eventType : eventTypes) {
            eventSystem.registerEvent(packageName + ":" + eventType.getSimpleName(), eventType);
        }
    }
}

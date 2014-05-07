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

import com.google.common.collect.ListMultimap;

import org.terasology.asset.AssetType;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StreamingSound;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.event.internal.EventSystemImpl;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.MetadataUtil;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.entitySystem.systems.internal.DoNotAutoRegister;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.NodesClassLibrary;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.AssetTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.BlockFamilyTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.BlockTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.CollisionGroupTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.ColorTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.EntityRefTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.PrefabTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.TextureRegionTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Quat4fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Region3iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4fTypeHandler;
import org.terasology.physics.CollisionGroup;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.properties.OneOfProviderFactory;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import java.util.Map;

/**
 * @author Immortius
 */
// TODO: Review - This could be a static class but its existence is also questionable.
public class EntitySystemBuilder {

    public EngineEntityManager build(ModuleManager moduleManager, NetworkSystem networkSystem, ReflectFactory reflectFactory) {
        return build(moduleManager, networkSystem, reflectFactory, new CopyStrategyLibrary(reflectFactory));
    }

    public EngineEntityManager build(ModuleManager moduleManager, NetworkSystem networkSystem, ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategyLibrary) {
        // Entity Manager
        PojoEntityManager entityManager = CoreRegistry.put(EntityManager.class, new PojoEntityManager());
        CoreRegistry.put(EngineEntityManager.class, entityManager);

        // Standard serialization library
        TypeSerializationLibrary typeSerializationLibrary = buildTypeLibrary(entityManager, reflectFactory, copyStrategyLibrary);
        entityManager.setTypeSerializerLibrary(typeSerializationLibrary);

        // Entity System Library
        EntitySystemLibrary library = CoreRegistry.put(EntitySystemLibrary.class, new EntitySystemLibrary(reflectFactory, copyStrategyLibrary, typeSerializationLibrary));
        entityManager.setEntitySystemLibrary(library);
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
        nodesClassLibrary.scan(moduleManager);

        registerComponents(library.getComponentLibrary(), moduleManager);
        registerEvents(entityManager.getEventSystem(), moduleManager);
        return entityManager;
    }

    private TypeSerializationLibrary buildTypeLibrary(PojoEntityManager entityManager, ReflectFactory factory, CopyStrategyLibrary copyStrategies) {
        TypeSerializationLibrary serializationLibrary = new TypeSerializationLibrary(factory, copyStrategies);
        serializationLibrary.add(BlockFamily.class, new BlockFamilyTypeHandler());
        serializationLibrary.add(Block.class, new BlockTypeHandler());
        serializationLibrary.add(Color.class, new ColorTypeHandler());
        serializationLibrary.add(Quat4f.class, new Quat4fTypeHandler());
        serializationLibrary.add(Texture.class, new AssetTypeHandler<>(AssetType.TEXTURE, Texture.class));
        serializationLibrary.add(UIElement.class, new AssetTypeHandler<>(AssetType.UI_ELEMENT, UIElement.class));
        serializationLibrary.add(Mesh.class, new AssetTypeHandler<>(AssetType.MESH, Mesh.class));
        serializationLibrary.add(StaticSound.class, new AssetTypeHandler<>(AssetType.SOUND, StaticSound.class));
        serializationLibrary.add(StreamingSound.class, new AssetTypeHandler<>(AssetType.MUSIC, StreamingSound.class));
        serializationLibrary.add(Material.class, new AssetTypeHandler<>(AssetType.MATERIAL, Material.class));
        serializationLibrary.add(SkeletalMesh.class, new AssetTypeHandler<>(AssetType.SKELETON_MESH, SkeletalMesh.class));
        serializationLibrary.add(MeshAnimation.class, new AssetTypeHandler<>(AssetType.ANIMATION, MeshAnimation.class));
        serializationLibrary.add(TextureRegion.class, new TextureRegionTypeHandler());
        serializationLibrary.add(TextureRegionAsset.class, new TextureRegionTypeHandler());
        serializationLibrary.add(Vector4f.class, new Vector4fTypeHandler());
        serializationLibrary.add(Vector3f.class, new Vector3fTypeHandler());
        serializationLibrary.add(Vector2f.class, new Vector2fTypeHandler());
        serializationLibrary.add(Vector3i.class, new Vector3iTypeHandler());
        serializationLibrary.add(CollisionGroup.class, new CollisionGroupTypeHandler());
        serializationLibrary.add(Region3i.class, new Region3iTypeHandler());
        serializationLibrary.add(EntityRef.class, new EntityRefTypeHandler(entityManager));
        serializationLibrary.add(Prefab.class, new PrefabTypeHandler());
        serializationLibrary.add(BehaviorTree.class, new AssetTypeHandler<>(AssetType.BEHAVIOR, BehaviorTree.class));
        return serializationLibrary;
    }

    private void registerComponents(ComponentLibrary library, ModuleManager moduleManager) {
        for (Module module : moduleManager.getActiveCodeModules()) {
            for (Class<? extends Component> componentType : module.getReflections().getSubTypesOf(Component.class)) {
                if (componentType.getAnnotation(DoNotAutoRegister.class) == null) {
                    String componentName = MetadataUtil.getComponentClassName(componentType);
                    library.register(new SimpleUri(module.getId(), componentName), componentType);
                }
            }
        }
    }

    private void registerEvents(EventSystem eventSystem, ModuleManager moduleManager) {
        ListMultimap<String, Class<? extends Event>> allSubclassesOf = moduleManager.findAllSubclassesOf(Event.class);
        for (Map.Entry<String, Class<? extends Event>> entry : allSubclassesOf.entries()) {
            if (entry.getValue().getAnnotation(DoNotAutoRegister.class) == null) {
                eventSystem.registerEvent(new SimpleUri(entry.getKey(), entry.getValue().getSimpleName()), entry.getValue());
            }
        }
    }

}

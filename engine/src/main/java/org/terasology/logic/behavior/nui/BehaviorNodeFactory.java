/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.behavior.nui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.engine.ComponentFieldUri;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.logic.behavior.core.BehaviorTreeBuilder;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.properties.OneOfProviderFactory;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Factory to create node instances for BehaviorNodeComponents.
 *
 * @author synopia
 */
@RegisterSystem
public class BehaviorNodeFactory extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorNodeFactory.class);

    private List<BehaviorNodeComponent> nodeComponents = Lists.newArrayList();
    private Map<String, List<BehaviorNodeComponent>> categoryComponents = Maps.newHashMap();
    private List<String> categories;

    @In
    private ModuleManager moduleManager;
    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;
    @In
    private AssetManager assetManager;
    @In
    private OneOfProviderFactory providerFactory;
    @In
    private ComponentLibrary componentLibrary;
    @In
    private BehaviorTreeBuilder treeBuilder;

    private List<AssetUri> sounds = Lists.newArrayList();
    private List<AssetUri> music = Lists.newArrayList();

    public BehaviorNodeFactory() {
        CoreRegistry.put(BehaviorNodeFactory.class, this);
    }

    @Override
    public void postBegin() {
        refreshLibrary();
    }

    public void refreshLibrary() {
        for (AssetUri uri : assetManager.listAssets(AssetType.SOUND)) {
            sounds.add(uri);
        }
        for (AssetUri uri : assetManager.listAssets(AssetType.MUSIC)) {
            music.add(uri);
        }
        providerFactory.register("sounds", new ReadOnlyBinding<List<AssetUri>>() {
                    @Override
                    public List<AssetUri> get() {
                        return sounds;
                    }
                }, new StringTextRenderer<AssetUri>() {
                    @Override
                    public String getString(AssetUri value) {
                        return value.getAssetName() != null ? value.getAssetName().toString() : "";
                    }
                }
        );
        providerFactory.register("music", new ReadOnlyBinding<List<AssetUri>>() {
                    @Override
                    public List<AssetUri> get() {
                        return music;
                    }
                }, new StringTextRenderer<AssetUri>() {
                    @Override
                    public String getString(AssetUri value) {
                        return value.getAssetName().toString();
                    }
                }
        );
        providerFactory.register("animations", new AnimationPoolUriBinding(),
                new StringTextRenderer<ComponentFieldUri>() {
                    @Override
                    public String getString(ComponentFieldUri value) {
                        return value.toString();
                    }
                }
        );
        refreshPrefabs();
        sortLibrary();
    }

    private List<ComponentFieldUri> determineAnimationPoolUris() {
        final List<ComponentFieldUri> animationSetUris = Lists.newArrayList();
        for (ComponentMetadata<?> componentMetadata : componentLibrary.iterateComponentMetadata()) {
            SimpleUri uri = componentMetadata.getUri();

            for (FieldMetadata<?, ?> fieldMetadata : componentMetadata.getFields()) {
                if (fieldMetadata.getType().isAssignableFrom(List.class)) {
                    Type fieldType = fieldMetadata.getField().getGenericType();
                    if (fieldType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) fieldType;
                        Type[] typeArguments = parameterizedType.getActualTypeArguments();
                        if (typeArguments.length == 1) {
                            Class<?> typeClass = ReflectionUtil.getClassOfType(typeArguments[0]);
                            if (typeClass.isAssignableFrom(MeshAnimation.class)) {
                                animationSetUris.add(new ComponentFieldUri(uri, fieldMetadata.getName()));
                            }
                        }
                    }
                }
            }
        }
        return animationSetUris;
    }

    private void sortLibrary() {
        categories = Lists.newArrayList(categoryComponents.keySet());
        Collections.sort(categories);
        for (String category : categories) {
            Collections.sort(categoryComponents.get(category), new Comparator<BehaviorNodeComponent>() {
                @Override
                public int compare(BehaviorNodeComponent o1, BehaviorNodeComponent o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
        }
    }

    private void refreshPrefabs() {
        Collection<Prefab> prefabs = prefabManager.listPrefabs(BehaviorNodeComponent.class);
        if (prefabs.size() == 0) {
            // called from main menu
            List<String> nodes = Arrays.asList(
                    "counter", "timer", "loop", "lookup", "dynselector",
                    "fail", "parallel", "playMusic", "playSound", "running", "selector", "setAnimation", "sequence", "succeed"
            );
            prefabs = Lists.newArrayList();
            for (String node : nodes) {
                prefabs.add(prefabManager.getPrefab("engine:" + node));
            }
        }
        for (Prefab prefab : prefabs) {
            EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
            entityBuilder.setPersistent(false);
            EntityRef entityRef = entityBuilder.build();
            BehaviorNodeComponent component = entityRef.getComponent(BehaviorNodeComponent.class);
            addToCategory(component);
            nodeComponents.add(component);
        }
    }

    private void addToCategory(BehaviorNodeComponent component) {
        List<BehaviorNodeComponent> list = categoryComponents.get(component.category);
        if (list == null) {
            list = Lists.newArrayList();
            categoryComponents.put(component.category, list);
        }
        list.add(component);
    }

    public BehaviorNodeComponent getNodeComponent(BehaviorNode node) {
        for (BehaviorNodeComponent nodeComponent : nodeComponents) {
            if (node.getName().equals(nodeComponent.name)) {
                return nodeComponent;
            }
        }
        return BehaviorNodeComponent.DEFAULT;
    }

    public BehaviorNode createNode(BehaviorNodeComponent nodeComponent) {
        return treeBuilder.fromJson(nodeComponent.action);
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<BehaviorNodeComponent> getNodesComponents(String category) {
        return categoryComponents.get(category);
    }

    private class AnimationPoolUriBinding extends ReadOnlyBinding<List<ComponentFieldUri>> {
        private List<ComponentFieldUri> list;

        @Override
        public List<ComponentFieldUri> get() {
            if (list == null) {
                list = Collections.unmodifiableList(determineAnimationPoolUris());
            }
            return list;
        }
    }
}

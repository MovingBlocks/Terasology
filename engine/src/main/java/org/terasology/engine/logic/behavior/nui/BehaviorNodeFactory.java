// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.nui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.core.ComponentFieldUri;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.core.BehaviorTreeBuilder;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.properties.OneOfProviderFactory;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.assets.animation.MeshAnimation;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.ReflectionUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory to create node instances for BehaviorNodeComponents.
 *
 */
@RegisterSystem
@Share(BehaviorNodeFactory.class)
public class BehaviorNodeFactory extends BaseComponentSystem {

    private static final Comparator<BehaviorNodeComponent> COMPARE_BY_NAME = Comparator.comparing(o -> o.name);

    private List<BehaviorNodeComponent> nodeComponents = Lists.newArrayList();
    private Map<String, List<BehaviorNodeComponent>> categoryComponents = Maps.newHashMap();
    private List<String> categories;

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

    private List<ResourceUrn> sounds = Lists.newArrayList();
    private List<ResourceUrn> music = Lists.newArrayList();

    @Override
    public void postBegin() {
        refreshLibrary();
    }

    public void refreshLibrary() {
        sounds.addAll(assetManager.getAvailableAssets(StaticSound.class).stream().collect(Collectors.toList()));
        music.addAll(assetManager.getAvailableAssets(StreamingSound.class).stream().collect(Collectors.toList()));
        providerFactory.register("sounds", new ReadOnlyBinding<List<ResourceUrn>>() {
            @Override
            public List<ResourceUrn> get() {
                return sounds;
            }
        }, new StringTextRenderer<ResourceUrn>() {
            @Override
            public String getString(ResourceUrn value) {
                return value.getResourceName().toString();
            }
        });
        providerFactory.register("music", new ReadOnlyBinding<List<ResourceUrn>>() {
            @Override
            public List<ResourceUrn> get() {
                return music;
            }
        }, new StringTextRenderer<ResourceUrn>() {
            @Override
            public String getString(ResourceUrn value) {
                return value.getResourceName().toString();
            }
        });
        providerFactory.register("animations", new AnimationPoolUriBinding(),
                new StringTextRenderer<ComponentFieldUri>() {
                    @Override
                    public String getString(ComponentFieldUri value) {
                        return value.toString();
                    }
                });
        refreshPrefabs();
        sortLibrary();
    }

    private List<ComponentFieldUri> determineAnimationPoolUris() {
        final List<ComponentFieldUri> animationSetUris = Lists.newArrayList();
        for (ComponentMetadata<?> componentMetadata : componentLibrary.iterateComponentMetadata()) {
            SimpleUri uri = new SimpleUri(componentMetadata.getId().toString());

            for (FieldMetadata<?, ?> fieldMetadata : componentMetadata.getFields()) {
                if (fieldMetadata.getType().isAssignableFrom(List.class)) {
                    Type fieldType = fieldMetadata.getField().getGenericType();
                    if (fieldType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) fieldType;
                        Type[] typeArguments = parameterizedType.getActualTypeArguments();
                        if (typeArguments.length == 1) {
                            Class<?> typeClass = ReflectionUtil.getRawType(typeArguments[0]);
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
            Collections.sort(categoryComponents.get(category), COMPARE_BY_NAME);
        }
    }

    private void refreshPrefabs() {
        Collection<Prefab> prefabs = prefabManager.listPrefabs(BehaviorNodeComponent.class);
        if (prefabs.size() == 0) {
            // called from main menu
            List<String> nodes = Arrays.asList(
                    "counter", "timer", "loop", "lookup", "dynselector",
                    "fail", "parallel", "playMusic", "playSound", "running", "selector", "setAnimation", "sequence", "succeed");
            prefabs = Lists.newArrayList();
            for (String node : nodes) {
                prefabs.add(Assets.get(new ResourceUrn("engine:" + node), Prefab.class).orElse(null));
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

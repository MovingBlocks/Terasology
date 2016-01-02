/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.behavior;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StreamingSound;
import org.terasology.engine.ComponentFieldUri;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.behavior.asset.NodesClassLibrary;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.properties.OneOfProviderFactory;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory to create node instances from node entities.
 *
 */
@RegisterSystem
@Share(BehaviorNodeFactory.class)
public class BehaviorNodeFactory extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorNodeFactory.class);

    private Map<ClassMetadata<? extends Node, ?>, BehaviorNodeComponent> nodes = Maps.newHashMap();
    private Map<String, List<BehaviorNodeComponent>> categoryComponents = Maps.newHashMap();
    private List<String> categories;

    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;
    @In
    private AssetManager assetManager;
    @In
    private NodesClassLibrary nodesClassLibrary;
    @In
    private OneOfProviderFactory providerFactory;

    @In
    private ComponentLibrary componentLibrary;

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
                }
        );
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
            Collections.sort(categoryComponents.get(category), (o1, o2) -> o1.name.compareTo(o2.name));
        }
    }

    private void refreshPrefabs() {
        Collection<Prefab> prefabs = prefabManager.listPrefabs(BehaviorNodeComponent.class);
        for (Prefab prefab : prefabs) {
            EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
            entityBuilder.setPersistent(false);
            EntityRef entityRef = entityBuilder.build();
            BehaviorNodeComponent component = entityRef.getComponent(BehaviorNodeComponent.class);
            ClassMetadata<? extends Node, ?> classMetadata = nodesClassLibrary.resolve(component.type);
            if (classMetadata != null) {
                if (classMetadata.isConstructable()) {
                    nodes.put(classMetadata, component);
                    logger.debug("Found behavior node for class " + component.type + " name=" + component.name);
                    List<BehaviorNodeComponent> list = categoryComponents.get(component.category);
                    if (list == null) {
                        list = Lists.newArrayList();
                        categoryComponents.put(component.category, list);
                    }
                    list.add(component);
                } else {
                    logger.warn("Node cannot be constructed! -> ignoring " + component.type + " name=" + component.name);
                }
            } else {
                logger.warn("Node not found -> ignoring! " + component.type + " name=" + component.name);
            }
        }
    }

    public BehaviorNodeComponent getNodeComponent(Node node) {
        ClassMetadata<? extends Node, ?> metadata = nodesClassLibrary.getMetadata(node.getClass());
        if (metadata != null) {
            BehaviorNodeComponent nodeComponent = nodes.get(metadata);
            if (nodeComponent == null) {
                return BehaviorNodeComponent.DEFAULT;
            }
            return nodeComponent;
        } else {
            return null;
        }
    }

    public Node getNode(BehaviorNodeComponent nodeComponent) {
        for (Map.Entry<ClassMetadata<? extends Node, ?>, BehaviorNodeComponent> entry : nodes.entrySet()) {
            if (nodeComponent == entry.getValue()) {
                return entry.getKey().newInstance();
            }
        }
        return null;
    }

    public Collection<BehaviorNodeComponent> getNodeComponents() {
        return nodes.values();
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

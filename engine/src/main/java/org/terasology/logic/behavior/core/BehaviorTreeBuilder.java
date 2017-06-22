/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.behavior.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.behavior.ActionName;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.nui.BehaviorNodeComponent;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.TableModuleRegistry;
import org.terasology.naming.Name;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.registry.InjectionHelper;
import org.terasology.utilities.gson.UriTypeAdapterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON deserializer to create trees using json:
 * <p/>
 * { sequence:[action1, {action2:{ foo:bar, duration:7}}] }
 * <p/>
 * Actions and Decorators need to be registered before parsing.
 */
public class BehaviorTreeBuilder implements JsonDeserializer<BehaviorNode>, JsonSerializer<BehaviorNode> {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorTreeBuilder.class);

    private Map<String, Class<? extends Action>> actions = Maps.newHashMap();
    private Map<String, Class<? extends Action>> decorators = Maps.newHashMap();


    @In
    private PrefabManager prefabManager;

    private Gson gson;

    private int nextId = 1;

    public BehaviorTreeBuilder() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        if (moduleManager != null) {
            ModuleEnvironment environment = moduleManager.getEnvironment();

            Set<Module> behavior = new HashSet<>();
            behavior.add(moduleManager.getRegistry().getLatestModuleVersion(new Name("Behaviors")));
            ModuleEnvironment behaviorEnvironment =  moduleManager.loadEnvironment(behavior, false);




            // TODO This is here until we can move everything over to Behaviors
            for (Class<? extends Action> type : behaviorEnvironment.getSubtypesOf(Action.class)) {
                ActionName actionName = type.getAnnotation(ActionName.class);
                if (actionName != null) {
                    String name = actionName.value();
                    if (actionName.isDecorator()) {
                        registerDecorator(name, type);
                        logger.info("Found decorator {}", name);
                    } else {
                        registerAction(name, type);
                        logger.info("Found action {}", name);
                    }
                }
            }


            for (Class<? extends Action> type : environment.getSubtypesOf(Action.class)) {
                ActionName actionName = type.getAnnotation(ActionName.class);
                if (actionName != null) {
                    String name = actionName.value();
                    if (actionName.isDecorator()) {
                        registerDecorator(name, type);
                        logger.info("Found decorator {}", name);
                    } else {
                        registerAction(name, type);
                        logger.info("Found action {}", name);
                    }
                }
            }
        }
    }

    public BehaviorNode fromJson(String json) {
        initGson();
        return gson.fromJson(json, BehaviorNode.class);
    }

    public String toJson(BehaviorNode node) {
        initGson();
        return gson.toJson(node);
    }

    public BehaviorNode fromJson(InputStream json) {
        initGson();
        return gson.fromJson(new InputStreamReader(json), BehaviorNode.class);
    }

    public void registerAction(String name, Class<? extends Action> action) {
        actions.put(name, action);
        gson = null;
    }

    public void registerDecorator(String name, Class<? extends Action> action) {
        decorators.put(name, action);
        gson = null;
    }

    private void initGson() {
        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeHierarchyAdapter(BehaviorNode.class, this);
            gsonBuilder.registerTypeAdapterFactory(new UriTypeAdapterFactory());
            gsonBuilder.registerTypeAdapter(BehaviorTree.class, new TypeAdapter<BehaviorTree>() {
                @Override
                public void write(JsonWriter out, BehaviorTree value) throws IOException {
                    if (value != null) {
                        // TODO doublecheck URN
                        out.value(value.getUrn().toString());
                    } else {
                        out.value("");
                    }
                }

                @Override
                public BehaviorTree read(JsonReader in) throws IOException {
                    String uri = in.nextString();
                    return CoreRegistry.get(AssetManager.class).getAsset(new ResourceUrn(uri), BehaviorTree.class).get();
                }
            });
            gson = gsonBuilder.create();
        }
    }

    @Override
    public BehaviorNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        BehaviorNode node;
        if (json.isJsonPrimitive()) {
            node = getPrimitiveNode(json, context);
        } else {
            node = getCompositeNode(json, context);
        }
        node = createNode(node);
        return node;
    }

    @Override
    public JsonElement serialize(BehaviorNode src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = new JsonObject();
        if (src instanceof DelegateNode) {
            DelegateNode delegateNode = (DelegateNode) src;
            return serialize(delegateNode.delegate, BehaviorNode.class, context);
        }
        if (src instanceof CompositeNode) {
            String name = src.getName();
            JsonArray array = new JsonArray();
            for (int i = 0; i < src.getChildrenCount(); i++) {
                array.add(serialize(src.getChild(i), BehaviorNode.class, context));
            }
            node.add(name, array);
        } else if (src instanceof ActionNode) {
            ActionNode actionNode = (ActionNode) src;
            JsonObject content;
            String name;
            if (actionNode.action != null) {
                content = (JsonObject) context.serialize(actionNode.action);
                name = actionNode.action.getName();
            } else {
                content = new JsonObject();
                name = actionNode.getName();
            }

            if (src instanceof DecoratorNode) {
                DecoratorNode decoratorNode = (DecoratorNode) src;
                if (decoratorNode.getChildrenCount() > 0) {
                    content.add("child", serialize(decoratorNode.getChild(0), BehaviorNode.class, context));
                }
            }

            node.add(name, content);
        } else {
            return new JsonPrimitive(src.getName());
        }
        return node;
    }

    public BehaviorNode createNode(BehaviorNode node) {
        return node;
    }

    private BehaviorNode getPrimitiveNode(JsonElement json, JsonDeserializationContext context) {
        String type = json.getAsString();
        BehaviorNode node = createNode(type);
        if (actions.containsKey(type)) {
            Action action = context.deserialize(new JsonObject(), actions.get(type));
            addAction((ActionNode) node, action);
        } else if (decorators.containsKey(type)) {
            Action action = context.deserialize(new JsonObject(), decorators.get(type));
            addAction((ActionNode) node, action);
        }
        return node;
    }

    private void addAction(ActionNode node, Action action) {
        action.setId(nextId);
        nextId++;
        node.setAction(action);
        InjectionHelper.inject(action);
        action.setup();
    }

    private BehaviorNode getCompositeNode(JsonElement json, JsonDeserializationContext context) {
        String type;
        JsonObject obj = json.getAsJsonObject();
        Map.Entry<String, JsonElement> entry = obj.entrySet().iterator().next();
        type = entry.getKey();
        JsonElement jsonElement = entry.getValue();

        BehaviorNode node = createNode(type);

        if (actions.containsKey(type)) {
            Action action = context.deserialize(jsonElement, actions.get(type));
            addAction((ActionNode) node, action);
        } else if (decorators.containsKey(type)) {
            Action action = context.deserialize(jsonElement, decorators.get(type));
            addAction((ActionNode) node, action);
            JsonElement childJson = jsonElement.getAsJsonObject().get("child");
            BehaviorNode child = context.deserialize(childJson, BehaviorNode.class);
            node.insertChild(0, child);
        } else if (jsonElement.isJsonArray()) {
            List<BehaviorNode> children = context.deserialize(jsonElement, new TypeToken<List<BehaviorNode>>() {
            }.getType());
            for (int i = 0; i < children.size(); i++) {
                BehaviorNode child = children.get(i);
                node.insertChild(i, child);
            }
        }

        return node;
    }

    private BehaviorNode createNode(String type) {
        switch (type) {
            case "sequence":
                return new SequenceNode();
            case "selector":
                return new SelectorNode();
            case "dynamic":
                return new DynamicSelectorNode();
            case "parallel":
                return new ParallelNode();
            case "failure":
                return new FailureNode();
            case "success":
                return new SuccessNode();
            case "running":
                return new RunningNode();
            case "action":
                return new ActionNode();
            case "decorator":
                return new DecoratorNode();
            default:
                if (actions.containsKey(type)) {
                    return new ActionNode();
                }
                if (decorators.containsKey(type)) {
                    return new DecoratorNode();
                }
                throw new IllegalArgumentException("Unknown behavior node type " + type);
        }
    }
}

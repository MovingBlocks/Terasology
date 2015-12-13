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
package org.terasology.persistence.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import gnu.trove.list.TByteList;
import gnu.trove.list.array.TByteArrayList;
import org.terasology.protobuf.EntityData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;

/**
 * Converts between the EntityData types and JSON.
 * <br><br>
 * This means that serialization between JSON and Entities/Prefabs is a two step process, with EntityData as an
 * intermediate step - it was done this way because it is much simpler to write gson handlers for the small number of
 * EntityData types than to dynamically build handlers for every component type (and have gson properly handle missing
 * types). This can be revisited in the future.
 *
 */
// TODO: More javadoc
public final class EntityDataJSONFormat {

    private EntityDataJSONFormat() {
    }

    public static void write(EntityData.GlobalStore world, BufferedWriter writer) {
        newGson().toJson(world, writer);
    }

    public static void write(EntityData.Prefab prefab, BufferedWriter writer) {
        newGson().toJson(prefab, writer);
    }

    public static String write(EntityData.Entity entity) {
        return newGson().toJson(entity);
    }

    public static EntityData.GlobalStore readWorld(BufferedReader reader) throws IOException {
        try {
            return newGson().fromJson(reader, EntityData.GlobalStore.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("Failed to load world", e);
        }
    }

    public static EntityData.Prefab readPrefab(BufferedReader reader) throws IOException {
        try {
            return newGson().fromJson(reader, EntityData.Prefab.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("Failed to load prefab", e);
        }
    }

    private static Gson newGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(EntityData.GlobalStore.class, new WorldHandler())
                .registerTypeAdapter(EntityData.Entity.class, new EntityHandler())
                .registerTypeAdapter(EntityData.Prefab.class, new PrefabHandler())
                .registerTypeAdapter(EntityData.Component.class, new ComponentHandler())
                .registerTypeAdapter(EntityData.Component.Builder.class, new ComponentBuilderHandler())
                .registerTypeAdapter(EntityData.Value.class, new ValueHandler())
                .create();
    }

    private static class WorldHandler implements JsonSerializer<EntityData.GlobalStore>, JsonDeserializer<EntityData.GlobalStore> {

        @Override
        public JsonElement serialize(EntityData.GlobalStore src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            for (Map.Entry<Descriptors.FieldDescriptor, Object> field : src.getAllFields().entrySet()) {
                result.add(field.getKey().getName(), context.serialize(field.getValue()));
            }
            return result;
        }

        @Override
        public EntityData.GlobalStore deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityData.GlobalStore.Builder world = EntityData.GlobalStore.newBuilder();
            if (json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                JsonArray prefabArray = jsonObject.getAsJsonArray("prefab");
                if (prefabArray != null) {
                    for (JsonElement prefabElem : prefabArray) {
                        world.addPrefab((EntityData.Prefab) context.deserialize(prefabElem, EntityData.Prefab.class));
                    }
                }
                JsonArray entityArray = jsonObject.getAsJsonArray("entity");
                if (entityArray != null) {
                    for (JsonElement entityElem : entityArray) {
                        world.addEntity((EntityData.Entity) context.deserialize(entityElem, EntityData.Entity.class));
                    }
                }
                JsonPrimitive nextId = jsonObject.getAsJsonPrimitive("next_entity_id");
                if (nextId != null) {
                    world.setNextEntityId(nextId.getAsInt());
                }

            }
            return world.build();
        }
    }

    private static class ComponentHandler implements JsonSerializer<EntityData.Component> {

        @Override
        public JsonElement serialize(EntityData.Component src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            for (EntityData.NameValue field : src.getFieldList()) {
                result.add(field.getName(), context.serialize(field.getValue()));
            }
            return result;
        }

    }

    private static class ComponentBuilderHandler implements JsonDeserializer<EntityData.Component.Builder> {
        @Override
        public EntityData.Component.Builder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityData.Component.Builder component = EntityData.Component.newBuilder();
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                EntityData.NameValue.Builder nameValue = EntityData.NameValue.newBuilder();
                nameValue.setName(entry.getKey());
                EntityData.Value value = context.deserialize(entry.getValue(), EntityData.Value.class);
                nameValue.setValue(value);
                component.addField(nameValue);
            }
            return component;
        }
    }

    private static class EntityHandler implements JsonSerializer<EntityData.Entity>, JsonDeserializer<EntityData.Entity> {

        @Override
        public JsonElement serialize(EntityData.Entity src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            if (src.hasId()) {
                result.addProperty("id", src.getId());
            }
            if (src.hasParentPrefab() && !src.getParentPrefab().isEmpty()) {
                result.addProperty("parentPrefab", src.getParentPrefab());
            }
            if (src.hasAlwaysRelevant()) {
                result.addProperty("alwaysRelevant", src.getAlwaysRelevant());
            }
            if (src.hasOwner()) {
                result.addProperty("owner", src.getOwner());
            }
            for (EntityData.Component component : src.getComponentList()) {
                result.add(component.getType(), context.serialize(component));
            }

            if (src.getRemovedComponentCount() > 0) {
                JsonArray removedComponentArray = new JsonArray();
                for (String removedComponent : src.getRemovedComponentList()) {
                    removedComponentArray.add(new JsonPrimitive(removedComponent));
                }
                result.add("removedComponent", removedComponentArray);
            }
            return result;
        }

        @Override
        public EntityData.Entity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
            JsonObject jsonObject = json.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String name = entry.getKey().toLowerCase(Locale.ENGLISH);
                switch (name) {
                    case "parentprefab":
                        if (entry.getValue().isJsonPrimitive()) {
                            entity.setParentPrefab(entry.getValue().getAsString());
                        }
                        break;
                    case "id":
                        if (entry.getValue().isJsonPrimitive()) {
                            entity.setId(entry.getValue().getAsInt());
                        }
                        break;
                    case "removedcomponent":
                        if (entry.getValue().isJsonArray()) {
                            for (JsonElement element : entry.getValue().getAsJsonArray()) {
                                entity.addRemovedComponent(element.getAsString());
                            }
                        }
                        break;
                    case "owner":
                        if (entry.getValue().isJsonPrimitive()) {
                            entity.setOwner(entry.getValue().getAsInt());
                        }
                        break;
                    case "alwaysrelevant":
                        entity.setAlwaysRelevant(entry.getValue().getAsBoolean());
                        break;
                    default:
                        EntityData.Component.Builder component = context.deserialize(entry.getValue(), EntityData.Component.Builder.class);
                        component.setType(entry.getKey());
                        entity.addComponent(component);
                }
            }
            return entity.build();
        }
    }

    private static class PrefabHandler implements JsonSerializer<EntityData.Prefab>, JsonDeserializer<EntityData.Prefab> {

        @Override
        public JsonElement serialize(EntityData.Prefab src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            if (src.hasName()) {
                result.addProperty("name", src.getName());
            }
            if (src.hasParentName()) {
                result.addProperty("parent", src.getParentName());
            }
            if (src.hasPersisted()) {
                result.addProperty("persisted", src.getPersisted());
            }
            if (src.hasAlwaysRelevant()) {
                result.addProperty("alwaysRelevant", src.getAlwaysRelevant());
            }
            if (src.getRemovedComponentCount() > 0) {
                result.add("removedComponents", context.serialize(src.getRemovedComponentList()));
            }
            for (EntityData.Component component : src.getComponentList()) {
                result.add(component.getType(), context.serialize(component));
            }
            return result;
        }

        @Override
        public EntityData.Prefab deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityData.Prefab.Builder prefab = EntityData.Prefab.newBuilder();
            JsonObject jsonObject = json.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String name = entry.getKey().toLowerCase(Locale.ENGLISH);
                switch (name) {
                    case "name":
                        if (entry.getValue().isJsonPrimitive()) {
                            prefab.setName(entry.getValue().getAsString());
                        }
                        break;
                    case "parent":
                        if (entry.getValue().isJsonPrimitive()) {
                            prefab.setParentName(entry.getValue().getAsString());
                        }
                        break;
                    case "removedcomponents":
                        if (entry.getValue().isJsonPrimitive()) {
                            prefab.addRemovedComponent(entry.getValue().getAsString());
                        } else if (entry.getValue().isJsonArray()) {
                            for (JsonElement element : entry.getValue().getAsJsonArray()) {
                                prefab.addRemovedComponent(element.getAsString());
                            }
                        }
                        break;
                    case "persisted":
                        prefab.setPersisted(entry.getValue().getAsBoolean());
                        break;
                    case "alwaysrelevant":
                        prefab.setAlwaysRelevant(entry.getValue().getAsBoolean());
                        break;
                    default:
                        if (entry.getValue().isJsonObject()) {
                            EntityData.Component.Builder component = context.deserialize(entry.getValue(), EntityData.Component.Builder.class);
                            component.setType(entry.getKey());
                            prefab.addComponent(component);
                        }
                }
            }
            return prefab.build();
        }
    }

    private static class ValueHandler implements JsonSerializer<EntityData.Value>, JsonDeserializer<EntityData.Value> {

        @Override
        public JsonElement serialize(EntityData.Value src, Type typeOfSrc, JsonSerializationContext context) {
            if (src.getBooleanCount() > 1) {
                return context.serialize(src.getBooleanList());
            } else if (src.getBooleanCount() == 1) {
                return context.serialize(src.getBoolean(0));
            } else if (src.getDoubleCount() > 1) {
                return context.serialize(src.getDoubleList());
            } else if (src.getDoubleCount() == 1) {
                return context.serialize(src.getDouble(0));
            } else if (src.getFloatCount() > 1) {
                return context.serialize(src.getFloatList());
            } else if (src.getFloatCount() == 1) {
                return context.serialize(src.getFloat(0));
            } else if (src.getIntegerCount() > 1) {
                return context.serialize(src.getIntegerList());
            } else if (src.getIntegerCount() == 1) {
                return context.serialize(src.getInteger(0));
            } else if (src.getLongCount() > 1) {
                return context.serialize(src.getLongList());
            } else if (src.getLongCount() == 1) {
                return context.serialize(src.getLong(0));
            } else if (src.getStringCount() > 1) {
                return context.serialize(src.getStringList());
            } else if (src.getStringCount() == 1) {
                return context.serialize(src.getString(0));
            } else if (src.getValueCount() > 0) {
                return context.serialize(src.getValueList());
            } else if (src.hasBytes()) {
                return context.serialize(src.getBytes().toByteArray());
            } else if (src.getNameValueCount() > 0) {
                JsonObject obj = new JsonObject();
                for (EntityData.NameValue nameValue : src.getNameValueList()) {
                    obj.add(nameValue.getName(), context.serialize(nameValue.getValue()));
                }
                return obj;
            }
            return JsonNull.INSTANCE;
        }

        @Override
        public EntityData.Value deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityData.Value.Builder value = EntityData.Value.newBuilder();
            if (json.isJsonPrimitive()) {
                extractPrimitive(value, json);
            } else if (json.isJsonObject()) {
                extractMap(json, context, value);
            } else if (json.isJsonArray()) {
                JsonArray jsonArray = json.getAsJsonArray();
                TByteList byteList = new TByteArrayList();
                for (JsonElement element : jsonArray) {
                    if (element.isJsonArray()) {
                        value.addValue((EntityData.Value) context.deserialize(element, EntityData.Value.class));
                    } else if (element.isJsonObject()) {
                        value.addValue((EntityData.Value) context.deserialize(element, EntityData.Value.class));
                    } else if (element.isJsonPrimitive()) {
                        extractPrimitive(value, element);
                        if (element.getAsJsonPrimitive().isNumber()) {
                            try {
                                byteList.add(element.getAsByte());
                            } catch (NumberFormatException nfe) {
                                byteList.add((byte) 0);
                            }
                        }
                    }
                }
                value.setBytes(ByteString.copyFrom(byteList.toArray()));
            }
            return value.build();
        }

        private void extractMap(JsonElement json, JsonDeserializationContext context, EntityData.Value.Builder value) {
            JsonObject nameValueObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> nameValue : nameValueObject.entrySet()) {
                EntityData.Value innerValue = context.deserialize(nameValue.getValue(), EntityData.Value.class);
                value.addNameValue(EntityData.NameValue.newBuilder().setName(nameValue.getKey()).setValue(innerValue));
            }
        }

        private void extractPrimitive(EntityData.Value.Builder value, JsonElement element) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                value.addDouble(primitive.getAsDouble());
                value.addFloat(primitive.getAsFloat());
                try {
                    value.addInteger(primitive.getAsInt());
                    value.addLong(primitive.getAsLong());
                } catch (NumberFormatException e) {
                    value.addInteger(0);
                    value.addLong(0);
                }
            }
            if (primitive.isBoolean()) {
                value.addBoolean(primitive.getAsBoolean());
            }
            if (primitive.isString()) {
                value.addString(primitive.getAsString());
            }
        }
    }

}

package org.terasology.entitySystem.pojo.persistence;

import com.google.gson.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
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
 * @author Immortius <immortius@gmail.com>
 */
public class EntityDataJSONFormat {

    public static void write(EntityData.World world, BufferedWriter writer) throws IOException {
        newGson().toJson(world, writer);
    }

    public static void write(EntityData.Prefab prefab, BufferedWriter writer) throws IOException {
        newGson().toJson(prefab, writer);
    }

    public static EntityData.World readWorld(BufferedReader reader) throws IOException {
        try {
            return newGson().fromJson(reader, EntityData.World.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("Failed to load world", e);
        }
    }

    public static EntityData.Prefab readPrefab(BufferedReader reader) throws IOException {
        try {
            return newGson().fromJson(reader, EntityData.Prefab.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("Failed to load world", e);
        }
    }

    private static Gson newGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(EntityData.World.class, new WorldHandler())
                .registerTypeAdapter(EntityData.Entity.class, new EntityHandler())
                .registerTypeAdapter(EntityData.Prefab.class, new PrefabHandler())
                .registerTypeAdapter(EntityData.Component.class, new ComponentHandler())
                .registerTypeAdapter(EntityData.Component.Builder.class, new ComponentBuilderHandler())
                .registerTypeAdapter(EntityData.Value.class, new ValueHandler())
                .create();
    }

    private static class WorldHandler implements JsonSerializer<EntityData.World>, JsonDeserializer<EntityData.World> {

        public JsonElement serialize(EntityData.World src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            for (Map.Entry<Descriptors.FieldDescriptor, Object> field : src.getAllFields().entrySet()) {
                result.add(field.getKey().getName(), context.serialize(field.getValue()));
            }
            return result;
        }

        public EntityData.World deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityData.World.Builder world = EntityData.World.newBuilder();
            if (json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                JsonArray prefabArray = jsonObject.getAsJsonArray("prefab");
                if (prefabArray != null) {
                    for (JsonElement prefabElem : prefabArray) {
                        world.addPrefab((EntityData.Prefab)context.deserialize(prefabElem, EntityData.Prefab.class));
                    }
                }
                JsonArray entityArray = jsonObject.getAsJsonArray("entity");
                if (entityArray != null) {
                    for (JsonElement entityElem : entityArray) {
                        world.addEntity((EntityData.Entity)context.deserialize(entityElem, EntityData.Entity.class));
                    }
                }
                JsonPrimitive nextId = jsonObject.getAsJsonPrimitive("next_entity_id");
                if (nextId != null) {
                    world.setNextEntityId(nextId.getAsInt());
                }
                JsonArray freedIdArray = jsonObject.getAsJsonArray("freed_entity_id");
                if (freedIdArray != null) {
                    for (JsonElement freedId : freedIdArray) {
                        world.addFreedEntityId(freedId.getAsInt());
                    }
                }

            }
            return world.build();
        }
    }

    private static class ComponentHandler implements JsonSerializer<EntityData.Component> {

        public JsonElement serialize(EntityData.Component src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            for (EntityData.NameValue field : src.getFieldList()) {
                result.add(field.getName(), context.serialize(field.getValue()));
            }
            return result;
        }

    }

    private static class ComponentBuilderHandler implements JsonDeserializer<EntityData.Component.Builder> {
        public EntityData.Component.Builder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityData.Component.Builder component = EntityData.Component.newBuilder();
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                EntityData.NameValue.Builder nameValue = EntityData.NameValue.newBuilder();
                nameValue.setName(entry.getKey());
                EntityData.Value value = (EntityData.Value) context.deserialize(entry.getValue(), EntityData.Value.class);
                nameValue.setValue(value);
                component.addField(nameValue);
            }
            return component;
        }
    }

    private static class EntityHandler implements JsonSerializer<EntityData.Entity>, JsonDeserializer<EntityData.Entity> {

        public JsonElement serialize(EntityData.Entity src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            if (src.hasId()) {
                result.addProperty("id", src.getId());
            }
            if (src.hasParentPrefab() && !src.getParentPrefab().isEmpty()) {
                result.addProperty("parentPrefab", src.getParentPrefab());
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

        public EntityData.Entity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
            JsonObject jsonObject = json.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String name = entry.getKey().toLowerCase(Locale.ENGLISH);
                // JAVA7: Make this a switch statement
                if (name.equals("parentprefab")) {
                    if (entry.getValue().isJsonPrimitive()) {
                        entity.setParentPrefab(entry.getValue().getAsString());
                    }
                } else if (name.equals("id")) {
                    if (entry.getValue().isJsonPrimitive()) {
                        entity.setId(entry.getValue().getAsInt());
                    }
                } else if (name.equals("removedcomponent")) {
                    if (entry.getValue().isJsonArray()) {
                        for (JsonElement element : entry.getValue().getAsJsonArray()) {
                            entity.addRemovedComponent(element.getAsString());
                        }
                    }
                } else {
                    EntityData.Component.Builder component = context.deserialize(entry.getValue(), EntityData.Component.Builder.class);
                    component.setType(entry.getKey());
                    entity.addComponent(component);
                }

            }
            return entity.build();
        }
    }

    private static class PrefabHandler implements JsonSerializer<EntityData.Prefab>, JsonDeserializer<EntityData.Prefab> {

        public JsonElement serialize(EntityData.Prefab src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            if (src.hasName()) {
                result.addProperty("name", src.getName());
            }
            if (src.getParentNameCount() > 0) {
                result.add("parent", context.serialize(src.getParentNameList()));
            }
            for (EntityData.Component component : src.getComponentList()) {
                result.add(component.getType(), context.serialize(component));
            }
            return result;
        }

        public EntityData.Prefab deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityData.Prefab.Builder prefab = EntityData.Prefab.newBuilder();
            JsonObject jsonObject = json.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String name = entry.getKey().toLowerCase(Locale.ENGLISH);
                // JAVA7: Make this a switch statement
                if (name.equals("name")) {
                    if (entry.getValue().isJsonPrimitive())
                        prefab.setName(entry.getValue().getAsString());
                }
                else if (name.equals("parent")) {
                    if (entry.getValue().isJsonPrimitive()) {
                        prefab.addParentName(entry.getValue().getAsString());
                    } else if (entry.getValue().isJsonArray()) {
                        for (JsonElement element : entry.getValue().getAsJsonArray()) {
                            prefab.addParentName(element.getAsString());
                        }
                    }
                } else {
                    EntityData.Component.Builder component = context.deserialize(entry.getValue(), EntityData.Component.Builder.class);
                    component.setType(entry.getKey());
                    prefab.addComponent(component);
                }

            }
            return prefab.build();
        }
    }

    private static class ValueHandler implements JsonSerializer<EntityData.Value>, JsonDeserializer<EntityData.Value> {

        public JsonElement serialize(EntityData.Value src, Type typeOfSrc, JsonSerializationContext context) {
            if (src.getBooleanCount() > 1) {
                return context.serialize(src.getBooleanList());
            }
            else if (src.getBooleanCount() == 1) {
                return context.serialize(src.getBoolean(0));
            }
            else if (src.getDoubleCount() > 1) {
                return context.serialize(src.getDoubleList());
            }
            else if (src.getDoubleCount() == 1) {
                return context.serialize(src.getDouble(0));
            }
            else if (src.getFloatCount() > 1) {
                return context.serialize(src.getFloatList());
            }
            else if (src.getFloatCount() == 1) {
                return context.serialize(src.getFloat(0));
            }
            else if (src.getIntegerCount() > 1) {
                return context.serialize(src.getIntegerList());
            }
            else if (src.getIntegerCount() == 1) {
                return context.serialize(src.getInteger(0));
            }
            else if (src.getLongCount() > 1) {
                return context.serialize(src.getLongList());
            }
            else if (src.getLongCount() == 1) {
                return context.serialize(src.getLong(0));
            }
            else if (src.getStringCount() > 1) {
                return context.serialize(src.getStringList());
            }
            else if (src.getStringCount() == 1) {
                return context.serialize(src.getString(0));
            }
            else if (src.getValueCount() > 0) {
                return context.serialize(src.getValueList());
            }
            else if (src.hasBytes()) {
                return context.serialize(src.getBytes().toByteArray());
            }
            else if (src.getNameValueCount() > 0) {
                JsonObject obj = new JsonObject();
                for (EntityData.NameValue nameValue : src.getNameValueList()) {
                    obj.add(nameValue.getName(), context.serialize(nameValue.getValue()));
                }
                return obj;
            }
            return JsonNull.INSTANCE;
        }

        public EntityData.Value deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityData.Value.Builder value = EntityData.Value.newBuilder();
            if (json.isJsonPrimitive()) {
                extractPrimitive(value, json);
            }
            else if (json.isJsonObject()) {
                extractMap(json, context, value);
            }
            else if (json.isJsonArray())
            {
                JsonArray jsonArray = json.getAsJsonArray();
                TByteList byteList = new TByteArrayList();
                for (JsonElement element : jsonArray) {
                    if (element.isJsonArray()) {
                        value.addValue((EntityData.Value)context.deserialize(element, EntityData.Value.class));
                    }
                    else if (json.isJsonObject()) {
                        extractMap(json, context, value);
                    }
                    else if (element.isJsonPrimitive()) {
                        extractPrimitive(value, element);
                        if (element.getAsJsonPrimitive().isNumber()) {
                            try {
                                byteList.add(element.getAsByte());
                            } catch (NumberFormatException nfe) { }
                        }
                    }
                }
                value.setBytes(ByteString.copyFrom(byteList.toArray()));
            }
            return value.build();
        }

        private void extractMap(JsonElement json, JsonDeserializationContext context, EntityData.Value.Builder value) {
            JsonObject nameValueObject = json.getAsJsonObject();
            for (Map.Entry<String,JsonElement> nameValue : nameValueObject.entrySet()) {
                EntityData.Value innerValue = (EntityData.Value)context.deserialize(nameValue.getValue(), EntityData.Value.class);
                value.addNameValue(EntityData.NameValue.newBuilder().setName(nameValue.getKey()).setValue(innerValue));
            }
        }

        private void extractPrimitive(EntityData.Value.Builder value, JsonElement element) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                value.addDouble(primitive.getAsDouble());
                value.addFloat(primitive.getAsFloat());
                try {
                    value.addLong(primitive.getAsLong());
                    value.addInteger(primitive.getAsInt());
                } catch (NumberFormatException e) {}
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

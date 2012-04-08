package org.terasology.entitySystem.pojo.persistence;

import com.google.gson.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.terasology.protobuf.EntityData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntityDataJSONFormat {

    public static void write(BufferedWriter writer, EntityData.World world) throws IOException {
        writer.write(new EntityDataJSONFormat().write(world));
    }

    Gson gson;

    EntityDataJSONFormat() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(EntityData.World.class, new GenericMessageHandler())
                .registerTypeAdapter(EntityData.Entity.class, new EntityHandler())
                .registerTypeAdapter(EntityData.Component.class, new ComponentHandler())
                .registerTypeAdapter(EntityData.Value.class, new ValueHandler())
                .create();
    }

    String write(EntityData.World world) {
        JsonObject result = new JsonObject();
        /*for (Map.Entry<Descriptors.FieldDescriptor, Object> field : world.getAllFields().entrySet()) {
            if (!field.getKey().getName().equalsIgnoreCase("entity")) {
                result.add(field.getKey().getName(), gson.toJsonTree(field.getValue()));
            }
        } */
        return gson.toJson(world);
    }

    private static class GenericMessageHandler implements JsonSerializer<com.google.protobuf.Message> {

        public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            for (Map.Entry<Descriptors.FieldDescriptor, Object> field : src.getAllFields().entrySet()) {
                result.add(field.getKey().getName(), context.serialize(field.getValue()));
            }
            return result;
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

    private static class EntityHandler implements JsonSerializer<EntityData.Entity> {

        public JsonElement serialize(EntityData.Entity src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("id", src.getId());
            for (EntityData.Component component : src.getComponentList()) {
                result.add(component.getType(), context.serialize(component));
            }
            return result;
        }
    }

    private static class ValueHandler implements JsonSerializer<EntityData.Value> {

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
            else if (src.getValueCount() > 1) {
                return context.serialize(src.getValueList());
            }
            else if (src.getValueCount() == 1) {
                return context.serialize(src.getValue(0));
            }
            else if (src.getByteCount() > 1) {
                JsonArray array = new JsonArray();
                for (ByteString bytes : src.getByteList()) {
                    array.add(context.serialize(bytes.toByteArray()));
                }
                return array;
            }
            else if (src.getByteCount() == 1) {
                return context.serialize(src.getByte(0).toByteArray());
            }
            else if (src.getNameValueCount() > 0) {
                JsonObject obj = new JsonObject();
                for (EntityData.NameValue nameValue : src.getNameValueList()) {
                    obj.add(nameValue.getName(), context.serialize(nameValue.getValue()));
                }
                return obj;
            }
            return JsonNull.INSTANCE;

            // TODO: Support byte
        }
    }

}

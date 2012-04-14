package org.terasology.entitySystem.persist;


import com.google.gson.*;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.pojo.PojoPrefabManager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;

public class JsonPersister implements PrefabPersister {

    private Gson gson;

    protected PrefabManager prefabManager;

    public JsonPersister() {
        this.initialize();
    }

    private void initialize() {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Prefab.class, new PrefabAdapter());
        builder.registerTypeAdapter(PrefabManager.class, new PrefabManagerAdapter());
        builder.registerTypeAdapter(Component.class, new ComponentAdapter());
        builder.registerTypeAdapter(EntityRef.class, new EntityRefAdapter());

        this.gson = builder.create();
    }


    public Prefab loadPrefab(Reader reader, PrefabManager manager) throws IOException {
        prefabManager = manager;
        Prefab prefab = gson.fromJson(reader, Prefab.class);
        prefabManager = null;

        return prefab;
    }

    public PrefabManager loadPrefabs(Reader reader) throws IOException {
        PrefabManager manager = gson.fromJson(reader, PrefabManager.class);
        prefabManager = null;

        return manager;
    }

    public void savePrefab(Writer writer, Prefab prefab) throws IOException {
        gson.toJson(prefab, Prefab.class, writer);

        writer.flush();
    }

    public void savePrefabs(Writer writer, PrefabManager manager) throws IOException {
        gson.toJson(manager, PrefabManager.class, writer);

        writer.flush();
    }

    protected class PrefabAdapter implements JsonDeserializer<Prefab>, JsonSerializer<Prefab> {

        public Prefab deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();

            String prefabName = object.get("name").getAsString();

            Prefab prefab = prefabManager.createPrefab(prefabName);

            JsonArray parents = object.get("parents").getAsJsonArray();

            for (JsonElement parentElement : parents) {
                prefab.addParent(prefabManager.createPrefab(parentElement.getAsString()));
            }

            JsonObject components = object.get("components").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : components.entrySet()) {
                prefab.setComponent(gson.fromJson(entry.getValue(), Component.class));
            }

            return prefab;
        }

        public JsonElement serialize(Prefab src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            {

                object.addProperty("name", src.getName());

                JsonArray parents = new JsonArray();
                {
                    for (Prefab parent : src.getParents()) {
                        parents.add(new JsonPrimitive(parent.getName()));
                    }
                }
                object.add("parents", parents);

                JsonObject components = new JsonObject();
                {
                    for (Component component : src.listOwnComponents()) {
                        components.add(component.getName(), gson.toJsonTree(component, Component.class));
                    }
                }
                object.add("components", components);
            }
            return object;
        }
    }

    protected class PrefabManagerAdapter implements JsonDeserializer<PrefabManager>, JsonSerializer<PrefabManager> {

        public PrefabManager deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();

            prefabManager = new PojoPrefabManager();

            JsonArray prefabs = object.get("prefabs").getAsJsonArray();
            for (JsonElement prefabElement : prefabs) {
                gson.fromJson(prefabElement, Prefab.class);
            }

            return prefabManager;
        }

        public JsonElement serialize(PrefabManager src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            {
                JsonArray prefabs = new JsonArray();
                {
                    for (Prefab prefab : src.listPrefabs()) {
                        prefabs.add(gson.toJsonTree(prefab, Prefab.class));
                    }
                }
                object.add("prefabs", prefabs);
            }
            return object;
        }
    }

    protected class ComponentAdapter implements JsonDeserializer<Component>, JsonSerializer<Component> {

        public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();

            try {
                Class<?> componentClass = Class.forName(object.get("class").getAsString());

                if (!Component.class.isAssignableFrom(componentClass)) {
                    throw new IllegalStateException("Wrong class specified - not Component!");
                }

                Component instance = (Component) componentClass.newInstance();

                for (Map.Entry<String, JsonElement> element : object.entrySet()) {
                    if ("class".equals(element.getKey())) {
                        continue;
                    }

                    Field field = componentClass.getDeclaredField(element.getKey());
                    field.setAccessible(true);
                    field.set(instance, gson.fromJson(element.getValue(), field.getType()));
                }

                return instance;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            {
                object.addProperty("class", src.getClass().getName());

                for (Field field : src.getClass().getDeclaredFields()) {
                    if (Modifier.isTransient(field.getModifiers())) { // skip transient fields
                        continue;
                    }

                    field.setAccessible(true);

                    try {
                        object.add(field.getName(), gson.toJsonTree(field.get(src)));
                    } catch (IllegalAccessException e) {
                        // @todo add proper logging
                        throw new IllegalStateException("Can't obtain field '" + field.getName() + "' from '" + object.getClass().getName() + "'");
                    }
                }
            }
            return object;
        }
    }

    // Basically don't serialize entity references on prototypes
    private class EntityRefAdapter implements JsonDeserializer<EntityRef>, JsonSerializer<EntityRef> {
        public EntityRef deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return EntityRef.NULL;
        }

        public JsonElement serialize(EntityRef src, Type typeOfSrc, JsonSerializationContext context) {
            return JsonNull.INSTANCE;
        }
    }
}

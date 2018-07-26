/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.recording;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.LowLevelEntityManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.Event;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.GetMaxSpeedEvent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.AttackEvent;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.module.ModuleEnvironment;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.EnumTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.EntityRefTypeHandler;
import org.terasology.persistence.typeHandling.gson.GsonPersistedData;
import org.terasology.persistence.typeHandling.gson.GsonSerializationContext;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.naming.Name;
import org.terasology.input.BindAxisEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.InputEvent;
import org.terasology.input.events.KeyUpEvent;
import org.terasology.input.events.KeyRepeatEvent;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseAxisEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.input.ButtonState;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes and deserializes RecordedEvents.
 */
class RecordedEventSerializer {

    private static final Logger logger = LoggerFactory.getLogger(RecordedEventSerializer.class);
    private static final double DEFAULT_DOUBLE_VALUE = 0.0;
    private TypeSerializationLibrary typeSerializationLibrary;
    private EntityManager entityManager;
    private ModuleEnvironment moduleEnvironment;
    private Map<String, Class<? extends InputEvent>> inputEventClassMap;


    RecordedEventSerializer(EntityManager entityManager, ModuleEnvironment moduleEnvironment) {
        ReflectionReflectFactory reflectFactory = new ReflectionReflectFactory();
        CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(reflectFactory);
        this.typeSerializationLibrary = TypeSerializationLibrary.createDefaultLibrary(reflectFactory, copyStrategyLibrary);
        typeSerializationLibrary.addTypeHandler(EntityRef.class, new EntityRefTypeHandler((EngineEntityManager) entityManager));
        typeSerializationLibrary.addTypeHandler(MouseAxisEvent.MouseAxis.class, new EnumTypeHandler<>(MouseAxisEvent.MouseAxis.class));
        typeSerializationLibrary.addTypeHandler(ButtonState.class, new EnumTypeHandler<>(ButtonState.class));
        typeSerializationLibrary.addTypeHandler(Keyboard.Key.class, new EnumTypeHandler<>(Keyboard.Key.class));
        typeSerializationLibrary.addTypeHandler(MouseInput.class, new EnumTypeHandler<>(MouseInput.class));
        typeSerializationLibrary.addTypeHandler(MovementMode.class, new EnumTypeHandler<>(MovementMode.class));
        this.entityManager = entityManager;
        this.moduleEnvironment = moduleEnvironment;
    }

    void serializeRecordedEvents(List<RecordedEvent> events, String filePath) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(filePath));
            writer.beginObject();
            writer.name("events");
            writer.beginArray();
            for (RecordedEvent event : events) {
                writer.beginObject();
                writer.name("entityRef_ID").value(event.getEntityId());
                writer.name("timestamp").value(event.getTimestamp());
                writer.name("index").value(event.getIndex());
                writer.name("event_class").value(event.getEvent().getClass().getName());
                writer.name("event_data");
                writer.beginObject();
                writeSpecificEventData(writer, event.getEvent());
                writer.endObject();
                writer.endObject();
            }
            writer.endArray();
            writer.endObject();
            writer.close();
        } catch (Exception e) {
            logger.error("Error while serializing events:", e);
        }


    }

    private void writeSpecificEventData(JsonWriter writer, Event event) {
        try {
            GsonSerializationContext serializationContext = new GsonSerializationContext();
            Gson gson = new GsonBuilder().create();
            if (event instanceof InputEvent) {
                InputEvent e = (InputEvent) event;
                writer.name("delta").value(e.getDelta());
                writer.name("consumed").value(e.isConsumed());
                writer.name("target").value(e.getTarget().getId());

                writeVector3fData(writer, serializationContext, e);
                writeInputEventInstanceData(writer, event, serializationContext);


            } else if (event instanceof CameraTargetChangedEvent) {
                CameraTargetChangedEvent e = (CameraTargetChangedEvent) event;
                writer.name("OldTarget").value(e.getOldTarget().getId());
                writer.name("NewTarget").value(e.getNewTarget().getId());
            } else if (event instanceof PlaySoundEvent) {
                PlaySoundEvent e = (PlaySoundEvent) event;
                writer.name("volume").value(e.getVolume());
                TypeHandler handler = typeSerializationLibrary.getTypeHandler(StaticSound.class);
                PersistedData data = handler.serialize(e.getSound(), serializationContext);
                writer.name("sound").value(data.getAsString());

            } else if (event instanceof CharacterMoveInputEvent) {
                CharacterMoveInputEvent e = (CharacterMoveInputEvent) event;
                writer.name("delta").value(e.getDeltaMs());
                writer.name("pitch").value(e.getPitch());
                writer.name("yaw").value(e.getYaw());
                writer.name("running").value(e.isRunning());
                writer.name("crouching").value(e.isCrouching());
                writer.name("jumpRequested").value(e.isJumpRequested());
                writer.name("sequeceNumber").value(e.getSequenceNumber());
                writer.name("firstRun").value(e.isFirstRun());
                TypeHandler handler = typeSerializationLibrary.getTypeHandler(Vector3f.class);
                GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getMovementDirection(), serializationContext);
                writer.name("movementDirection");
                writer.beginObject();
                JsonArray array = data.getElement().getAsJsonArray();
                writer.name("x").value(array.get(0).getAsFloat());
                writer.name("y").value(array.get(1).getAsFloat());
                writer.name("z").value(array.get(2).getAsFloat());
                writer.endObject();

            } else if (event instanceof GetMaxSpeedEvent) {
                GetMaxSpeedEvent e = (GetMaxSpeedEvent) event;
                writer.name("baseValue").value(e.getBaseValue());
                TypeHandler handler = typeSerializationLibrary.getTypeHandler(MovementMode.class);
                GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getMovementMode(), serializationContext);
                writer.name("movementMode").value(data.getAsString());
                writer.name("modifiers");
                gson.toJson(e.getModifiers(), TFloatArrayList.class, writer);
                writer.name("multipliers");
                gson.toJson(e.getMultipliers(), TFloatArrayList.class, writer);
                writer.name("postModifiers");
                gson.toJson(e.getPostModifiers(), TFloatArrayList.class, writer);
            } else if (event instanceof AttackEvent) {
                AttackEvent e = (AttackEvent) event;
                writer.name("instigator").value(e.getInstigator().getId());
                writer.name("directCause").value(e.getDirectCause().getId());
            } else {
                logger.error("ERROR: EVENT NOT SUPPORTED FOR SERIALIZATION");
            }
        } catch (Exception e) {
            logger.error("Could not serialize this event: " + event.toString(), e);
        }
    }

    private void writeVector3fData(JsonWriter writer, GsonSerializationContext serializationContext, InputEvent e) throws IOException {
        if (e.getHitNormal() == null) {
            writeDefaultVector3fData(writer);
        } else {
            writeRealVector3fData(writer, serializationContext, e);
        }
    }

    private void writeRealVector3fData(JsonWriter writer, GsonSerializationContext serializationContext, InputEvent e) throws IOException {
        TypeHandler handler = typeSerializationLibrary.getTypeHandler(Vector3f.class);
        GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getHitNormal(), serializationContext);
        writer.name("hitNormal");
        writer.beginObject();
        JsonArray array = data.getElement().getAsJsonArray();
        writer.name("x").value(array.get(0).getAsFloat());
        writer.name("y").value(array.get(1).getAsFloat());
        writer.name("z").value(array.get(2).getAsFloat());
        writer.endObject();

        data = (GsonPersistedData) handler.serialize(e.getHitPosition(), serializationContext);
        writer.name("hitPosition");
        writer.beginObject();
        array = data.getElement().getAsJsonArray();
        writer.name("x").value(array.get(0).getAsFloat());
        writer.name("y").value(array.get(1).getAsFloat());
        writer.name("z").value(array.get(2).getAsFloat());
        writer.endObject();

        handler = typeSerializationLibrary.getTypeHandler(Vector3i.class);
        data = (GsonPersistedData) handler.serialize(e.getTargetBlockPosition(), serializationContext);
        writer.name("targetBlockPosition");
        writer.beginObject();
        array = data.getElement().getAsJsonArray();
        writer.name("x").value(array.get(0).getAsInt());
        writer.name("y").value(array.get(1).getAsInt());
        writer.name("z").value(array.get(2).getAsInt());
        writer.endObject();
    }

    private void writeDefaultVector3fData(JsonWriter writer) throws IOException {
        writer.name("hitNormal");
        writer.beginObject();
        writer.name("x").value(DEFAULT_DOUBLE_VALUE);
        writer.name("y").value(DEFAULT_DOUBLE_VALUE);
        writer.name("z").value(DEFAULT_DOUBLE_VALUE);
        writer.endObject();

        writer.name("hitPosition");
        writer.beginObject();
        writer.name("x").value(DEFAULT_DOUBLE_VALUE);
        writer.name("y").value(DEFAULT_DOUBLE_VALUE);
        writer.name("z").value(DEFAULT_DOUBLE_VALUE);
        writer.endObject();

        writer.name("targetBlockPosition");
        writer.beginObject();
        writer.name("x").value(DEFAULT_DOUBLE_VALUE);
        writer.name("y").value(DEFAULT_DOUBLE_VALUE);
        writer.name("z").value(DEFAULT_DOUBLE_VALUE);
        writer.endObject();
    }

    private void writeInputEventInstanceData(JsonWriter writer, Event event, GsonSerializationContext serializationContext) throws Exception {
        if (event instanceof MouseWheelEvent) {
            MouseWheelEvent e = (MouseWheelEvent) event;
            writer.name("wheelTurns").value(e.getWheelTurns());
            TypeHandler handler = typeSerializationLibrary.getTypeHandler(Vector2i.class);
            GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getMousePosition(), serializationContext);
            writer.name("mousePosition");
            writer.beginObject();
            JsonArray array = data.getElement().getAsJsonArray();
            writer.name("x").value(array.get(0).getAsInt());
            writer.name("y").value(array.get(1).getAsInt());
            writer.endObject();
        } else if (event instanceof MouseAxisEvent) {
            MouseAxisEvent e = (MouseAxisEvent) event;
            writer.name("value").value(e.getValue());
            TypeHandler handler = typeSerializationLibrary.getTypeHandler(MouseAxisEvent.MouseAxis.class);
            GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getMouseAxis(), serializationContext);
            writer.name("mouseAxis").value(data.getAsString());
        } else if (event instanceof BindAxisEvent) {
            BindAxisEvent e = (BindAxisEvent) event;
            writer.name("id").value(e.getId());
            writer.name("value").value(e.getValue());
        } else if (event instanceof BindButtonEvent) {
            BindButtonEvent e = (BindButtonEvent) event;
            TypeHandler handler = typeSerializationLibrary.getTypeHandler(ButtonState.class);
            GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getState(), serializationContext);
            writer.name("state").value(data.getAsString());
            writer.name("id");
            writer.beginObject();
            handler = typeSerializationLibrary.getTypeHandler(Name.class);
            data = (GsonPersistedData) handler.serialize(e.getId().getModuleName(), serializationContext);
            writer.name("moduleName").value(data.getAsString());
            data = (GsonPersistedData) handler.serialize(e.getId().getObjectName(), serializationContext);
            writer.name("objectName").value(data.getAsString());
            writer.endObject();
        } else if (event instanceof KeyEvent) {
            KeyEvent e = (KeyEvent) event;
            writer.name("keychar").value(e.getKeyCharacter());
            TypeHandler handler = typeSerializationLibrary.getTypeHandler(ButtonState.class);
            GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getState(), serializationContext);
            writer.name("state").value(data.getAsString());
            handler = typeSerializationLibrary.getTypeHandler(Keyboard.Key.class); // might need to add some things to key
            data = (GsonPersistedData) handler.serialize(e.getKey(), serializationContext);
            writer.name("input").value(data.getAsString());
        } else if (event instanceof MouseButtonEvent) {
            MouseButtonEvent e = (MouseButtonEvent) event;
            TypeHandler handler = typeSerializationLibrary.getTypeHandler(ButtonState.class);
            GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getState(), serializationContext);
            writer.name("state").value(data.getAsString());
            handler = typeSerializationLibrary.getTypeHandler(MouseInput.class);
            data = (GsonPersistedData) handler.serialize(e.getButton(), serializationContext);
            writer.name("button").value(data.getAsString());
            handler = typeSerializationLibrary.getTypeHandler(Vector2i.class);
            data = (GsonPersistedData) handler.serialize(e.getMousePosition(), serializationContext);
            writer.name("mousePosition");
            writer.beginObject();
            JsonArray array = data.getElement().getAsJsonArray();
            writer.name("x").value(array.get(0).getAsInt());
            writer.name("y").value(array.get(1).getAsInt());
            writer.endObject();
        } else {
            logger.error("ERROR, EVENT NOT COMPATIBLE");
        }
    }

    List<RecordedEvent> deserializeRecordedEvents(String path) {
        List<RecordedEvent> events = new ArrayList<>();
        createInputEventClassMap();
        JsonObject jsonObject;
        try (FileReader fileReader = new FileReader(path)) {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(fileReader);
            jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonEvents = jsonObject.getAsJsonArray("events");
            for (JsonElement element : jsonEvents) {
                jsonObject = element.getAsJsonObject();
                String className = jsonObject.get("event_class").getAsString();
                long refId = jsonObject.get("entityRef_ID").getAsLong();
                long index = jsonObject.get("index").getAsLong();
                long timestamp = jsonObject.get("timestamp").getAsLong();
                Event event = deserializeSpecificEventData(jsonObject.get("event_data").getAsJsonObject(), className);
                RecordedEvent re = new RecordedEvent(refId, event, timestamp, index);
                events.add(re);
            }
        } catch (Exception e) {
            logger.error("Error while deserializing event:", e);
        }

        return events;
    }

    private void createInputEventClassMap() {
        this.inputEventClassMap = new HashMap<>();
        Iterable<Class<? extends InputEvent>> classes = moduleEnvironment.getSubtypesOf(InputEvent.class);
        for (Class<? extends InputEvent> c : classes) {
            this.inputEventClassMap.put(c.getName(), c);
        }
    }

    private Event deserializeSpecificEventData(JsonObject jsonObject, String className) {
        Event result = null;
        Gson gson = new GsonBuilder().create();
        if (className.equals(CameraTargetChangedEvent.class.getName())) {
            EntityRef oldTarget = new RecordedEntityRef(jsonObject.get("OldTarget").getAsLong(), (LowLevelEntityManager) this.entityManager);
            EntityRef newTarget =  new RecordedEntityRef(jsonObject.get("NewTarget").getAsLong(), (LowLevelEntityManager) this.entityManager);
            result = new CameraTargetChangedEvent(oldTarget, newTarget);
        } else if (className.equals(PlaySoundEvent.class.getName())) {
            float volume = jsonObject.get("volume").getAsFloat();
            GsonPersistedData data = new GsonPersistedData(jsonObject.get("sound"));
            TypeHandler handler = typeSerializationLibrary.getTypeHandler(StaticSound.class);
            StaticSound sound = (StaticSound) handler.deserialize(data);
            result = new PlaySoundEvent(sound, volume);
        } else if (className.equals(CharacterMoveInputEvent.class.getName())) {
            long delta = jsonObject.get("delta").getAsLong();
            float pitch = jsonObject.get("pitch").getAsFloat();
            float yaw = jsonObject.get("yaw").getAsFloat();
            boolean running = jsonObject.get("running").getAsBoolean();
            boolean crouching = jsonObject.get("crouching").getAsBoolean();
            boolean jumpRequested = jsonObject.get("jumpRequested").getAsBoolean();
            int sequenceNumber = jsonObject.get("sequeceNumber").getAsInt();
            boolean firstRun = jsonObject.get("firstRun").getAsBoolean();
            JsonObject objMoveDirection = jsonObject.get("movementDirection").getAsJsonObject();
            Vector3f movementDirection = new Vector3f(objMoveDirection.get("x").getAsFloat(),
                    objMoveDirection.get("y").getAsFloat(),
                    objMoveDirection.get("z").getAsFloat());
            result = new CharacterMoveInputEvent(sequenceNumber, pitch, yaw, movementDirection, running, crouching, jumpRequested, delta);
        } else if (className.equals(GetMaxSpeedEvent.class.getName())) {
            float baseValue = jsonObject.get("baseValue").getAsFloat();
            TypeHandler handler = typeSerializationLibrary.getTypeHandler(MovementMode.class);
            GsonPersistedData data = new GsonPersistedData(jsonObject.get("movementMode"));
            MovementMode movementMode = (MovementMode) handler.deserialize(data);
            TFloatList modifiers = gson.fromJson(jsonObject.get("modifiers"), TFloatArrayList.class);
            TFloatList multipliers = gson.fromJson(jsonObject.get("multipliers"), TFloatArrayList.class);
            TFloatList postModifiers = gson.fromJson(jsonObject.get("postModifiers"), TFloatArrayList.class);
            GetMaxSpeedEvent event = new GetMaxSpeedEvent(baseValue, movementMode);
            event.setPostModifiers(postModifiers);
            event.setMultipliers(multipliers);
            event.setModifiers(modifiers);
            result = event;
        } else if (className.equals(AttackEvent.class.getName())) {
            EntityRef instigator = new RecordedEntityRef(jsonObject.get("instigator").getAsLong(), (LowLevelEntityManager) this.entityManager);
            EntityRef directCause = new RecordedEntityRef(jsonObject.get("directCause").getAsLong(), (LowLevelEntityManager) this.entityManager);
            result = new AttackEvent(instigator, directCause);
        } else if (getInputEventSpecificType(jsonObject, className) != null) { //input events
            result = getInputEventSpecificType(jsonObject, className);
        }

        return result;
    }

    private InputEvent getInputEventSpecificType(JsonObject jsonObject, String className) {
        InputEvent newEvent = null;
        try {
            Class clazz = this.inputEventClassMap.get(className);
            if (BindButtonEvent.class.isAssignableFrom(clazz) || BindAxisEvent.class.isAssignableFrom(clazz)) {
                newEvent = (InputEvent) clazz.getConstructor().newInstance();
            } else if (clazz.equals(KeyDownEvent.class) || clazz.equals(KeyRepeatEvent.class) || clazz.equals(KeyUpEvent.class)) { //KeyEvent
                GsonPersistedData data = new GsonPersistedData(jsonObject.get("input"));
                TypeHandler typeHandler = typeSerializationLibrary.getTypeHandler(Keyboard.Key.class);
                Keyboard.Key input = (Keyboard.Key) typeHandler.deserialize(data);
                data = new GsonPersistedData(jsonObject.get("state"));
                typeHandler = typeSerializationLibrary.getTypeHandler(ButtonState.class);
                ButtonState state = (ButtonState) typeHandler.deserialize(data);
                char keychar = jsonObject.get("keychar").getAsCharacter();
                float delta = jsonObject.get("delta").getAsFloat();
                KeyEvent aux;
                if (clazz.equals(KeyDownEvent.class)) {
                    aux = KeyDownEvent.create(input, keychar, delta); // The instance created here is static
                    aux = KeyDownEvent.createCopy((KeyDownEvent) aux); // This copies the static value so each KeyEvent does not have the same value
                } else if (clazz.equals(KeyRepeatEvent.class)) {
                    aux = KeyRepeatEvent.create(input, keychar, delta);
                    aux = KeyRepeatEvent.createCopy((KeyRepeatEvent) aux);
                } else {
                    aux = KeyUpEvent.create(input, keychar, delta);
                    aux = KeyUpEvent.createCopy((KeyUpEvent) aux);
                }
                newEvent = aux;
            } else if (clazz.equals(MouseButtonEvent.class)) {
                GsonPersistedData data = new GsonPersistedData(jsonObject.get("button"));
                TypeHandler typeHandler = typeSerializationLibrary.getTypeHandler(MouseInput.class);
                MouseInput button = (MouseInput) typeHandler.deserialize(data);
                data = new GsonPersistedData(jsonObject.get("state"));
                typeHandler = typeSerializationLibrary.getTypeHandler(ButtonState.class);
                ButtonState state = (ButtonState) typeHandler.deserialize(data);
                JsonObject aux = jsonObject.get("mousePosition").getAsJsonObject();
                Vector2i mousePosition = new Vector2i(aux.get("x").getAsInt(), aux.get("y").getAsInt());
                float delta = jsonObject.get("delta").getAsFloat();
                MouseButtonEvent event = new MouseButtonEvent(button, state, delta);
                event.setMousePosition(mousePosition);
                newEvent = event;
            } else if (clazz.equals(MouseAxisEvent.class)) {
                GsonPersistedData data = new GsonPersistedData(jsonObject.get("mouseAxis"));
                TypeHandler typeHandler = typeSerializationLibrary.getTypeHandler(MouseAxisEvent.MouseAxis.class);
                MouseAxisEvent.MouseAxis mouseAxis = (MouseAxisEvent.MouseAxis) typeHandler.deserialize(data);
                float value = jsonObject.get("value").getAsFloat();
                float delta = jsonObject.get("delta").getAsFloat();
                MouseAxisEvent aux = MouseAxisEvent.create(mouseAxis, value, delta);
                newEvent = MouseAxisEvent.createCopy(aux);
            } else if (clazz.equals(MouseWheelEvent.class)) {
                JsonObject aux = jsonObject.get("mousePosition").getAsJsonObject();
                Vector2i mousePosition = new Vector2i(aux.get("x").getAsInt(), aux.get("y").getAsInt());
                int wheelTurns = jsonObject.get("wheelTurns").getAsInt();
                float delta = jsonObject.get("delta").getAsFloat();
                newEvent = new MouseWheelEvent(mousePosition, wheelTurns, delta);
            } else {
                logger.error("Not an Input Event");
                return null;
            }

            if (newEvent instanceof BindButtonEvent) {
                bindButtonEventSetup((BindButtonEvent) newEvent, jsonObject);
            } else if (newEvent instanceof BindAxisEvent) {
                bindAxisEvent((BindAxisEvent) newEvent, jsonObject);
            }

            inputEventSetup(newEvent, jsonObject);
        } catch (Exception e) {
            logger.error("Error while deserializing event. Could not find class " + className, e);
        }
        return newEvent;
    }

    private void bindButtonEventSetup(BindButtonEvent event, JsonObject jsonObject) {
        GsonPersistedData data = new GsonPersistedData(jsonObject.get("state"));
        TypeHandler typeHandler = typeSerializationLibrary.getTypeHandler(ButtonState.class);
        ButtonState state = (ButtonState) typeHandler.deserialize(data);
        float delta = jsonObject.get("delta").getAsFloat();
        typeHandler = typeSerializationLibrary.getTypeHandler(Name.class);
        JsonObject aux = jsonObject.get("id").getAsJsonObject();
        data = new GsonPersistedData(aux.get("moduleName"));
        Name moduleName = (Name) typeHandler.deserialize(data);
        data = new GsonPersistedData(aux.get("objectName"));
        Name objectName = (Name) typeHandler.deserialize(data);
        SimpleUri id = new SimpleUri(moduleName, objectName);
        event.prepare(id, state, delta);
    }

    private void bindAxisEvent(BindAxisEvent event, JsonObject jsonObject) {
        String id = jsonObject.get("id").getAsString();
        float value = jsonObject.get("value").getAsFloat();
        float delta = jsonObject.get("delta").getAsFloat();
        event.prepare(id, value, delta);
    }

    private void inputEventSetup(InputEvent event, JsonObject jsonObject) {
        float delta = jsonObject.get("delta").getAsFloat();
        boolean consumed = jsonObject.get("consumed").getAsBoolean();
        EntityRef target =  new RecordedEntityRef(jsonObject.get("target").getAsLong(), (LowLevelEntityManager) this.entityManager);
        JsonObject aux = jsonObject.get("hitNormal").getAsJsonObject();
        Vector3f hitNormal = new Vector3f(aux.get("x").getAsFloat(), aux.get("y").getAsFloat(), aux.get("z").getAsFloat());
        aux = jsonObject.get("hitPosition").getAsJsonObject();
        Vector3f hitPosition = new Vector3f(aux.get("x").getAsFloat(), aux.get("y").getAsFloat(), aux.get("z").getAsFloat());
        aux = jsonObject.get("targetBlockPosition").getAsJsonObject();
        Vector3i targetBlockPosition = new Vector3i(aux.get("x").getAsInt(), aux.get("y").getAsInt(), aux.get("z").getAsInt());
        event.setTargetInfo(target, targetBlockPosition, hitPosition, hitNormal);
    }
}

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

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import org.lwjgl.input.Mouse;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.Event;
import org.terasology.input.*;
import org.terasology.input.binds.general.*;
import org.terasology.input.binds.interaction.AttackButton;
import org.terasology.input.binds.interaction.FrobButton;
import org.terasology.input.binds.inventory.UseItemButton;
import org.terasology.input.binds.movement.*;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.input.events.*;
import org.terasology.logic.behavior.nui.BTEditorButton;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.players.DecreaseViewDistanceButton;
import org.terasology.logic.players.IncreaseViewDistanceButton;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.EnumTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.EntityRefTypeHandler;
import org.terasology.persistence.typeHandling.gson.GsonDeserializationContext;
import org.terasology.persistence.typeHandling.gson.GsonPersistedData;
import org.terasology.persistence.typeHandling.gson.GsonSerializationContext;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.naming.Name;
import org.terasology.rendering.nui.editor.binds.NUIEditorButton;
import org.terasology.rendering.nui.editor.binds.NUISkinEditorButton;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RecordedEventSerializer {

    private TypeSerializationLibrary typeSerializationLibrary;
    private EntityManager entityManager;

    public RecordedEventSerializer(EntityManager entityManager) {
        ReflectionReflectFactory reflectFactory = new ReflectionReflectFactory();
        CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(reflectFactory);
        this.typeSerializationLibrary = TypeSerializationLibrary.createDefaultLibrary(reflectFactory, copyStrategyLibrary);
        typeSerializationLibrary.add(EntityRef.class, new EntityRefTypeHandler((EngineEntityManager) entityManager));
        typeSerializationLibrary.add(MouseAxisEvent.MouseAxis.class, new EnumTypeHandler<MouseAxisEvent.MouseAxis>(MouseAxisEvent.MouseAxis.class));
        typeSerializationLibrary.add(ButtonState.class, new EnumTypeHandler<ButtonState>(ButtonState.class));
        typeSerializationLibrary.add(Keyboard.Key.class, new EnumTypeHandler<Keyboard.Key>(Keyboard.Key.class));
        typeSerializationLibrary.add(MouseInput.class, new EnumTypeHandler<MouseInput>(MouseInput.class));
        this.entityManager = entityManager;
    }

    public void serializeRecordedEvents (List<RecordedEvent> events, String recordingPath) {
        try {
            /*Writer writer = new FileWriter("Output.json");
            Gson gson = new GsonBuilder().create();
            for (RecordedEvent event : events) {

            }*/
            JsonWriter writer = new JsonWriter(new FileWriter(recordingPath + "/events.json"));
            writer.beginObject();
            writer.name("events");
            writer.beginArray();
            for (RecordedEvent event : events) {
                writer.beginObject();
                writer.name("entityRef_ID").value(event.getEntityRefId());
                writer.name("timestamp").value(event.getTimestamp());
                writer.name("position").value(event.getPosition());
                writer.name("event_class").value(event.getEvent().getClass().toString());
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
            e.printStackTrace(); // put this into a logger
        }


    }

    private void writeSpecificEventData(JsonWriter writer, Event event) throws Exception {

        GsonSerializationContext serializationContext = new GsonSerializationContext(null);
        if (event instanceof InputEvent) {
            InputEvent e = (InputEvent) event;
            writer.name("delta").value(e.getDelta());
            writer.name("consumed").value(e.isConsumed());
            writer.name("target").value(e.getTarget().getId());

            TypeHandler handler = typeSerializationLibrary.getTypeHandlerFromClass(Vector3f.class);
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

            handler = typeSerializationLibrary.getTypeHandlerFromClass(Vector3i.class);
            data = (GsonPersistedData) handler.serialize(e.getTargetBlockPosition(), serializationContext);
            writer.name("targetBlockPosition");
            writer.beginObject();
            array = data.getElement().getAsJsonArray();
            writer.name("x").value(array.get(0).getAsInt());
            writer.name("y").value(array.get(1).getAsInt());
            writer.name("z").value(array.get(2).getAsInt());
            writer.endObject();

            writeInputEventInstanceData(writer, event, serializationContext);


        } else if (event instanceof CameraTargetChangedEvent) {
            CameraTargetChangedEvent e = (CameraTargetChangedEvent) event;
            writer.name("OldTarget").value(e.getOldTarget().getId());
            writer.name("NewTarget").value(e.getNewTarget().getId());
        } else if (event instanceof PlaySoundEvent) {
            PlaySoundEvent e = (PlaySoundEvent) event;
            writer.name("volume").value(e.getVolume());
            TypeHandler handler = typeSerializationLibrary.getTypeHandlerFromClass(StaticSound.class);
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
            TypeHandler handler = typeSerializationLibrary.getTypeHandlerFromClass(Vector3f.class);
            GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getMovementDirection(), serializationContext);
            writer.name("movementDirection");
            writer.beginObject();
            JsonArray array = data.getElement().getAsJsonArray();
            writer.name("x").value(array.get(0).getAsFloat());
            writer.name("y").value(array.get(1).getAsFloat());
            writer.name("z").value(array.get(2).getAsFloat());
            writer.endObject();

        } else {
            System.out.println("ERROR: EVENT NOT SUPPORTED FOR SERIALIZATION");
        }
    }

    private void writeInputEventInstanceData(JsonWriter writer, Event event, GsonSerializationContext serializationContext) throws Exception {
        if (event instanceof MouseWheelEvent) {
            MouseWheelEvent e = (MouseWheelEvent) event;
            writer.name("wheelTurns").value(e.getWheelTurns());
            TypeHandler handler = typeSerializationLibrary.getTypeHandlerFromClass(Vector2i.class);
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
            TypeHandler handler = typeSerializationLibrary.getTypeHandlerFromClass(MouseAxisEvent.MouseAxis.class);
            GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getMouseAxis(), serializationContext);
            writer.name("mouseAxis").value(data.getAsString());
        } else if (event instanceof BindAxisEvent) {
            BindAxisEvent e = (BindAxisEvent) event;
            writer.name("id").value(e.getId());
            writer.name("value").value(e.getValue());
        } else if (event instanceof BindButtonEvent) {
            BindButtonEvent e = (BindButtonEvent) event;
            TypeHandler handler = typeSerializationLibrary.getTypeHandlerFromClass(ButtonState.class);
            GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getState(), serializationContext);
            writer.name("state").value(data.getAsString());
            writer.name("id");
            writer.beginObject();
            handler = typeSerializationLibrary.getTypeHandlerFromClass(Name.class);
            data = (GsonPersistedData) handler.serialize(e.getId().getModuleName(), serializationContext);
            writer.name("moduleName").value(data.getAsString());
            data = (GsonPersistedData) handler.serialize(e.getId().getObjectName(), serializationContext);
            writer.name("objectName").value(data.getAsString());
            writer.endObject();
        } else if (event instanceof KeyEvent) {
            KeyEvent e = (KeyEvent) event;
            writer.name("keychar").value(e.getKeyCharacter());
            TypeHandler handler = typeSerializationLibrary.getTypeHandlerFromClass(ButtonState.class);
            GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getState(), serializationContext);
            writer.name("state").value(data.getAsString());
            handler = typeSerializationLibrary.getTypeHandlerFromClass(Keyboard.Key.class); // might need to add some things to key
            data = (GsonPersistedData) handler.serialize(e.getKey(), serializationContext);
            writer.name("input").value(data.getAsString());
        } else if (event instanceof MouseButtonEvent) {
            MouseButtonEvent e = (MouseButtonEvent) event;
            TypeHandler handler = typeSerializationLibrary.getTypeHandlerFromClass(ButtonState.class);
            GsonPersistedData data = (GsonPersistedData) handler.serialize(e.getState(), serializationContext);
            writer.name("state").value(data.getAsString());
            handler = typeSerializationLibrary.getTypeHandlerFromClass(MouseInput.class);
            data = (GsonPersistedData) handler.serialize(e.getButton(), serializationContext);
            writer.name("button").value(data.getAsString());
            handler = typeSerializationLibrary.getTypeHandlerFromClass(Vector2i.class);
            data = (GsonPersistedData) handler.serialize(e.getMousePosition(), serializationContext);
            writer.name("mousePosition");
            writer.beginObject();
            JsonArray array = data.getElement().getAsJsonArray();
            writer.name("x").value(array.get(0).getAsInt());
            writer.name("y").value(array.get(1).getAsInt());
            writer.endObject();
        } else {
            System.out.println("ERROR, EVENT NOT COMPATIBLE");
        }
    }

    public List<RecordedEvent> deserializeRecordedEvents(String path) {
        List<RecordedEvent> events = new ArrayList<>();
        JsonObject jsonObject;
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(path));
            jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonEvents = jsonObject.getAsJsonArray("events");
            String clazz;
            long refId;
            long timestamp;
            long position;
            Event event;
            for (JsonElement element : jsonEvents) {
                jsonObject = element.getAsJsonObject();
                clazz = jsonObject.get("event_class").getAsString();
                refId = jsonObject.get("entityRef_ID").getAsLong();
                position = jsonObject.get("position").getAsLong();
                timestamp = jsonObject.get("timestamp").getAsLong();
                event = deserializeSpecificEventData(jsonObject.get("event_data").getAsJsonObject(), clazz);
                RecordedEvent re = new RecordedEvent(refId, event, timestamp, position);
                events.add(re);
            }
        } catch (Exception e) {
            e.printStackTrace(); // put this into a logger
        }

        return events;
    }

    private Event deserializeSpecificEventData(JsonObject jsonObject, String clazz) {
        Event result = null;
        GsonDeserializationContext deserializationContext = new GsonDeserializationContext(null);
        if (clazz.equals(CameraTargetChangedEvent.class.toString())) {
            EntityRef oldTarget = this.entityManager.getEntity(jsonObject.get("OldTarget").getAsLong());
            EntityRef newTarget = this.entityManager.getEntity(jsonObject.get("NewTarget").getAsLong());
            result = new CameraTargetChangedEvent(oldTarget, newTarget);
        } else if (clazz.equals(PlaySoundEvent.class.toString())) {
            float volume = jsonObject.get("volume").getAsFloat();
            GsonPersistedData data = new GsonPersistedData(jsonObject.get("sound"));
            TypeHandler handler = typeSerializationLibrary.getTypeHandlerFromClass(StaticSound.class);
            StaticSound sound = (StaticSound) handler.deserialize(data, deserializationContext);
            result = new PlaySoundEvent(sound, volume);
        } else if (clazz.equals(CharacterMoveInputEvent.class.toString())) {
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
        } else if (getInputEventSpecificType(jsonObject, clazz, deserializationContext) != null) { //input events
            result = getInputEventSpecificType(jsonObject, clazz, deserializationContext);
        }

        return result;
    }

    private InputEvent getInputEventSpecificType(JsonObject jsonObject, String c, GsonDeserializationContext deserializationContext) {
        InputEvent newEvent;


        if (c.equals(ChatButton.class.toString())) { //BindButtonEvent
            newEvent = new ChatButton();
        } else if (c.equals(ConsoleButton.class.toString())) {
            newEvent = new ConsoleButton();
        } else if (c.equals(HideHUDButton.class.toString())) {
            newEvent = new HideHUDButton();
        } else if (c.equals(OnlinePlayersButton.class.toString())) {
            newEvent = new OnlinePlayersButton();
        } else if (c.equals(PauseButton.class.toString())) {
            newEvent = new PauseButton();
        } else if (c.equals(ScreenshotButton.class.toString())) {
            newEvent = new ScreenshotButton();
        } else if (c.equals(AttackButton.class.toString())) {
            newEvent = new AttackButton();
        } else if (c.equals(FrobButton.class.toString())) {
            newEvent = new FrobButton();
        } else if (c.equals(UseItemButton.class.toString())) {
            newEvent = new UseItemButton();
        } else if (c.equals(AutoMoveButton.class.toString())) {
            newEvent = new AutoMoveButton();
        } else if (c.equals(BackwardsButton.class.toString())) {
            newEvent = new BackwardsButton();
        } else if (c.equals(CrouchButton.class.toString())) {
            newEvent = new CrouchButton();
        } else if (c.equals(ForwardsButton.class.toString())) {
            newEvent = new ForwardsButton();
        } else if (c.equals(JumpButton.class.toString())) {
            newEvent = new JumpButton();
        } else if (c.equals(LeftStrafeButton.class.toString())) {
            newEvent = new LeftStrafeButton();
        } else if (c.equals(RightStrafeButton.class.toString())) {
            newEvent = new RightStrafeButton();
        } else if (c.equals(ToggleSpeedPermanentlyButton.class.toString())) {
            newEvent = new ToggleSpeedPermanentlyButton();
        } else if (c.equals(ToggleSpeedTemporarilyButton.class.toString())) {
            newEvent = new ToggleSpeedTemporarilyButton();
        } else if (c.equals(BTEditorButton.class.toString())) {
            newEvent = new BTEditorButton();
        } else if (c.equals(DecreaseViewDistanceButton.class.toString())) {
            newEvent = new DecreaseViewDistanceButton();
        } else if (c.equals(IncreaseViewDistanceButton.class.toString())) {
            newEvent = new IncreaseViewDistanceButton();
        } else if (c.equals(NUIEditorButton.class.toString())) {
            newEvent = new NUIEditorButton();
        } else if (c.equals(NUISkinEditorButton.class.toString())) {
            newEvent = new NUISkinEditorButton();
        } else if (c.equals(ForwardsMovementAxis.class.toString())) { //BindAxisEvent
            newEvent = new ForwardsMovementAxis();
        } else if (c.equals(ForwardsRealMovementAxis.class.toString())) {
            newEvent = new ForwardsRealMovementAxis();
        } else if (c.equals(RotationPitchAxis.class.toString())) {
            newEvent = new RotationPitchAxis();
        } else if (c.equals(RotationYawAxis.class.toString())) {
            newEvent = new RotationYawAxis();
        } else if (c.equals(StrafeMovementAxis.class.toString())) {
            newEvent = new StrafeMovementAxis();
        } else if (c.equals(StrafeRealMovementAxis.class.toString())) {
            newEvent = new StrafeRealMovementAxis();
        } else if (c.equals(VerticalMovementAxis.class.toString())) {
            newEvent = new VerticalMovementAxis();
        } else if (c.equals(VerticalRealMovementAxis.class.toString())) {
            newEvent = new VerticalRealMovementAxis();
        } else if (c.equals(KeyDownEvent.class.toString()) || c.equals(KeyRepeatEvent.class.toString()) || c.equals(KeyUpEvent.class.toString())) { //KeyEvent
            GsonPersistedData data = new GsonPersistedData(jsonObject.get("input"));
            TypeHandler typeHandler = typeSerializationLibrary.getTypeHandlerFromClass(Keyboard.Key.class);
            Keyboard.Key input = (Keyboard.Key) typeHandler.deserialize(data, deserializationContext);
            data = new GsonPersistedData(jsonObject.get("state"));
            typeHandler = typeSerializationLibrary.getTypeHandlerFromClass(ButtonState.class);
            ButtonState state = (ButtonState) typeHandler.deserialize(data, deserializationContext);
            char keychar = jsonObject.get("keychar").getAsCharacter();
            float delta = jsonObject.get("delta").getAsFloat();
            KeyEvent aux;
            if (c.equals(KeyDownEvent.class.toString())) {
                aux = KeyDownEvent.create(input, keychar, delta);
            } else if (c.equals(KeyRepeatEvent.class.toString())) {
                aux = KeyRepeatEvent.create(input, keychar, delta);
            } else {
                aux = KeyUpEvent.create(input, keychar, delta);
            }
            aux.setState(state);
            newEvent = aux;
        } else if (c.equals(MouseButtonEvent.class.toString())) {
            GsonPersistedData data = new GsonPersistedData(jsonObject.get("button"));
            TypeHandler typeHandler = typeSerializationLibrary.getTypeHandlerFromClass(MouseInput.class);
            MouseInput button = (MouseInput) typeHandler.deserialize(data, deserializationContext);
            data = new GsonPersistedData(jsonObject.get("state"));
            typeHandler = typeSerializationLibrary.getTypeHandlerFromClass(ButtonState.class);
            ButtonState state = (ButtonState) typeHandler.deserialize(data, deserializationContext);
            JsonObject aux = jsonObject.get("mousePosition").getAsJsonObject();
            Vector2i mousePosition = new Vector2i(aux.get("x").getAsInt(), aux.get("y").getAsInt());
            float delta = jsonObject.get("delta").getAsFloat();
            MouseButtonEvent event = new MouseButtonEvent(button, state, delta);
            event.setMousePosition(mousePosition);
            newEvent = event;
        } else if (c.equals(MouseAxisEvent.class.toString())) {
            GsonPersistedData data = new GsonPersistedData(jsonObject.get("mouseAxis"));
            TypeHandler typeHandler = typeSerializationLibrary.getTypeHandlerFromClass(MouseAxisEvent.MouseAxis.class);
            MouseAxisEvent.MouseAxis mouseAxis = (MouseAxisEvent.MouseAxis) typeHandler.deserialize(data, deserializationContext);
            float value = jsonObject.get("value").getAsFloat();
            float delta = jsonObject.get("delta").getAsFloat();
            newEvent = MouseAxisEvent.create(mouseAxis, value, delta);
        } else if (c.equals(MouseWheelEvent.class.toString())) {
            JsonObject aux = jsonObject.get("mousePosition").getAsJsonObject();
            Vector2i mousePosition = new Vector2i(aux.get("x").getAsInt(), aux.get("y").getAsInt());
            int wheelTurns = jsonObject.get("wheelTurns").getAsInt();
            float delta = jsonObject.get("delta").getAsFloat();
            newEvent = new MouseWheelEvent(mousePosition, wheelTurns, delta);
        } else {
            System.out.println("Not an Input Event"); // change to logger
            return null;
        }

        if (newEvent instanceof BindButtonEvent) {
            bindButtonEventSetup((BindButtonEvent) newEvent, jsonObject, deserializationContext);
        } else if (newEvent instanceof BindAxisEvent) {
            bindAxisEvent((BindAxisEvent) newEvent, jsonObject);
        }

        inputEventSetup(newEvent, jsonObject);
        return newEvent;
    }

    private void bindButtonEventSetup(BindButtonEvent event, JsonObject jsonObject, GsonDeserializationContext deserializationContext) {
        GsonPersistedData data = new GsonPersistedData(jsonObject.get("state"));
        TypeHandler typeHandler = typeSerializationLibrary.getTypeHandlerFromClass(ButtonState.class);
        ButtonState state = (ButtonState) typeHandler.deserialize(data, deserializationContext);
        float delta = jsonObject.get("delta").getAsFloat();
        typeHandler = typeSerializationLibrary.getTypeHandlerFromClass(Name.class);
        JsonObject aux = jsonObject.get("id").getAsJsonObject();
        data = new GsonPersistedData(aux.get("moduleName"));
        Name moduleName = (Name) typeHandler.deserialize(data, deserializationContext);
        data = new GsonPersistedData(aux.get("objectName"));
        Name objectName = (Name) typeHandler.deserialize(data, deserializationContext);
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
        EntityRef target = this.entityManager.getEntity(jsonObject.get("target").getAsLong());
        JsonObject aux = jsonObject.get("hitNormal").getAsJsonObject();
        Vector3f hitNormal = new Vector3f(aux.get("x").getAsFloat(), aux.get("y").getAsFloat(), aux.get("z").getAsFloat());
        aux = jsonObject.get("hitPosition").getAsJsonObject();
        Vector3f hitPosition = new Vector3f(aux.get("x").getAsFloat(), aux.get("y").getAsFloat(), aux.get("z").getAsFloat());
        aux = jsonObject.get("targetBlockPosition").getAsJsonObject();
        Vector3i targetBlockPosition = new Vector3i(aux.get("x").getAsInt(), aux.get("y").getAsInt(), aux.get("z").getAsInt());
        event.setTargetInfo(target, targetBlockPosition, hitPosition, hitNormal);
    }
}

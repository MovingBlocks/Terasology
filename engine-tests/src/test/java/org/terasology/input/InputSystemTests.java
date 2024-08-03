// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input;

import com.google.common.collect.Queues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.core.subsystem.config.BindsManager;
import org.terasology.engine.core.subsystem.headless.device.TimeSystem;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.internal.DoNotAutoRegister;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.BindableButton;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.engine.input.cameraTarget.CameraTargetSystem;
import org.terasology.engine.input.events.KeyEvent;
import org.terasology.engine.input.internal.BindableButtonImpl;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.input.Keyboard.Key;
import org.terasology.input.Keyboard.KeyId;
import org.terasology.input.device.CharKeyboardAction;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.RawKeyboardAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InputSystemTests {

    private InputSystem inputSystem;

    private TestKeyboard testKeyboard;

    private EntityRef clientEntity;
    private List<CapturedKeyEvent> clientEntityKeyEvents;
    private EntityRef characterEntity;
    private List<CapturedKeyEvent> characterEntityKeyEvents;

    private BindsManager bindsManager;

    @BeforeEach
    public void setUp() {

        Context context = new ContextImpl();

        setUpLocalPlayer(context);
        setUpDisplayDevice(context);
        setUpBindsManager(context);
        setUpTargetSystem(context);
        context.put(Time.class, new TimeSystem());

        inputSystem = new InputSystem();
        InjectionHelper.inject(inputSystem, context);

        testKeyboard = new TestKeyboard();
        inputSystem.setKeyboardDevice(testKeyboard);

        clientEntityKeyEvents = new ArrayList<>();
        characterEntityKeyEvents = new ArrayList<>();
    }

    private void setUpLocalPlayer(Context context) {
        LocalPlayer localPlayer = new LocalPlayer();
        localPlayer.setRecordAndReplayClasses(new DirectionAndOriginPosRecorderList(), new RecordAndReplayCurrentStatus());
        clientEntity = mock(EntityRef.class);
        ClientComponent clientComponent = new ClientComponent();
        characterEntity = mock(EntityRef.class);
        clientComponent.character = characterEntity;
        when(clientEntity.getComponent(ClientComponent.class)).thenReturn(clientComponent);
        localPlayer.setClientEntity(clientEntity);
        context.put(LocalPlayer.class, localPlayer);

        registerEntityKeyCapturing();
    }

    private void registerEntityKeyCapturing() {
        /*
         * KeyUpEvent and KeyDownEvent are singletons with a single instance that gets reset each time.
         * Therefore it is not possible to capture multiple events with an ArgumentCaptor
         * because previous instances become invalid.
         * For this reason, the data of these events is captured to a separate class.
         */
        when(clientEntity.send(Mockito.any())).then(invocation -> {
            Event event = invocation.getArgument(0);
            if (event instanceof KeyEvent) {
                clientEntityKeyEvents.add(new CapturedKeyEvent((KeyEvent) event));
            }
            return event;
        });
        when(characterEntity.send(Mockito.any())).then(invocation -> {
            Event event = invocation.getArgument(0);
            if (event instanceof KeyEvent) {
                characterEntityKeyEvents.add(new CapturedKeyEvent((KeyEvent) event));
            }
            return event;
        });
    }

    private void setUpDisplayDevice(Context context) {
        DisplayDevice displayDevice = mock(DisplayDevice.class);
        when(displayDevice.hasFocus()).thenReturn(true);
        context.put(DisplayDevice.class, displayDevice);
    }

    private void setUpBindsManager(Context context) {
        bindsManager = mock(BindsManager.class);
        context.put(BindsManager.class, bindsManager);
    }

    private void setUpTargetSystem(Context context) {
        CameraTargetSystem targetSystem = mock(CameraTargetSystem.class);
        context.put(CameraTargetSystem.class, targetSystem);
    }

    @Test
    public void testNoInput() {
        inputSystem.update(1f);

        verify(clientEntity, never()).send(any());
        verify(characterEntity, never()).send(any());
    }

    @Test
    public void testSingleKeyPress() {
        pressKey(Key.W);
        float delta = 1f;

        inputSystem.update(delta);

        assertEquals(clientEntityKeyEvents.size(), 1);
        CapturedKeyEvent clientEvent = clientEntityKeyEvents.get(0);
        assertEquals(clientEvent.key, Key.W);
        assertEquals(clientEvent.delta, delta, 0f);
        assertEquals(clientEvent.buttonState, ButtonState.DOWN);

        assertEquals(characterEntityKeyEvents.size(), 1);
        CapturedKeyEvent characterEvent = characterEntityKeyEvents.get(0);
        assertEquals(characterEvent.key, Key.W);
        assertEquals(characterEvent.delta, delta);
        assertEquals(characterEvent.buttonState, ButtonState.DOWN);
    }

    @Test
    public void testSingleKeyRelease() {
        releaseKey(Key.W);
        float delta = 1f;

        inputSystem.update(delta);

        assertEquals(clientEntityKeyEvents.size(), 1);
        CapturedKeyEvent clientEvent = clientEntityKeyEvents.get(0);
        assertEquals(clientEvent.key, Key.W);
        assertEquals(clientEvent.delta, delta, 0f);
        assertEquals(clientEvent.buttonState, ButtonState.UP);

        assertEquals(characterEntityKeyEvents.size(), 1);
        CapturedKeyEvent characterEvent = characterEntityKeyEvents.get(0);
        assertEquals(characterEvent.key, Key.W);
        assertEquals(characterEvent.delta, delta);
        assertEquals(characterEvent.buttonState, ButtonState.UP);
    }

    @Test
    public void testKeyOrder() {
        pressAndReleaseKey(Key.A);
        pressAndReleaseKey(Key.B);
        pressAndReleaseKey(Key.C);

        inputSystem.update(1f);

        assertEquals(clientEntityKeyEvents.size(), 6);
        assertEquals(clientEntityKeyEvents.get(0).key, Key.A);
        assertEquals(clientEntityKeyEvents.get(0).buttonState, ButtonState.DOWN);
        assertEquals(clientEntityKeyEvents.get(1).key, Key.A);
        assertEquals(clientEntityKeyEvents.get(1).buttonState, ButtonState.UP);
        assertEquals(clientEntityKeyEvents.get(2).key, Key.B);
        assertEquals(clientEntityKeyEvents.get(2).buttonState, ButtonState.DOWN);
        assertEquals(clientEntityKeyEvents.get(3).key, Key.B);
        assertEquals(clientEntityKeyEvents.get(3).buttonState, ButtonState.UP);
        assertEquals(clientEntityKeyEvents.get(4).key, Key.C);
        assertEquals(clientEntityKeyEvents.get(4).buttonState, ButtonState.DOWN);
        assertEquals(clientEntityKeyEvents.get(5).key, Key.C);
        assertEquals(clientEntityKeyEvents.get(5).buttonState, ButtonState.UP);
    }

    @Test
    public void testKeyBinding() {
        Map<Integer, BindableButton> keyBinds = new HashMap<>();
        //mock binding to the TestEventButton, this is done by the BindsManager over the annotations by default
        keyBinds.put(KeyId.T, new BindableButtonImpl(new SimpleUri("engine-tests", "testEvent"), "theTestEvent", new TestEventButton()));

        when(bindsManager.getKeyBinds()).thenReturn(keyBinds);

        pressKey(Key.T);

        inputSystem.update(1f);

        verify(clientEntity).send(Mockito.any(TestEventButton.class));
    }

    private void pressAndReleaseKey(Key key) {
        pressKey(key);
        releaseKey(key);
    }

    private void pressKey(Key key) {
        RawKeyboardAction rawKeyboardAction = new RawKeyboardAction(key, ButtonState.DOWN);
        testKeyboard.add(rawKeyboardAction);
    }

    private void releaseKey(Key key) {
        RawKeyboardAction rawKeyboardAction = new RawKeyboardAction(key, ButtonState.UP);
        testKeyboard.add(rawKeyboardAction);
    }

    private static class TestKeyboard implements KeyboardDevice {

        private Queue<RawKeyboardAction> queue = new LinkedBlockingQueue<>();

        @Override
        public Queue<RawKeyboardAction> getInputQueue() {
            return queue;
        }

        @Override
        public Queue<CharKeyboardAction> getCharInputQueue() {
            return Queues.newArrayDeque();
        }

        @Override
        public boolean isKeyDown(int key) {
            return false;
        }

        public void add(RawKeyboardAction action) {
            queue.add(action);
        }

    }

    @DoNotAutoRegister
    @RegisterBindButton(id = "testEvent", description = "${engine-tests:menu#theTestEvent}", category = "tests")
    @DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.T)
    public class TestEventButton extends BindButtonEvent {
        //the annotations are not used in this tests but represent the way a binding is registered by default
    }

    private static class CapturedKeyEvent {

        public Input key;
        public float delta;
        private ButtonState buttonState;

        CapturedKeyEvent(KeyEvent event) {
            key = event.getKey();
            delta = event.getDelta();
            buttonState = event.getState();
        }

    }
}

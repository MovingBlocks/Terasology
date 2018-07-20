
package org.terasology.input;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.Time;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.engine.subsystem.headless.device.TimeSystem;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.input.Keyboard.Key;
import org.terasology.input.Keyboard.KeyId;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.input.device.KeyboardAction;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.internal.BindableButtonImpl;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.recording.DirectionAndOriginPosRecorderList;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.registry.InjectionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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

    @Before
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

        assertThat(clientEntityKeyEvents.size(), is(1));
        CapturedKeyEvent clientEvent = clientEntityKeyEvents.get(0);
        assertThat(clientEvent.key, is(Key.W));
        assertThat(clientEvent.keyCharacter, is(characterFor(Key.W)));
        assertThat(clientEvent.delta, is(delta));
        assertThat(clientEvent.buttonState, is(ButtonState.DOWN));

        assertThat(characterEntityKeyEvents.size(), is(1));
        CapturedKeyEvent characterEvent = characterEntityKeyEvents.get(0);
        assertThat(characterEvent.key, is(Key.W));
        assertThat(characterEvent.keyCharacter, is(characterFor(Key.W)));
        assertThat(characterEvent.delta, is(delta));
        assertThat(characterEvent.buttonState, is(ButtonState.DOWN));
    }

    @Test
    public void testSingleKeyRelease() {
        releaseKey(Key.W);
        float delta = 1f;

        inputSystem.update(delta);

        assertThat(clientEntityKeyEvents.size(), is(1));
        CapturedKeyEvent clientEvent = clientEntityKeyEvents.get(0);
        assertThat(clientEvent.key, is(Key.W));
        assertThat(clientEvent.keyCharacter, is(characterFor(Key.W)));
        assertThat(clientEvent.delta, is(delta));
        assertThat(clientEvent.buttonState, is(ButtonState.UP));

        assertThat(characterEntityKeyEvents.size(), is(1));
        CapturedKeyEvent characterEvent = characterEntityKeyEvents.get(0);
        assertThat(characterEvent.key, is(Key.W));
        assertThat(characterEvent.keyCharacter, is(characterFor(Key.W)));
        assertThat(characterEvent.delta, is(delta));
        assertThat(characterEvent.buttonState, is(ButtonState.UP));
    }

    @Test
    public void testKeyOrder() {
        pressAndReleaseKey(Key.A);
        pressAndReleaseKey(Key.B);
        pressAndReleaseKey(Key.C);

        inputSystem.update(1f);

        assertThat(clientEntityKeyEvents.size(), is(6));
        assertThat(clientEntityKeyEvents.get(0).key, is(Key.A));
        assertThat(clientEntityKeyEvents.get(0).buttonState, is(ButtonState.DOWN));
        assertThat(clientEntityKeyEvents.get(1).key, is(Key.A));
        assertThat(clientEntityKeyEvents.get(1).buttonState, is(ButtonState.UP));
        assertThat(clientEntityKeyEvents.get(2).key, is(Key.B));
        assertThat(clientEntityKeyEvents.get(2).buttonState, is(ButtonState.DOWN));
        assertThat(clientEntityKeyEvents.get(3).key, is(Key.B));
        assertThat(clientEntityKeyEvents.get(3).buttonState, is(ButtonState.UP));
        assertThat(clientEntityKeyEvents.get(4).key, is(Key.C));
        assertThat(clientEntityKeyEvents.get(4).buttonState, is(ButtonState.DOWN));
        assertThat(clientEntityKeyEvents.get(5).key, is(Key.C));
        assertThat(clientEntityKeyEvents.get(5).buttonState, is(ButtonState.UP));
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
        KeyboardAction keyboardAction = new KeyboardAction(key, ButtonState.DOWN, characterFor(key));
        testKeyboard.add(keyboardAction);
    }

    private void releaseKey(Key key) {
        KeyboardAction keyboardAction = new KeyboardAction(key, ButtonState.UP, characterFor(key));
        testKeyboard.add(keyboardAction);
    }

    private static char characterFor(Key key) {
        //internal key chars depend on the lwjgl keyboard, this works for all letters
        String displayName = key.getDisplayName();
        if (displayName.length() == 1) {
            return displayName.charAt(0);
        } else {
            return ' ';
        }
    }

    private static class TestKeyboard implements KeyboardDevice {

        private Queue<KeyboardAction> queue = new LinkedBlockingQueue<>();

        @Override
        public Queue<KeyboardAction> getInputQueue() {
            return queue;
        }

        @Override
        public boolean isKeyDown(int key) {
            return false;
        }

        public void add(KeyboardAction action) {
            queue.add(action);
        }

    }

    @RegisterBindButton(id = "testEvent", description = "${engine-tests:menu#theTestEvent}", repeating = false, category = "tests")
    @DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.T)
    public class TestEventButton extends BindButtonEvent {
        //the annotations are not used in this tests but represent the way a binding is registered by default
    }

    private static class CapturedKeyEvent {

        public Input key;
        public float delta;
        public char keyCharacter;
        private ButtonState buttonState;

        public CapturedKeyEvent(KeyEvent event) {
            key = event.getKey();
            delta = event.getDelta();
            keyCharacter = event.getKeyCharacter();
            buttonState = event.getState();
        }

    }
}

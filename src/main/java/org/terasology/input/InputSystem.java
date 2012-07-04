package org.terasology.input;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventPriority;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.input.*;
import org.terasology.events.input.binds.*;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.mods.miniions.events.ToggleMinionModeButton;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This system processes input, sending it out as events against the LocalPlayer entity.
 * <p/>
 * In addition to raw keyboard and mouse input, the system handles Bind Buttons and Bind Axis, which can be mapped
 * to one or more inputs.
 */
public class InputSystem implements EventHandlerSystem {
    private Logger logger = Logger.getLogger(getClass().getName());

    private float mouseSensitivity = (float) Config.getInstance().getMouseSens();

    private Map<String, BindableAxisImpl> axisLookup = Maps.newHashMap();
    private Map<String, BindableButtonImpl> buttonLookup = Maps.newHashMap();

    private List<BindableAxisImpl> axisBinds = Lists.newArrayList();
    private List<BindableButtonImpl> buttonBinds = Lists.newArrayList();

    // Links between primitive inputs and bind buttons
    private Map<Integer, BindableButtonImpl> keyBinds = Maps.newHashMap();
    private Map<Integer, BindableButtonImpl> mouseButtonBinds = Maps.newHashMap();
    private BindableButtonImpl mouseWheelUpBind;
    private BindableButtonImpl mouseWheelDownBind;

    private LocalPlayer localPlayer;
    private CameraTargetSystem cameraTargetSystem;

    public void initialise() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);

        REPLACE_THIS_WITH_CONFIG();
    }

    @Override
    public void shutdown() {
    }

    public BindableButton registerBindButton(String bindId, String displayName) {
        return registerBindButton(bindId, displayName, new BindButtonEvent());
    }

    public BindableButton registerBindButton(String bindId, String displayName, BindButtonEvent event) {
        BindableButtonImpl bind = new BindableButtonImpl(bindId, displayName, event);
        buttonLookup.put(bindId, bind);
        buttonBinds.add(bind);
        return bind;
    }

    public BindableButton getBindButton(String bindId) {
        return buttonLookup.get(bindId);
    }

    public void linkBindButtonToInput(InputEvent input, String bindId) {
        if (input instanceof KeyEvent) {
            linkBindButtonToKey(((KeyEvent) input).getKey(), bindId);
        } else if (input instanceof MouseButtonEvent) {
            linkBindButtonToMouse(((MouseButtonEvent) input).getButton(), bindId);
        } else if (input instanceof MouseWheelEvent) {
            linkBindButtonToMouseWheel(((MouseWheelEvent) input).getWheelTurns(), bindId);
        }
    }

    public void linkBindButtonToKey(int key, String bindId) {
        BindableButtonImpl bindInfo = buttonLookup.get(bindId);
        keyBinds.put(key, bindInfo);
    }

    public void linkBindButtonToMouse(int mouseButton, String bindId) {
        BindableButtonImpl bindInfo = buttonLookup.get(bindId);
        mouseButtonBinds.put(mouseButton, bindInfo);
    }

    public void linkBindButtonToMouseWheel(int direction, String bindId) {
        if (direction > 0) {
            mouseWheelUpBind = buttonLookup.get(bindId);
        } else if (direction < 0) {
            mouseWheelDownBind = buttonLookup.get(bindId);
        }
    }

    public BindableAxis registerBindAxis(String id, BindableButton positiveButton, BindableButton negativeButton) {
        return registerBindAxis(id, new BindAxisEvent(), positiveButton, negativeButton);
    }

    public BindableAxis registerBindAxis(String id, BindAxisEvent event, String positiveButtonId, String negativeButtonId) {
        return registerBindAxis(id, event, getBindButton(positiveButtonId), getBindButton(negativeButtonId));
    }

    public BindableAxis registerBindAxis(String id, BindAxisEvent event, BindableButton positiveButton, BindableButton negativeButton) {
        BindableAxisImpl axis = new BindableAxisImpl(id, event, positiveButton, negativeButton);
        axisBinds.add(axis);
        axisLookup.put(id, axis);
        return axis;
    }

    public void update(float delta) {
        processMouseInput(delta);
        processKeyboardInput(delta);
        processBindRepeats(delta);
        processBindAxis(delta);
    }

    private void processMouseInput(float delta) {
        while (Mouse.next()) {
            if (Mouse.getEventButton() != -1) {
                int button = Mouse.getEventButton();
                boolean buttonDown = Mouse.getEventButtonState();
                boolean consumed = sendMouseEvent(button, buttonDown, delta);

                BindableButtonImpl bind = mouseButtonBinds.get(button);
                if (bind != null) {
                    bind.updateBindState(buttonDown, delta, localPlayer.getEntity(), cameraTargetSystem.getTarget(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal(), consumed, GUIManager.getInstance().isConsumingInput());
                }
            } else if (Mouse.getEventDWheel() != 0) {
                int wheelMoved = Mouse.getEventDWheel();
                boolean consumed = sendMouseWheelEvent(wheelMoved / 120, delta);

                BindableButtonImpl bind = (wheelMoved > 0) ? mouseWheelUpBind : mouseWheelDownBind;
                if (bind != null) {
                    bind.updateBindState(true, delta, localPlayer.getEntity(), cameraTargetSystem.getTarget(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal(), consumed, GUIManager.getInstance().isConsumingInput());
                    bind.updateBindState(false, delta, localPlayer.getEntity(), cameraTargetSystem.getTarget(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal(), consumed, GUIManager.getInstance().isConsumingInput());
                }
            }
        }
        int deltaX = Mouse.getDX();
        if (deltaX != 0 && !GUIManager.getInstance().isConsumingInput()) {
            MouseAxisEvent event = new MouseXAxisEvent(deltaX * mouseSensitivity, delta);
            setupTarget(event);
            localPlayer.getEntity().send(event);
        }
        int deltaY = Mouse.getDY();
        if (deltaY != 0 && !GUIManager.getInstance().isConsumingInput()) {
            MouseAxisEvent event = new MouseYAxisEvent(deltaY * mouseSensitivity, delta);
            setupTarget(event);
            localPlayer.getEntity().send(event);
        }
    }

    private void setupTarget(InputEvent event) {
        if (cameraTargetSystem.isTargetAvailable()) {
            event.setTarget(cameraTargetSystem.getTarget(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal());
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void sendEventToGUI(MouseButtonEvent mouseEvent, EntityRef entity) {
        if (GUIManager.getInstance().isConsumingInput()) {
            GUIManager.getInstance().processMouseInput(mouseEvent.getButton(), mouseEvent.getState() != ButtonState.UP, 0);
            mouseEvent.consume();
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void sendEventToGUI(MouseWheelEvent mouseEvent, EntityRef entity) {
        if (GUIManager.getInstance().isConsumingInput()) {
            GUIManager.getInstance().processMouseInput(-1, false, mouseEvent.getWheelTurns() * 120);
            mouseEvent.consume();
        }
    }

    private void processKeyboardInput(float delta) {
        boolean guiConsumingInput = GUIManager.getInstance().isConsumingInput();
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            ButtonState state = getButtonState(Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
            boolean consumed = sendKeyEvent(key, state, delta);

            // Update bind
            BindableButtonImpl bind = keyBinds.get(key);
            if (bind != null && !Keyboard.isRepeatEvent()) {
                bind.updateBindState(Keyboard.getEventKeyState(), delta, localPlayer.getEntity(), cameraTargetSystem.getTarget(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal(), consumed, guiConsumingInput);
            }
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void sendEventToGUI(KeyEvent keyEvent, EntityRef entity) {
        if (GUIManager.getInstance().isConsumingInput()) {
            if (keyEvent.getState() != ButtonState.UP) {
                GUIManager.getInstance().processKeyboardInput(keyEvent.getKey());
            }
            keyEvent.consume();
        }
    }

    private void processBindAxis(float delta) {
        for (BindableAxisImpl axis : axisBinds) {
            axis.update(localPlayer.getEntity(), delta, cameraTargetSystem.getTarget(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal());
        }
    }

    private void processBindRepeats(float delta) {
        for (BindableButtonImpl button : buttonBinds) {
            button.update(localPlayer.getEntity(), delta, cameraTargetSystem.getTarget(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal());
        }
    }

    private ButtonState getButtonState(boolean keyDown, boolean repeatEvent) {
        if (repeatEvent) {
            return ButtonState.REPEAT;
        }
        return (keyDown) ? ButtonState.DOWN : ButtonState.UP;
    }

    private boolean sendKeyEvent(int key, ButtonState state, float delta) {
        KeyEvent event;
        switch (state) {
            case UP:
                event = KeyUpEvent.create(key, delta);
                break;
            case DOWN:
                event = KeyDownEvent.create(key, delta);
                break;
            case REPEAT:
                event = KeyRepeatEvent.create(key, delta);
                break;
            default:
                return false;
        }
        setupTarget(event);
        localPlayer.getEntity().send(event);
        return event.isConsumed();
    }

    private boolean sendMouseEvent(int button, boolean buttonDown, float delta) {
        MouseButtonEvent event;
        switch (button) {
            case -1:
                return false;
            case 0:
                event = (buttonDown) ? LeftMouseDownButtonEvent.create(delta) : LeftMouseUpButtonEvent.create(delta);
                break;
            case 1:
                event = (buttonDown) ? RightMouseDownButtonEvent.create(delta) : RightMouseUpButtonEvent.create(delta);
                break;
            default:
                event = (buttonDown) ? MouseDownButtonEvent.create(button, delta) : MouseUpButtonEvent.create(button, delta);
                break;
        }
        setupTarget(event);
        localPlayer.getEntity().send(event);
        return event.isConsumed();
    }

    private boolean sendMouseWheelEvent(int wheelTurns, float delta) {
        MouseWheelEvent mouseWheelEvent = new MouseWheelEvent(wheelTurns, delta);
        setupTarget(mouseWheelEvent);
        localPlayer.getEntity().send(mouseWheelEvent);
        return mouseWheelEvent.isConsumed();
    }

    private void REPLACE_THIS_WITH_CONFIG() {
        registerBindButton(InventoryButton.ID, "Inventory", new InventoryButton());
        linkBindButtonToKey(Keyboard.KEY_I, InventoryButton.ID);

        registerBindButton(ConsoleButton.ID, "Console", new ConsoleButton());
        linkBindButtonToKey(Keyboard.KEY_TAB, ConsoleButton.ID);

        registerBindButton(PauseButton.ID, "Pause", new PauseButton());
        linkBindButtonToKey(Keyboard.KEY_ESCAPE, PauseButton.ID);

        registerBindButton(ForwardsButton.ID, "Forwards", new ForwardsButton());
        linkBindButtonToKey(Keyboard.KEY_W, ForwardsButton.ID);

        registerBindButton(BackwardsButton.ID, "Backwards", new BackwardsButton());
        linkBindButtonToKey(Keyboard.KEY_S, BackwardsButton.ID);

        registerBindAxis(ForwardsMovementAxis.ID, new ForwardsMovementAxis(), ForwardsButton.ID, BackwardsButton.ID).setSendEventMode(BindableAxisImpl.SendEventMode.WHEN_CHANGED);

        registerBindButton(LeftStrafeButton.ID, "Left", new LeftStrafeButton());
        linkBindButtonToKey(Keyboard.KEY_A, LeftStrafeButton.ID);

        registerBindButton(RightStrafeButton.ID, "Right", new RightStrafeButton());
        linkBindButtonToKey(Keyboard.KEY_D, RightStrafeButton.ID);

        registerBindAxis(StrafeMovementAxis.ID, new StrafeMovementAxis(), LeftStrafeButton.ID, RightStrafeButton.ID).setSendEventMode(BindableAxisImpl.SendEventMode.WHEN_CHANGED);

        registerBindButton(JumpButton.ID, "Jump", new JumpButton());
        linkBindButtonToKey(Keyboard.KEY_SPACE, JumpButton.ID);

        registerBindButton(CrouchButton.ID, "Crouch", new CrouchButton());
        linkBindButtonToKey(Keyboard.KEY_C, CrouchButton.ID);

        registerBindAxis(VerticalMovementAxis.ID, new VerticalMovementAxis(), JumpButton.ID, CrouchButton.ID).setSendEventMode(BindableAxisImpl.SendEventMode.WHEN_CHANGED);

        registerBindButton(RunButton.ID, "Run", new RunButton());
        linkBindButtonToKey(Keyboard.KEY_LSHIFT, RunButton.ID);
        linkBindButtonToKey(Keyboard.KEY_RSHIFT, RunButton.ID);

        registerBindButton(AttackButton.ID, "Attack", new AttackButton()).setRepeating(true);
        linkBindButtonToMouse(1, AttackButton.ID);

        registerBindButton(UseItemButton.ID, "Use Held Item", new UseItemButton()).setRepeating(true);
        linkBindButtonToMouse(0, UseItemButton.ID);

        registerBindButton(FrobButton.ID, "Frob", new FrobButton());
        linkBindButtonToKey(Keyboard.KEY_E, FrobButton.ID);

        registerBindButton(ToolbarNextButton.ID, "Toolbar Next", new ToolbarNextButton()).setMode(BindableButtonImpl.ActivateMode.PRESS);
        linkBindButtonToMouseWheel(+1, ToolbarNextButton.ID);

        registerBindButton(ToolbarPrevButton.ID, "Toolbar Previous", new ToolbarPrevButton()).setMode(BindableButtonImpl.ActivateMode.PRESS);
        linkBindButtonToMouseWheel(-1, ToolbarPrevButton.ID);

        for (int i = 0; i < 9; ++i) {
            String inventorySlotBind = "engine:toolbarSlot" + i;
            registerBindButton(inventorySlotBind, "Inventory Slot " + (i + 1), new ToolbarSlotButton(i));
            linkBindButtonToKey(Keyboard.KEY_1 + i, inventorySlotBind);
        }

        registerBindButton(ToggleMinionModeButton.ID, "Toggle Minion Mode", new ToggleMinionModeButton()).setMode(BindableButtonImpl.ActivateMode.PRESS);
        linkBindButtonToKey(Keyboard.KEY_X, ToggleMinionModeButton.ID);
    }

}


	


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
import org.terasology.logic.manager.InputConfig;
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
                    bind.updateBindState(buttonDown, delta, cameraTargetSystem.getTarget(), localPlayer.getEntity(), consumed, GUIManager.getInstance().isConsumingInput());
                }
            } else if (Mouse.getEventDWheel() != 0) {
                int wheelMoved = Mouse.getEventDWheel();
                boolean consumed = sendMouseWheelEvent(wheelMoved / 120, delta);

                BindableButtonImpl bind = (wheelMoved > 0) ? mouseWheelUpBind : mouseWheelDownBind;
                if (bind != null) {
                    bind.updateBindState(true, delta, cameraTargetSystem.getTarget(), localPlayer.getEntity(), consumed, GUIManager.getInstance().isConsumingInput());
                    bind.updateBindState(false, delta, cameraTargetSystem.getTarget(), localPlayer.getEntity(), consumed, GUIManager.getInstance().isConsumingInput());
                }
            }
        }
        int deltaX = Mouse.getDX();
        if (deltaX != 0 && !GUIManager.getInstance().isConsumingInput()) {
            localPlayer.getEntity().send(new MouseXAxisEvent(deltaX * mouseSensitivity, delta, cameraTargetSystem.getTarget()));
        }
        int deltaY = Mouse.getDY();
        if (deltaY != 0 && !GUIManager.getInstance().isConsumingInput()) {
            localPlayer.getEntity().send(new MouseYAxisEvent(deltaY * mouseSensitivity, delta, cameraTargetSystem.getTarget()));
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
                bind.updateBindState(Keyboard.getEventKeyState(), delta, cameraTargetSystem.getTarget(), localPlayer.getEntity(), consumed, guiConsumingInput);
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
            axis.update(localPlayer.getEntity(), delta, cameraTargetSystem.getTarget());
        }
    }

    private void processBindRepeats(float delta) {
        for (BindableButtonImpl button : buttonBinds) {
            button.update(localPlayer.getEntity(), delta, cameraTargetSystem.getTarget());
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
                event = KeyUpEvent.create(key, delta, cameraTargetSystem.getTarget());
                break;
            case DOWN:
                event = KeyDownEvent.create(key, delta, cameraTargetSystem.getTarget());
                break;
            case REPEAT:
                event = KeyRepeatEvent.create(key, delta, cameraTargetSystem.getTarget());
                break;
            default:
                return false;
        }

        localPlayer.getEntity().send(event);
        return event.isConsumed();
    }

    private boolean sendMouseEvent(int button, boolean buttonDown, float delta) {
        MouseButtonEvent event;
        switch (button) {
            case -1:
                return false;
            case 0:
                event = (buttonDown) ? LeftMouseDownButtonEvent.create(delta, cameraTargetSystem.getTarget()) : LeftMouseUpButtonEvent.create(delta, cameraTargetSystem.getTarget());
                break;
            case 1:
                event = (buttonDown) ? RightMouseDownButtonEvent.create(delta, cameraTargetSystem.getTarget()) : RightMouseUpButtonEvent.create(delta, cameraTargetSystem.getTarget());
                break;
            default:
                event = (buttonDown) ? MouseDownButtonEvent.create(button, delta, cameraTargetSystem.getTarget()) : MouseUpButtonEvent.create(button, delta, cameraTargetSystem.getTarget());
                break;
        }
        localPlayer.getEntity().send(event);
        return event.isConsumed();
    }

    private boolean sendMouseWheelEvent(int wheelTurns, float delta) {
        MouseWheelEvent mouseWheelEvent = new MouseWheelEvent(wheelTurns, delta, cameraTargetSystem.getTarget());
        localPlayer.getEntity().send(mouseWheelEvent);
        return mouseWheelEvent.isConsumed();
    }

    private int linkMouseByConfig(int button) {
        switch (button) {
            case 256:
                return 0;
            case 257:
                return 1;
            case 258:
                return 2;
            case 259:
                return +1;
            case 260:
                return -1;
            default:
                return 2;
        }
    }

    private void REPLACE_THIS_WITH_CONFIG() {
        InputConfig inputConfig = InputConfig.getInstance();
        int keyvalue;

        registerBindButton(InventoryButton.ID, "Inventory", new InventoryButton());
        keyvalue = inputConfig.getKeyInventory();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, InventoryButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), InventoryButton.ID);
        }

        registerBindButton(ConsoleButton.ID, "Console", new ConsoleButton());
        keyvalue = inputConfig.getKeyConsole();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, ConsoleButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ConsoleButton.ID);
        }

        registerBindButton(PauseButton.ID, "Pause", new PauseButton());
        keyvalue = inputConfig.getKeyPauze();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, PauseButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), PauseButton.ID);
        }

        registerBindButton(ForwardsButton.ID, "Forwards", new ForwardsButton());
        keyvalue = inputConfig.getKeyForward();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, ForwardsButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ForwardsButton.ID);
        }

        registerBindButton(BackwardsButton.ID, "Backwards", new BackwardsButton());
        keyvalue = inputConfig.getKeyBackward();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, BackwardsButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), BackwardsButton.ID);
        }

        registerBindAxis(ForwardsMovementAxis.ID, new ForwardsMovementAxis(), ForwardsButton.ID, BackwardsButton.ID).setSendEventMode(BindableAxisImpl.SendEventMode.WHEN_CHANGED);

        registerBindButton(LeftStrafeButton.ID, "Left", new LeftStrafeButton());
        keyvalue = inputConfig.getKeyLeft();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, LeftStrafeButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), LeftStrafeButton.ID);
        }

        registerBindButton(RightStrafeButton.ID, "Right", new RightStrafeButton());
        keyvalue = inputConfig.getKeyRight();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, RightStrafeButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), RightStrafeButton.ID);
        }

        registerBindAxis(StrafeMovementAxis.ID, new StrafeMovementAxis(), LeftStrafeButton.ID, RightStrafeButton.ID).setSendEventMode(BindableAxisImpl.SendEventMode.WHEN_CHANGED);

        registerBindButton(JumpButton.ID, "Jump", new JumpButton());
        keyvalue = inputConfig.getKeyJump();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, JumpButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), JumpButton.ID);
        }

        registerBindButton(CrouchButton.ID, "Crouch", new CrouchButton());
        keyvalue = inputConfig.getKeyCrouch();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, CrouchButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), CrouchButton.ID);
        }

        registerBindAxis(VerticalMovementAxis.ID, new VerticalMovementAxis(), JumpButton.ID, CrouchButton.ID).setSendEventMode(BindableAxisImpl.SendEventMode.WHEN_CHANGED);

        registerBindButton(RunButton.ID, "Run", new RunButton());
        keyvalue = inputConfig.getKeyRun();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, RunButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), RunButton.ID);
        }
        // linkBindButtonToKey(Keyboard.KEY_RSHIFT, RunButton.ID); // necessary?

        registerBindButton(AttackButton.ID, "Attack", new AttackButton()).setRepeating(true);
        keyvalue = inputConfig.getKeyAttack();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, AttackButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), AttackButton.ID);
        }

        registerBindButton(UseItemButton.ID, "Use Held Item", new UseItemButton()).setRepeating(true);
        keyvalue = inputConfig.getKeyUsehelditem();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, UseItemButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), UseItemButton.ID);
        }

        registerBindButton(FrobButton.ID, "Frob", new FrobButton());
        keyvalue = inputConfig.getKeyFrob();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, FrobButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), FrobButton.ID);
        }

        registerBindButton(ToolbarNextButton.ID, "Toolbar Next", new ToolbarNextButton()).setMode(BindableButtonImpl.ActivateMode.PRESS);
        keyvalue = inputConfig.getKeyToolnext();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, ToolbarNextButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ToolbarNextButton.ID);
        }

        registerBindButton(ToolbarPrevButton.ID, "Toolbar Previous", new ToolbarPrevButton()).setMode(BindableButtonImpl.ActivateMode.PRESS);
        keyvalue = inputConfig.getKeyToolprev();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, ToolbarPrevButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ToolbarPrevButton.ID);
        }

        /*keyvalue = inputConfig.getKeyToolslot1();
        if(keyvalue < 256){
            linkBindButtonToKey(keyvalue, ConsoleButton.ID);
        }else{
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ConsoleButton.ID);
        }
        /*
        for (int i = 0; i < 9; ++i) {
            String inventorySlotBind = "engine:toolbarSlot" + i;
            registerBindButton(inventorySlotBind, "Inventory Slot " + (i + 1), new ToolbarSlotButton(i));
            linkBindButtonToKey(Keyboard.KEY_1 + i, inventorySlotBind);
        }*/

        registerBindButton(ToggleMinionModeButton.ID, "Toggle Minion Mode", new ToggleMinionModeButton()).setMode(BindableButtonImpl.ActivateMode.PRESS);
        keyvalue = inputConfig.getKeyMinionmode();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, ToggleMinionModeButton.ID);
        } else {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ToggleMinionModeButton.ID);
        }
    }

}


	


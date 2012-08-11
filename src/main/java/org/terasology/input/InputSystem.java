/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.input;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventPriority;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.input.InputEvent;
import org.terasology.events.input.KeyDownEvent;
import org.terasology.events.input.KeyEvent;
import org.terasology.events.input.KeyRepeatEvent;
import org.terasology.events.input.KeyUpEvent;
import org.terasology.events.input.LeftMouseDownButtonEvent;
import org.terasology.events.input.LeftMouseUpButtonEvent;
import org.terasology.events.input.MouseAxisEvent;
import org.terasology.events.input.MouseButtonEvent;
import org.terasology.events.input.MouseDownButtonEvent;
import org.terasology.events.input.MouseUpButtonEvent;
import org.terasology.events.input.MouseWheelEvent;
import org.terasology.events.input.MouseXAxisEvent;
import org.terasology.events.input.MouseYAxisEvent;
import org.terasology.events.input.RightMouseDownButtonEvent;
import org.terasology.events.input.RightMouseUpButtonEvent;
import org.terasology.events.input.binds.AttackButton;
import org.terasology.events.input.binds.BackwardsButton;
import org.terasology.events.input.binds.ConsoleButton;
import org.terasology.events.input.binds.CrouchButton;
import org.terasology.events.input.binds.DropItemButton;
import org.terasology.events.input.binds.ForwardsButton;
import org.terasology.events.input.binds.ForwardsMovementAxis;
import org.terasology.events.input.binds.FrobButton;
import org.terasology.events.input.binds.InventoryButton;
import org.terasology.events.input.binds.JumpButton;
import org.terasology.events.input.binds.LeftStrafeButton;
import org.terasology.events.input.binds.PauseButton;
import org.terasology.events.input.binds.RightStrafeButton;
import org.terasology.events.input.binds.RunButton;
import org.terasology.events.input.binds.StrafeMovementAxis;
import org.terasology.events.input.binds.ToolbarNextButton;
import org.terasology.events.input.binds.ToolbarPrevButton;
import org.terasology.events.input.binds.ToolbarSlotButton;
import org.terasology.events.input.binds.UseItemButton;
import org.terasology.events.input.binds.VerticalMovementAxis;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.InputConfig;
import org.terasology.mods.miniions.events.ToggleMinionModeButton;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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

        loadInputConfig();
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
            mouseWheelDownBind = buttonLookup.get(bindId);
        } else if (direction < 0) {
            mouseWheelUpBind = buttonLookup.get(bindId);
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
            
            GUIManager.getInstance().processMouseInput(Mouse.getEventButton(), Mouse.getEventButtonState(), Mouse.getEventDWheel());
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

    //TODO Send mouse / keyboard input on another way to the GUIManager. Let the GUI manager directly subscribe to these events.
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

    private void loadInputConfig() {
        InputConfig inputConfig = InputConfig.getInstance();
        int keyvalue;
        /*test*/
        String dropItemBind = "engine:dropItem";
        BindableButton dropBind = registerBindButton(dropItemBind, "Drop Item", new DropItemButton());
        dropBind.setRepeating(true);
        dropBind.setMode(BindableButton.ActivateMode.BOTH);

        linkBindButtonToKey(Keyboard.KEY_Q, dropItemBind);
        /*test*/
        registerBindButton(InventoryButton.ID, "Inventory", new InventoryButton());
        keyvalue = inputConfig.getKeyInventory();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, InventoryButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), InventoryButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), InventoryButton.ID);
        }

        registerBindButton(ConsoleButton.ID, "Console", new ConsoleButton());
        keyvalue = inputConfig.getKeyConsole();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, ConsoleButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ConsoleButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), ConsoleButton.ID);
        }

        registerBindButton(PauseButton.ID, "Pause", new PauseButton());
        keyvalue = inputConfig.getKeyPause();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, PauseButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), PauseButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), PauseButton.ID);
        }

        registerBindButton(ForwardsButton.ID, "Forwards", new ForwardsButton());
        keyvalue = inputConfig.getKeyForward();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, ForwardsButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ForwardsButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), ForwardsButton.ID);
        }

        registerBindButton(BackwardsButton.ID, "Backwards", new BackwardsButton());
        keyvalue = inputConfig.getKeyBackward();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, BackwardsButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), BackwardsButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), BackwardsButton.ID);
        }

        registerBindAxis(ForwardsMovementAxis.ID, new ForwardsMovementAxis(), ForwardsButton.ID, BackwardsButton.ID).setSendEventMode(BindableAxisImpl.SendEventMode.WHEN_CHANGED);

        registerBindButton(LeftStrafeButton.ID, "Left", new LeftStrafeButton());
        keyvalue = inputConfig.getKeyLeft();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, LeftStrafeButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), LeftStrafeButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), LeftStrafeButton.ID);
        }

        registerBindButton(RightStrafeButton.ID, "Right", new RightStrafeButton());
        keyvalue = inputConfig.getKeyRight();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, RightStrafeButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), RightStrafeButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), RightStrafeButton.ID);
        }

        registerBindAxis(StrafeMovementAxis.ID, new StrafeMovementAxis(), LeftStrafeButton.ID, RightStrafeButton.ID).setSendEventMode(BindableAxisImpl.SendEventMode.WHEN_CHANGED);

        registerBindButton(JumpButton.ID, "Jump", new JumpButton());
        keyvalue = inputConfig.getKeyJump();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, JumpButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), JumpButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), JumpButton.ID);
        }

        registerBindButton(CrouchButton.ID, "Crouch", new CrouchButton());
        keyvalue = inputConfig.getKeyCrouch();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, CrouchButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), CrouchButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), CrouchButton.ID);
        }

        registerBindAxis(VerticalMovementAxis.ID, new VerticalMovementAxis(), JumpButton.ID, CrouchButton.ID).setSendEventMode(BindableAxisImpl.SendEventMode.WHEN_CHANGED);

        registerBindButton(RunButton.ID, "Run", new RunButton());
        keyvalue = inputConfig.getKeyRun();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, RunButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), RunButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), RunButton.ID);
        }
        // linkBindButtonToKey(Keyboard.KEY_RSHIFT, RunButton.ID); // necessary?

        registerBindButton(AttackButton.ID, "Attack", new AttackButton()).setRepeating(true);
        keyvalue = inputConfig.getKeyAttack();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, AttackButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), AttackButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), AttackButton.ID);
        }

        registerBindButton(UseItemButton.ID, "Use Held Item", new UseItemButton()).setRepeating(true);
        keyvalue = inputConfig.getKeyUsehelditem();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, UseItemButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), UseItemButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), UseItemButton.ID);
        }

        registerBindButton(FrobButton.ID, "Frob", new FrobButton());
        keyvalue = inputConfig.getKeyFrob();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, FrobButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), FrobButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), FrobButton.ID);
        }

        registerBindButton(ToolbarNextButton.ID, "Toolbar Next", new ToolbarNextButton()).setMode(BindableButtonImpl.ActivateMode.PRESS);
        keyvalue = inputConfig.getKeyToolnext();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, ToolbarNextButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ToolbarNextButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), ToolbarNextButton.ID);
        }

        registerBindButton(ToolbarPrevButton.ID, "Toolbar Previous", new ToolbarPrevButton()).setMode(BindableButtonImpl.ActivateMode.PRESS);
        keyvalue = inputConfig.getKeyToolprev();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, ToolbarPrevButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ToolbarPrevButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), ToolbarPrevButton.ID);
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

        for (int i = 0; i < 9; ++i) {
            String inventorySlotBind = "engine:toolbarSlot" + i;
            registerBindButton(inventorySlotBind, "Inventory Slot " + (i + 1), new ToolbarSlotButton(i));
            linkBindButtonToKey(Keyboard.KEY_1 + i, inventorySlotBind);
        }

        registerBindButton(ToggleMinionModeButton.ID, "Toggle Minion Mode", new ToggleMinionModeButton()).setMode(BindableButtonImpl.ActivateMode.PRESS);
        keyvalue = inputConfig.getKeyMinionmode();
        if (keyvalue < 256) {
            linkBindButtonToKey(keyvalue, ToggleMinionModeButton.ID);
        } else if (keyvalue < 259) {
            linkBindButtonToMouse(linkMouseByConfig(keyvalue), ToggleMinionModeButton.ID);
        } else {
            linkBindButtonToMouseWheel(linkMouseByConfig(keyvalue), ToggleMinionModeButton.ID);
        }
    }

}


	


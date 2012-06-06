package org.terasology.game.client;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.componentSystem.block.BlockEntityRegistry;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.input.*;
import org.terasology.events.input.binds.*;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.BlockRaytracer;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.structures.RayBlockIntersection;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.world.WorldRenderer;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This system processes input, sending it out as events against the LocalPlayer entity.
 *
 * In addition to raw keyboard and mouse input, the system handles Bind Buttons and Bind Axis, which can be mapped
 * to one or more inputs.
 */
public class InputSystem implements EventHandlerSystem {
    private Logger logger = Logger.getLogger(getClass().getName());

    private float mouseSensitivity = (float)Config.getInstance().getMouseSens();

    private Map<String, BindableAxis> axisLookup = Maps.newHashMap();
    private Map<String, BindableButton> buttonLookup = Maps.newHashMap();

    private List<BindableAxis> axisBinds = Lists.newArrayList();
    private List<BindableButton> buttonBinds = Lists.newArrayList();

    // Links between primitive inputs and bind buttons
    private Map<Integer, BindableButton> keyBinds = Maps.newHashMap();
    private Map<Integer, BindableButton> mouseButtonBinds = Maps.newHashMap();
    private BindableButton mouseWheelUpBind;
    private BindableButton mouseWheelDownBind;

    private IWorldProvider worldProvider;
    private LocalPlayer localPlayer;
    private BlockEntityRegistry blockRegistry;
    private EntityRef target = EntityRef.NULL;
    private Vector3i targetBlockPos = null;
    private Timer timer;

    public void initialise() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        blockRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        timer = CoreRegistry.get(Timer.class);

        REPLACE_THIS_WITH_CONFIG();
    }

    public BindableButton registerBindButton(String bindId) {
        return registerBindButton(bindId, new BindButtonEvent());
    }

    public BindableButton registerBindButton(String bindId, BindButtonEvent event) {
        BindableButton bind = new BindableButton(bindId, event);
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
        BindableButton bindInfo = buttonLookup.get(bindId);
        keyBinds.put(key, bindInfo);
    }

    public void linkBindButtonToMouse(int mouseButton, String bindId) {
        BindableButton bindInfo = buttonLookup.get(bindId);
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
        BindableAxis axis = new BindableAxis(id, event, positiveButton, negativeButton);
        axisBinds.add(axis);
        axisLookup.put(id, axis);
        return axis;
    }

    public void update(float delta) {
        updateTarget();
        processMouseInput(delta);
        processKeyboardInput(delta);
        processBindRepeats(delta);
        processBindAxis(delta);
    }

    private void updateTarget() {
        // Repair lost target
        if (!target.exists() && targetBlockPos != null) {
            target = blockRegistry.getOrCreateEntityAt(targetBlockPos);
        }
        // If target no longer exists, out event
        if (!target.exists()) {
            localPlayer.getEntity().send(new MouseOutEvent(target));
            targetBlockPos = null;
        }

        // TODO: This will change when camera are handled better (via a component)
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        // TODO: Check for non-block targets once we have support for that

        RayBlockIntersection.Intersection hitInfo = BlockRaytracer.trace(camera.getPosition(), camera.getViewingDirection(), worldProvider);
        Vector3i newBlockPos = (hitInfo != null) ? hitInfo.getBlockPosition() : null;

        if (!Objects.equal(targetBlockPos, newBlockPos)) {
            if (targetBlockPos != null) {
                localPlayer.getEntity().send(new MouseOutEvent(target));
            }
            target = EntityRef.NULL;
            targetBlockPos = newBlockPos;
            if (newBlockPos != null) {
                target = blockRegistry.getOrCreateEntityAt(targetBlockPos);
                localPlayer.getEntity().send(new MouseOverEvent(target));
            }
        }
    }

    private void processMouseInput(float delta) {
        while (Mouse.next()) {
            if (Mouse.getEventButton() != -1) {
                int button = Mouse.getEventButton();
                boolean buttonDown = Mouse.getEventButtonState();
                boolean consumed = sendMouseEvent(button, buttonDown, delta);

                BindableButton bind = mouseButtonBinds.get(button);
                if (bind != null) {
                    bind.updateBindState(buttonDown, delta, target, localPlayer.getEntity(), consumed, GUIManager.getInstance().isConsumingInput());
                }
            } else if (Mouse.getEventDWheel() != 0) {
                int wheelMoved = Mouse.getEventDWheel();
                boolean consumed = sendMouseWheelEvent(wheelMoved, delta);

                BindableButton bind = (wheelMoved > 0) ? mouseWheelUpBind : mouseWheelDownBind;
                if (bind != null) {
                    bind.updateBindState(true, delta, target, localPlayer.getEntity(), consumed, GUIManager.getInstance().isConsumingInput());
                    bind.updateBindState(false, delta, target, localPlayer.getEntity(), consumed, GUIManager.getInstance().isConsumingInput());
                }
            }
        }
        int deltaX = Mouse.getDX();
        if (deltaX != 0 && !GUIManager.getInstance().isConsumingInput()) {
            localPlayer.getEntity().send(new MouseXAxisEvent(deltaX * mouseSensitivity, delta, target));
        }
        int deltaY = Mouse.getDY();
        if (deltaY != 0 && !GUIManager.getInstance().isConsumingInput()) {
            localPlayer.getEntity().send(new MouseYAxisEvent(deltaY * mouseSensitivity, delta, target));
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class, priority = ReceiveEvent.PRIORITY_HIGH)
    public void sendEventToGUI(MouseButtonEvent mouseEvent, EntityRef entity) {
        if (GUIManager.getInstance().isConsumingInput()) {
            GUIManager.getInstance().processMouseInput(mouseEvent.getButton(), mouseEvent.getState() != ButtonState.UP, 0);
            mouseEvent.consume();
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class, priority = ReceiveEvent.PRIORITY_HIGH)
    public void sendEventToGUI(MouseWheelEvent mouseEvent, EntityRef entity) {
        if (GUIManager.getInstance().isConsumingInput()) {
            GUIManager.getInstance().processMouseInput(-1, false, mouseEvent.getWheelTurns());
            UIDisplayWindow focusWindow = GUIManager.getInstance().getFocusedWindow();
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
            BindableButton bind = keyBinds.get(key);
            if (bind != null && !Keyboard.isRepeatEvent()) {
                bind.updateBindState(Keyboard.getEventKeyState(), delta, target, localPlayer.getEntity(), consumed, guiConsumingInput);
            }
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class, priority = ReceiveEvent.PRIORITY_HIGH)
    public void sendEventToGUI(KeyEvent keyEvent, EntityRef entity) {
        if (GUIManager.getInstance().isConsumingInput()) {
            if (keyEvent.getState() != ButtonState.UP) {
                GUIManager.getInstance().processKeyboardInput(keyEvent.getKey());
            }
            keyEvent.consume();
        }
    }

    private void processBindAxis(float delta) {
        for (BindableAxis axis : axisBinds) {
            axis.update(localPlayer.getEntity(), delta, target);
        }
    }

    private void processBindRepeats(float delta) {
        for (BindableButton button : buttonBinds) {
            button.update(localPlayer.getEntity(), delta, target);
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
                event = KeyUpEvent.create(key, delta, target);
                break;
            case DOWN:
                event = KeyDownEvent.create(key, delta, target);
                break;
            case REPEAT:
                event = KeyRepeatEvent.create(key, delta, target);
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
                event = (buttonDown) ? LeftMouseDownButtonEvent.create(delta, target) : LeftMouseUpButtonEvent.create(delta, target);
                break;
            case 1:
                event = (buttonDown) ? RightMouseDownButtonEvent.create(delta, target) : RightMouseUpButtonEvent.create(delta, target);
                break;
            default:
                event = (buttonDown) ? MouseDownButtonEvent.create(button, delta, target) : MouseUpButtonEvent.create(button, delta, target);
                break;
        }
        localPlayer.getEntity().send(event);
        return event.isConsumed();
    }

    private boolean sendMouseWheelEvent(int wheelTurns, float delta) {
        MouseWheelEvent mouseWheelEvent = new MouseWheelEvent(wheelTurns, delta, target);
        localPlayer.getEntity().send(mouseWheelEvent);
        return mouseWheelEvent.isConsumed();
    }

    private void REPLACE_THIS_WITH_CONFIG() {
        registerBindButton(InventoryButton.ID, new InventoryButton());
        linkBindButtonToKey(Keyboard.KEY_I, InventoryButton.ID);

        registerBindButton(ConsoleButton.ID, new ConsoleButton());
        linkBindButtonToKey(Keyboard.KEY_TAB, ConsoleButton.ID);

        registerBindButton(PauseButton.ID, new PauseButton());
        linkBindButtonToKey(Keyboard.KEY_ESCAPE, PauseButton.ID);

        registerBindButton(ForwardsButton.ID, new ForwardsButton());
        linkBindButtonToKey(Keyboard.KEY_W, ForwardsButton.ID);

        registerBindButton(BackwardsButton.ID,  new BackwardsButton());
        linkBindButtonToKey(Keyboard.KEY_S, BackwardsButton.ID);

        registerBindAxis(ForwardsMovementAxis.ID, new ForwardsMovementAxis(), ForwardsButton.ID, BackwardsButton.ID).setSendEventMode(BindableAxis.SendEventMode.WHEN_CHANGED);

        registerBindButton(LeftStrafeButton.ID, new LeftStrafeButton());
        linkBindButtonToKey(Keyboard.KEY_A, LeftStrafeButton.ID);

        registerBindButton(RightStrafeButton.ID, new RightStrafeButton());
        linkBindButtonToKey(Keyboard.KEY_D, RightStrafeButton.ID);

        registerBindAxis(StrafeMovementAxis.ID, new StrafeMovementAxis(), LeftStrafeButton.ID, RightStrafeButton.ID).setSendEventMode(BindableAxis.SendEventMode.WHEN_CHANGED);

        registerBindButton(JumpButton.ID, new JumpButton());
        linkBindButtonToKey(Keyboard.KEY_SPACE, JumpButton.ID);

        registerBindButton(CrouchButton.ID, new CrouchButton());
        linkBindButtonToKey(Keyboard.KEY_C, CrouchButton.ID);

        registerBindAxis(VerticalMovementAxis.ID, new VerticalMovementAxis(), JumpButton.ID, CrouchButton.ID).setSendEventMode(BindableAxis.SendEventMode.WHEN_CHANGED);

        registerBindButton(RunButton.ID, new RunButton());
        linkBindButtonToKey(Keyboard.KEY_LSHIFT, RunButton.ID);
        linkBindButtonToKey(Keyboard.KEY_RSHIFT, RunButton.ID);

        registerBindButton(AttackButton.ID, new AttackButton()).setRepeating(true);
        linkBindButtonToMouse(1, AttackButton.ID);

        registerBindButton(UseItemButton.ID, new UseItemButton()).setRepeating(true);
        linkBindButtonToMouse(0, UseItemButton.ID);

        registerBindButton(FrobButton.ID, new FrobButton());
        linkBindButtonToKey(Keyboard.KEY_E, FrobButton.ID);

        registerBindButton(ToolbarNextButton.ID, new ToolbarNextButton());
        linkBindButtonToMouseWheel(+1, ToolbarNextButton.ID);

        registerBindButton(ToolbarPrevButton.ID, new ToolbarPrevButton());
        linkBindButtonToMouseWheel(-1, ToolbarPrevButton.ID);
    }

}


	


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
import org.terasology.events.input.binds.ConsoleButton;
import org.terasology.events.input.binds.InventoryButton;
import org.terasology.events.input.binds.PauseButton;
import org.terasology.game.CoreRegistry;
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

    private Map<String, BindAxis> axisLookup = Maps.newHashMap();
    private Map<String, BindButton> buttonBinds = Maps.newHashMap();

    private List<BindAxis> axisBinds = Lists.newArrayList();

    // Links between primitive inputs and bind buttons
    private Map<Integer, BindButton> keyBinds = Maps.newHashMap();
    private Map<Integer, BindButton> mouseButtonBinds = Maps.newHashMap();
    private BindButton mouseWheelUpBind;
    private BindButton mouseWheelDownBind;

    private IWorldProvider worldProvider;
    private LocalPlayer localPlayer;
    private BlockEntityRegistry blockRegistry;
    private EntityRef target = EntityRef.NULL;
    private Vector3i targetBlockPos = null;

    public void initialise() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        blockRegistry = CoreRegistry.get(BlockEntityRegistry.class);

        REPLACE_THIS_WITH_CONFIG();
    }

    public BindButton registerBindButton(String bindId) {
        BindButton bind = new BindButton(bindId, new BindButtonEvent());
        buttonBinds.put(bindId, bind);
        return bind;
    }

    public BindButton registerBindButton(String bindId, BindButtonEvent event) {
        BindButton bind = new BindButton(bindId, event);
        buttonBinds.put(bindId, bind);
        return bind;
    }

    public BindButton getBindButton(String bindId) {
        return buttonBinds.get(bindId);
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
        BindButton bindInfo = buttonBinds.get(bindId);
        keyBinds.put(key, bindInfo);
    }

    public void linkBindButtonToMouse(int mouseButton, String bindId) {
        BindButton bindInfo = buttonBinds.get(bindId);
        mouseButtonBinds.put(mouseButton, bindInfo);
    }

    public void linkBindButtonToMouseWheel(int direction, String bindId) {
        if (direction > 0) {
            mouseWheelUpBind = buttonBinds.get(bindId);
        } else if (direction < 0) {
            mouseWheelDownBind = buttonBinds.get(bindId);
        }
    }

    public BindAxis registerBindAxis(String id, BindButton positiveButton, BindButton negativeButton) {
        return registerBindAxis(id, new BindAxisEvent(), positiveButton, negativeButton);
    }

    public BindAxis registerBindAxis(String id, BindAxisEvent event, BindButton positiveButton, BindButton negativeButton) {
        BindAxis axis = new BindAxis(id, event, positiveButton, negativeButton);
        axisBinds.add(axis);
        axisLookup.put(id, axis);
        return axis;
    }

    public void update(float delta) {
        updateTarget();
        processMouseInput(delta);
        processKeyboardInput(delta);
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

                BindButton bind = mouseButtonBinds.get(button);
                if (bind != null) {
                    bind.updateBindState(buttonDown, delta, target, localPlayer.getEntity(), consumed, GUIManager.getInstance().isConsumingInput());
                }
            } else if (Mouse.getEventDWheel() != 0) {
                int wheelMoved = Mouse.getEventDWheel();
                boolean consumed = sendMouseWheelEvent(wheelMoved, delta);

                BindButton bind = (wheelMoved > 0) ? mouseWheelUpBind : mouseWheelDownBind;
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
            BindButton bind = keyBinds.get(key);
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
        for (BindAxis axis : axisBinds) {
            axis.update(localPlayer.getEntity(), delta, target);
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

        /*ReusableEvent windowRemoveSE = ReusableEvent.makeEvent("core", "windowRemove");
          BindTarget windowRemoveBT = makeEventTarget("core", "windowRemove", windowRemoveSE, null, null);
          bind(Keyboard.KEY_ESCAPE, windowRemoveBT);

          ReusableEvent inventoryToggleSE = ReusableEvent.makeEvent("core", "inventoryToggle");
          BindTarget inventoryToggleBT = makeEventTarget("core", "inventoryToggle", inventoryToggleSE, null, null);
          bind(Keyboard.KEY_I, inventoryToggleBT);

          ReusableEvent debugToggleSE = ReusableEvent.makeEvent("core", "debugToggle");
          BindTarget debugToggleBT = makeEventTarget("core", "debugToggle", debugToggleSE, null, null);
          bind(Keyboard.KEY_F3, debugToggleBT);*/

    }

}


	


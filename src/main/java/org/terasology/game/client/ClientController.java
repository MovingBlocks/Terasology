package org.terasology.game.client;

import com.google.common.base.Objects;
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

import javax.vecmath.Vector2f;
import java.util.Map;
import java.util.logging.Logger;


public class ClientController implements EventHandlerSystem {
    private Logger logger = Logger.getLogger(getClass().getName());

    private double mouseSensititivy = Config.getInstance().getMouseSens();
    private Vector2f lookInput = new Vector2f();

    //private final Map<Integer, BindTarget> keybinds = new HashMap<Integer, BindTarget>();
    private Map<String, BindInfo> buttonBinds = Maps.newHashMap();

    private Map<Integer, BindInfo> keyBinds = Maps.newHashMap();
    private Map<Integer, BindInfo> mouseButtonBinds = Maps.newHashMap();
    private BindInfo mouseWheelUpBind;
    private BindInfo mouseWheelDownBind;

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

    public void registerBindButton(String bindId) {
        BindInfo info = new BindInfo(bindId, new BindButtonEvent());
        buttonBinds.put(bindId, info);
    }

    public void registerBindButton(String bindId, BindButtonEvent event) {
        BindInfo info = new BindInfo(bindId, event);
        buttonBinds.put(bindId, info);
    }

    public void linkBindButtonToKey(int key, String bindId) {
        BindInfo bindInfo = buttonBinds.get(bindId);
        keyBinds.put(key, bindInfo);
    }

    public void linkBindButtonToMouse(int mouseButton, String bindId) {
        BindInfo bindInfo = buttonBinds.get(bindId);
        mouseButtonBinds.put(mouseButton, bindInfo);
    }

    private void linkBindButtonToMouseWheel(int direction, String bindId) {
        if (direction > 0) {
            mouseWheelUpBind = buttonBinds.get(bindId);
        } else if (direction < 0) {
            mouseWheelDownBind = buttonBinds.get(bindId);
        }
    }

    public void update(float delta) {
        updateTarget();

        // TODO: GUI should actually determine whether it has consumed the input (or it should be blocked)
        UIDisplayWindow focusWindow = GUIManager.getInstance().getFocusedWindow();
        boolean consumedByUI = focusWindow != null && focusWindow.isVisible() && focusWindow.isModal();

        //updateCameraTarget();
        processMouseInput(delta);
        processKeyboardInput(delta);
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
        // TODO: Check for non-block targets one we have support for that

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

                BindInfo bind = mouseButtonBinds.get(button);
                if (bind != null) {
                    bind.invoke(buttonDown, delta, consumed, GUIManager.getInstance().isConsumingInput());
                }
            } else if (Mouse.getEventDWheel() != 0) {
                int wheelMoved = Mouse.getEventDWheel();
                boolean consumed = sendMouseWheelEvent(wheelMoved, delta);

                BindInfo bind = (wheelMoved > 0) ? mouseWheelUpBind : mouseWheelDownBind;
                if (bind != null) {
                    bind.invoke(true, delta, consumed, GUIManager.getInstance().isConsumingInput());
                    bind.invoke(false, delta, consumed, GUIManager.getInstance().isConsumingInput());
                }
            }
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
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            ButtonState state = getButtonState(Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
            boolean consumed = sendKeyEvent(key, state, delta);

            // Update bind
            BindInfo bind = keyBinds.get(key);
            if (bind != null && !Keyboard.isRepeatEvent()) {
                bind.invoke(Keyboard.getEventKeyState(), delta, consumed, GUIManager.getInstance().isConsumingInput());
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

        logger.info("Sending event: " + event.getState() + ", " + event.getKeyName());
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

    // returns the *previously* bound target, or null if none was previously bound.
    /*public BindTarget bind(int key, BindTarget target) {
        return keybinds.put(key, target);
    }

    public BindTarget unbind(int key) {
        return keybinds.remove(key);
    } */

    /*private BindTarget makeEventTarget(final String category,
                                       final String description, final ReusableEvent downEvent,
                                       final ReusableEvent upEvent, final ReusableEvent repeatEvent) {
        return new BindTarget(category, description) {
            public void start() {
                if (downEvent != null) {
                    downEvent.reset();
                    localPlayer.getEntity().send(downEvent);
                }
            }

            public void end() {
                if (upEvent != null) {
                    upEvent.reset();
                    localPlayer.getEntity().send(upEvent);
                }
            }

            public void repeat() {
                if (repeatEvent != null) {
                    repeatEvent.reset();
                    localPlayer.getEntity().send(repeatEvent);
                }
            }
        };
    } */


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

    private class BindInfo {

        private BindButtonEvent buttonEvent;
        private int downInputs = 0;
        private String id;

        public BindInfo(String id, BindButtonEvent event) {
            this.id = id;
            this.buttonEvent = event;
        }

        public boolean invoke(boolean pressed, float delta, boolean keyConsumed, boolean guiOnly) {
            if (pressed) {
                if (downInputs++ == 0) {
                    if (guiOnly) {
                        GUIManager.getInstance().processBindButton(id, pressed);
                        keyConsumed = true;
                    }
                    if (!keyConsumed) {
                        buttonEvent.prepare(id, ButtonState.DOWN, delta, target);
                        localPlayer.getEntity().send(buttonEvent);
                        keyConsumed = buttonEvent.isConsumed();
                    }
                }
            } else if (downInputs != 0) {
                if (--downInputs == 0) {
                    if (guiOnly) {
                        GUIManager.getInstance().processBindButton(id, pressed);
                        keyConsumed = true;
                    }
                    if (!keyConsumed) {
                        buttonEvent.prepare(id, ButtonState.UP, delta, target);
                        localPlayer.getEntity().send(buttonEvent);
                        keyConsumed = buttonEvent.isConsumed();
                    }
                }
            }
            return keyConsumed;
        }
    }
}


	


package org.terasology.game.client;

import com.google.common.base.Objects;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.componentSystem.block.BlockEntityRegistry;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.input.*;
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
import java.util.HashMap;
import java.util.Map;


public class ClientController implements ComponentSystem {
    private double mouseSensititivy = Config.getInstance().getMouseSens();
    private Vector2f lookInput = new Vector2f();

    private final Map<Integer, BindTarget> keybinds = new HashMap<Integer, BindTarget>();
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


    public void update(float delta) {
        updateTarget();

        // TODO: GUI should actually determine whether it has consumed the input (or it should be blocked)
        UIDisplayWindow focusWindow = GUIManager.getInstance().getFocusedWindow();
        boolean consumedByUI = focusWindow != null && focusWindow.isVisible() && focusWindow.isModal();

        //updateCameraTarget();
        processMouseInput(delta, consumedByUI);
        processKeyboardInput(delta, consumedByUI);
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

    private void processMouseInput(float delta, boolean consumedByUI) {
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            int wheelMoved = Mouse.getEventDWheel();
            boolean buttonDown = Mouse.getEventButtonState();

            GUIManager.getInstance().processMouseInput(button, buttonDown, wheelMoved);
            if (!consumedByUI) {
                if (wheelMoved != 0) {
                    sendMouseWheelEvent(wheelMoved, delta);
                }

                if (!sendMouseEvent(button, buttonDown, delta)) {
                    // TODO: Bind map and event here?
                }
            }


        }
    }

    private void processKeyboardInput(float delta, boolean consumedByUI) {
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            if (Keyboard.getEventKeyState()) {
                GUIManager.getInstance().processKeyboardInput(key);
            }
            if (!consumedByUI) {
                ButtonState state = getButtonState(Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
                if (!sendKeyEvent(key, state, delta)) {
                    // TODO: Bind and map event here
                    //keybinds.get(e.key).process(e.getState());
                }
            }
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

    private void sendMouseWheelEvent(int wheelTurns, float delta) {
        localPlayer.getEntity().send(new MouseWheelEvent(wheelTurns, delta, target));
    }

    // returns the *previously* bound target, or null if none was previously bound.
    public BindTarget bind(int key, BindTarget target) {
        return keybinds.put(key, target);
    }

    public BindTarget unbind(int key) {
        return keybinds.remove(key);
    }

    private BindTarget makeEventTarget(final String category,
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
    }


    private void REPLACE_THIS_WITH_CONFIG() {

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


	


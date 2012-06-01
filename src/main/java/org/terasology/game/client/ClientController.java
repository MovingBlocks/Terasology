package org.terasology.game.client;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.BlockComponent;
import org.terasology.components.CameraComponent;
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;


public class ClientController implements UpdateSubscriberSystem {
    private double mouseSensititivy = Config.getInstance().getMouseSens();
    private Vector2f lookInput = new Vector2f();

	// pre-allocate the events that will be fired to reduce object allocation
	private final KeyDownEvent KEY_DOWN_EVENT = new KeyDownEvent();
	private final KeyUpEvent KEY_UP_EVENT = new KeyUpEvent();
	private final KeyRepeatEvent KEY_REPEAT_EVENT = new KeyRepeatEvent();
	private final MouseOverEvent MOUSE_OVER_EVENT = new MouseOverEvent();
	private final MouseOutEvent MOUSE_OUT_EVENT = new MouseOutEvent();
	private final LeftMouseDownEvent LEFT_MOUSE_DOWN_EVENT = new LeftMouseDownEvent();
	private final LeftMouseUpEvent LEFT_MOUSE_UP_EVENT = new LeftMouseUpEvent();
	private final RightMouseDownEvent RIGHT_MOUSE_DOWN_EVENT = new RightMouseDownEvent();
	private final RightMouseUpEvent RIGHT_MOUSE_UP_EVENT = new RightMouseUpEvent();
	private final MouseUpEvent MOUSE_UP_EVENT = new MouseUpEvent();
	private final MouseDownEvent MOUSE_DOWN_EVENT = new MouseDownEvent();
	
	private final Map<Integer, BindTarget> keybinds = new HashMap<Integer, BindTarget>();
	private EventSystem eventSystem;
    private EntityRef localPlayerRef;
	
    public void initialise() {
    	eventSystem = CoreRegistry.get(EventSystem.class);
    	localPlayerRef = CoreRegistry.get(LocalPlayer.class).getEntity();
    	
    	
    	REPLACE_THIS_WITH_CONFIG();
    }


    @Override
    public void update(float delta) {
		EntityRef mouseTarget = null;
		
		if (modal) {
			// update the mouse position
			// figure out what ui element the mouse is over
			mouseTarget = null;

		} else { // look mode
//			update camera somehow
			lookInput.set((float)(mouseSensititivy * Mouse.getDX()), (float)(mouseSensititivy * Mouse.getDY()));
//			camera.

			// figure out what block is in the center of the screen
			mouseTarget = null;
		}
		
		processMouseTransition(mouseTarget);
		processMouseButtons(mouseTarget);
		processKeyboardInput(mouseTarget);
    }
    
    private EntityRef lastMouseTarget;
    private void processMouseTransition(EntityRef target) {
		if (target != lastMouseTarget) {
			if (lastMouseTarget != null) {
				MOUSE_OUT_EVENT.reset();
				MOUSE_OUT_EVENT.target = lastMouseTarget;
				eventSystem.send(localPlayerRef, MOUSE_OUT_EVENT); 
			}
			if (target != null) {
				MOUSE_OVER_EVENT.reset();
				MOUSE_OVER_EVENT.target = target;
				eventSystem.send(localPlayerRef, MOUSE_OVER_EVENT); 
			}
			lastMouseTarget = target;
		}
    }
    
	private void processMouseButtons(EntityRef target) {
		MouseEvent e;
		while (Mouse.next()) {
			if (Mouse.getEventButton() == 1) {
				e = Mouse.getEventButtonState() ? LEFT_MOUSE_DOWN_EVENT
						: LEFT_MOUSE_UP_EVENT;
			} else if (Mouse.getEventButton() == 1) {
				e = Mouse.getEventButtonState() ? RIGHT_MOUSE_DOWN_EVENT
						: RIGHT_MOUSE_UP_EVENT;
			} else {
				e = Mouse.getEventButtonState() ? MOUSE_DOWN_EVENT
						: MOUSE_UP_EVENT;
				e.button = Mouse.getEventButton();
			}
			e.reset();
			e.target = target;
			eventSystem.send(localPlayerRef, e); 
		}
	}
	
	private void processKeyboardInput(EntityRef target) {
		while (Keyboard.next()) {
			KeyEvent e; 
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.isRepeatEvent()) {
					e = KEY_REPEAT_EVENT;
				} else {
					e = KEY_DOWN_EVENT;
				}
			} else {
				e = KEY_UP_EVENT;
			}
			e.reset();
			e.key = Keyboard.getEventKey();
			e.target = target;
			
			eventSystem.send(localPlayerRef, e);
			if (!e.isConsumed()) {
				keybinds.get(e.key).process(e.getState());
			}
		}
	}
	
	// returns the *previously* bound target, or null if none was previously bound.
	public BindTarget bind(int key, BindTarget target) {
		return keybinds.put(key, target);
	}

	public BindTarget unbind(int key) {
		return keybinds.remove(key);
	}

	private BindTarget makeEventTarget(final String category,
			final String description, final StaticEvent downEvent,
			final StaticEvent upEvent, final StaticEvent repeatEvent) {
		return new BindTarget(category, description) {
			public void start() {
				if (downEvent != null) {
					downEvent.reset();
					eventSystem.send(localPlayerRef, downEvent);
				}
			}
			public void end() {
				if (upEvent != null) {
					upEvent.reset();
					eventSystem.send(localPlayerRef, upEvent);
				}
			}
			public void repeat() {
				if (repeatEvent != null) {
					repeatEvent.reset();
					eventSystem.send(localPlayerRef, repeatEvent);
				}
			}
		};
	}
	
	
	private void REPLACE_THIS_WITH_CONFIG() {

		StaticEvent windowRemoveSE = StaticEvent.makeEvent("core", "windowRemove");
		BindTarget windowRemoveBT = makeEventTarget("core", "windowRemove", windowRemoveSE, null, null);
		bind(Keyboard.KEY_ESCAPE, windowRemoveBT);

		StaticEvent inventoryToggleSE = StaticEvent.makeEvent("core", "inventoryToggle");
		BindTarget inventoryToggleBT = makeEventTarget("core", "inventoryToggle", inventoryToggleSE, null, null);
		bind(Keyboard.KEY_I, inventoryToggleBT);

		StaticEvent debugToggleSE = StaticEvent.makeEvent("core", "debugToggle");
		BindTarget debugToggleBT = makeEventTarget("core", "debugToggle", debugToggleSE, null, null);
		bind(Keyboard.KEY_F3, debugToggleBT);

	}
}


	


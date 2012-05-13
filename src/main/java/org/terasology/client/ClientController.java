package org.terasology.client;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.components.BlockComponent;
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;


public class ClientController {
    private double mouseSensititivy = Config.getInstance().getMouseSens();
    private Vector2f lookInput = new Vector2f();

	// pre-allocate the events that will be fired to reduce object allocation
	private final KeyDownEvent KEY_DOWN_EVENT = new KeyDownEvent();
	private final KeyUpEvent KEY_UP_EVENT = new KeyUpEvent();
	private final KeyRepeatEvent KEY_REPEAT_EVENT = new KeyRepeatEvent();
	private final MouseOverEvent MOUSE_OVER_EVENT = new MouseOverEvent();
	private final MouseOutEvent MOUSE_OUT_EVENT = new MouseOutEvent();
	
	private final Map<Integer, BindTarget> keybinds = new HashMap<Integer, BindTarget>();
	private EventSystem eventSystem;
    private EntityRef localPlayerRef;
	
    public void initialise() {
    	eventSystem = CoreRegistry.get(EventSystem.class);
    	localPlayerRef = CoreRegistry.get(LocalPlayer.class).getEntity();
    	
//    	eventSystem.registerEventReceiver(new KeyBindEventReceiver(), KeyEvent.class);
    	
    	REPLACE_THIS_WITH_CONFIG();
    }
		
    private EntityRef lastMouseTarget;
    
	public void processMouseInput() {

		// get from the ui manager if we are in mouse cursor or look mode
		boolean cursorMode = false;
		EntityRef mouseTarget = null;
		
		if (cursorMode) {
			// update the mouse position
			// figure out what ui element the mouse is over
			mouseTarget = null;

		} else { // look mode
//			update camera
			lookInput.set((float)(mouseSensititivy * Mouse.getDX()), (float)(mouseSensititivy * Mouse.getDY()));

			// figure out what block is in the center of the screen
			mouseTarget = null;
		}
		
		// if the current mouseover reference isn't the same as the last mouseover reference, throw events
		if (mouseTarget != lastMouseTarget) {
			if (lastMouseTarget != null) {
				MOUSE_OUT_EVENT.reset();
				MOUSE_OUT_EVENT.target = lastMouseTarget;
				eventSystem.send(localPlayerRef, MOUSE_OUT_EVENT); 
			}
			if (mouseTarget != null) {
				MOUSE_OVER_EVENT.reset();
				MOUSE_OVER_EVENT.target = mouseTarget;
				eventSystem.send(localPlayerRef, MOUSE_OVER_EVENT); 
			}
			lastMouseTarget = mouseTarget;
		}
		
		// iterate through the mouse events to determine if any buttons were pressed or released.
		// generate mouse up and down events.
		
	}
	
	
	
	public void processKeyboardInput() {
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
			
			eventSystem.send(localPlayerRef, e); 
		}
	}
	
	// returns the *previously* bound target, or null if none was previously bound.
	public BindTarget bind(int key, BindTarget target) {
		return keybinds.put(key, target);
	}

	public BindTarget unbind(int key) {
		return keybinds.remove(key);
	}

	private BindTarget makeEventTarget(final String category, final String description, final StaticEvent downEvent, final StaticEvent upEvent, final StaticEvent repeatEvent) {
		return new BindTarget(category, description) {
			public void start() {
				if (downEvent != null) {
					downEvent.reset();
					eventSystem.send(downEvent.getEntity(), downEvent);
				}
			}
			public void end() {
				if (upEvent != null) {
					upEvent.reset();
					eventSystem.send(upEvent.getEntity(), upEvent);
				}
			}
			public void repeat() {
				if (repeatEvent != null) {
					repeatEvent.reset();
					eventSystem.send(repeatEvent.getEntity(), repeatEvent);
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
	
    @ReceiveEvent(components={BlockComponent.class})
    public void onDestroyed(NoHealthEvent event, EntityRef entity) {
    	
    }
    
    


}


		
//        }
//        if (key == Keyboard.KEY_F && !screenHasFocus) {
//            toggleViewingDistance();
//        }
//
//        if (key == Keyboard.KEY_F12) {
//            _worldRenderer.printScreen();
//        }

	

//	class KeyBindEventReceiver implements EventReceiver<KeyEvent> {
//		public void onEvent(KeyEvent event, EntityRef entity) {
//			keybinds.get(event.key).fire();
//		}
//	}
		
	
//	
//		if (Keyboard.getEventKeyState()) {
//	                if (!Keyboard.isRepeatEvent()) {
//	                    if (key == Keyboard.KEY_ESCAPE) {
//	                        if (GUIManager.getInstance().getFocusedWindow() != null) {
//	                            GUIManager.getInstance().removeWindow(GUIManager.getInstance().getFocusedWindow());
//	                        } else {
//	                            togglePauseMenu();
//	                        }
//	                    }
//	                    if (key == Keyboard.KEY_F && !screenHasFocus) {
//	                        toggleViewingDistance();
//	                    }
//
//	                    if (key == Keyboard.KEY_F12) {
//	                        _worldRenderer.printScreen();
//	                    }
//	                }
//
//	                // Pass input to focused GUI element
//	                if (GUIManager.getInstance().getFocusedWindow() != null) {
//	                    GUIManager.getInstance().processKeyboardInput(key);
//	                } else {
//	                    for (UIDisplayElement screen : _guiScreens) {
//	                        if (screenCanFocus(screen)) {
//	                            screen.processKeyboardInput(key);
//	                        }
//	                    }
//	                }
//
//	            }
//
//	            // Features for debug mode only
//	            if (debugEnabled && !screenHasFocus && Keyboard.getEventKeyState()) {
//	                if (key == Keyboard.KEY_UP) {
//	                    getActiveWorldProvider().setTime(getActiveWorldProvider().getTime() + 0.005);
//	                }
//
//	                if (key == Keyboard.KEY_DOWN) {
//	                    getActiveWorldProvider().setTime(getActiveWorldProvider().getTime() - 0.005);
//	                }
//
//	                if (key == Keyboard.KEY_RIGHT) {
//	                    getActiveWorldProvider().setTime(getActiveWorldProvider().getTime() + 0.02);
//	                }
//
//	                if (key == Keyboard.KEY_LEFT) {
//	                    getActiveWorldProvider().setTime(getActiveWorldProvider().getTime() - 0.02);
//	                }
//
//	                if (key == Keyboard.KEY_R && !Keyboard.isRepeatEvent()) {
//	                    getWorldRenderer().setWireframe(!getWorldRenderer().isWireframe());
//	                }
//
//	                if (key == Keyboard.KEY_P && !Keyboard.isRepeatEvent()) {
//	                    getWorldRenderer().setCameraMode(WorldRenderer.CAMERA_MODE.PLAYER);
//	            }
//
//	                if (key == Keyboard.KEY_O && !Keyboard.isRepeatEvent()) {
//	                    getWorldRenderer().setCameraMode(WorldRenderer.CAMERA_MODE.SPAWN);
//	                }
//	            }
//
//	            // Pass input to the current player
//	            if (!screenHasFocus)
//	                _localPlayerSys.processKeyboardInput(key, Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
//	        }
//	    }
//	
//    public void processKeyboardInput(int key, boolean state, boolean repeatEvent) {
//        if (inventorySlotBindMap.containsKey(key)) {
//            LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
//            localPlayerComp.selectedTool = inventorySlotBindMap.get(key);
//            localPlayer.getEntity().saveComponent(localPlayerComp);
//            return;
//        }
//        switch (key) {
//            case Keyboard.KEY_K:
//                if (!repeatEvent && state) {
//                    localPlayer.getEntity().send(new DamageEvent(9999, null));
//                }
//                break;
//            case Keyboard.KEY_E:
//                if (!repeatEvent && state) {
//                    processFrob();
//                }
//                break;
//            case Keyboard.KEY_SPACE:
//                if (!repeatEvent && state) {
//                    jump = true;
//
//                    // TODO: handle time better
//                    if (timer.getTimeInMs() - lastTimeSpacePressed < 200) {
//                        toggleGodMode = true;
//                    }
//
//                    lastTimeSpacePressed = timer.getTimeInMs();
//                }
//                break;
//        }
//    }
//	
	
	


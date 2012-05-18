package org.terasology.game.client;

public class KeyEvent extends ClientEvent {
	enum KeyState {
		UP, DOWN, REPEAT;
	}
	protected KeyState state;
	public KeyState getState() {
		return state;
	}

	protected int key;
	
	public int getKey() {
		return key;
	}

}

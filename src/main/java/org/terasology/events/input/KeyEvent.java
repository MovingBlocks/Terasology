package org.terasology.events.input;

import org.lwjgl.input.Keyboard;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.input.InputEvent;
import org.terasology.game.client.ButtonState;

public class KeyEvent extends InputEvent {

    private int key;
    private ButtonState state;

    public KeyEvent(int key, ButtonState state, float delta, EntityRef target) {
        super(delta, target);
    }

	public ButtonState getState() {
		return state;
	}

	public int getKey() {
		return key;
	}

    public String getKeyName() {
        return Keyboard.getKeyName(key);
    }

    public char getKeyCharacter() {
        return Keyboard.getEventCharacter();
    }

    protected void setKey(int key) {
        this.key = key;
    }

}

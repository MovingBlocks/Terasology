package org.terasology.events.input;


import org.lwjgl.input.Mouse;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.client.ButtonState;

public abstract class MouseButtonEvent extends InputEvent {

	private ButtonState state;
    private int button = 0;

    public MouseButtonEvent(int button, ButtonState state, float delta, EntityRef target) {
        super(delta, target);
        this.state = state;
        this.button = button;
    }

	public ButtonState getState() {
		return state;
	}

	public int getButton() {
		return button;
	}

    public String getButtonName() {
        return Mouse.getButtonName(button);
    }

    protected void setButton(int button) {
        this.button = button;
    }

}

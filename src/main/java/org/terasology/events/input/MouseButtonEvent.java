package org.terasology.events.input;


import org.lwjgl.input.Mouse;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.client.ButtonState;

public class MouseButtonEvent extends ButtonEvent {

    private int button = 0;

    public MouseButtonEvent(int button, ButtonState state, float delta, EntityRef target) {
        super(delta, target);
        this.state = state;
        this.button = button;
    }

	public int getButton() {
		return button;
	}

    public String getMouseButtonName() {
        return Mouse.getButtonName(button);
    }

    public String getButtonName() {
        return "mouse:" + getMouseButtonName();
    }

    protected void setButton(int button) {
        this.button = button;
    }

}

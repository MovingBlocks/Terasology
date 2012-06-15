package org.terasology.events.input;


import org.lwjgl.input.Mouse;
import org.terasology.entitySystem.EntityRef;
import org.terasology.input.ButtonState;

public class MouseButtonEvent extends ButtonEvent {

    private int button = 0;
    private ButtonState state;

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

package org.terasology.game.client;


public abstract class MouseEvent extends ClientEvent {
	enum MouseState {
		UP, DOWN, OVER, OUT;
	}
	protected MouseState mouseState; 
	public MouseState getState() {
		return mouseState;
	}

	protected int button = 0;
	public int getButton() {
		return button;
	}

}

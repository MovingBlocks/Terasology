package org.terasology.rendering.gui.framework.events;

import org.terasology.rendering.gui.framework.UIDisplayElement;

public interface ClickListener {

	/**
	 * Click event. Will be called if the specific item was clicked.
	 * @param element The element of the event.
	 * @param button The button. Left = 0, Right = 1, Middle = 2.
	 */
    public void click(UIDisplayElement element, int button);
    
}

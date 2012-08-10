package org.terasology.rendering.gui.framework.events;

import org.terasology.rendering.gui.framework.UIDisplayElement;

public interface FocusListener {

	public void focusOn(UIDisplayElement element);
	
	public void focusOff(UIDisplayElement element);
	
}

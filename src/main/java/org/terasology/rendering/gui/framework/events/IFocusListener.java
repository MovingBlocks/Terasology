package org.terasology.rendering.gui.framework.events;

import org.terasology.rendering.gui.framework.UIDisplayElement;

public interface IFocusListener {

	public void focusOn(UIDisplayElement element);
	
	public void focusOff(UIDisplayElement element);
	
}

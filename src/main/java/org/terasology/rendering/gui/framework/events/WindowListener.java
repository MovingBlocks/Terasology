package org.terasology.rendering.gui.framework.events;

import org.terasology.rendering.gui.framework.UIDisplayElement;

public interface WindowListener {
	public void open(UIDisplayElement element);
	public void close(UIDisplayElement element);
}

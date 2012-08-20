package org.terasology.rendering.gui.framework.style;

import javax.vecmath.Vector2f;

import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIStyleBackgroundImage extends UIGraphicsElement implements UIStyle {
	
	private boolean maximized = true;
	
	public UIStyleBackgroundImage(Texture texture) {
		super(texture);
		//setCroped(false);
	}
	
	@Override
	public void layout() {
		if (getParent() != null && maximized) {
			setSize(getParent().getSize());
		}
		super.layout();
	}
	
	public void setTarget(Vector2f origin, Vector2f size) {
		maximized = false;
		
		setPosition(origin);
		setSize(size);
	}
	
}

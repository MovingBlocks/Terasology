package org.terasology.rendering.gui.framework.style;

import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.terasology.rendering.gui.framework.UIDisplayElement;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIStyleBorderSolid extends UIDisplayElement implements UIStyle {
	
    //Textured borders
    private float width;
    private Vector4f color;
    
    public UIStyleBorderSolid(float width, int r, int g, int b, float a) {
    	this.width = width;
    	this.color = new Vector4f(RGBtoColor(r), RGBtoColor(g), RGBtoColor(b), a);
    	setCroped(false);
	}
    
    private float RGBtoColor(int v) {
    	return (float)v / 255.0f;
    }

	@Override
	public void render() {
		renderSolid();
	}

	@Override
	public void update() {
		
	}

	@Override
	public void layout() {
		if (getParent() != null) {
			setSize(getParent().getSize());
		}
	}
	
    public void renderSolid() {
        glPushMatrix();
        glLoadIdentity();
        glTranslatef(calcAbsolutePosition().x, calcAbsolutePosition().y, 0);

        glLineWidth(width);
        glBegin(GL11.GL_LINES);
        glColor4f(color.x, color.y, color.z, color.w);
        glVertex2f(getPosition().x, getPosition().y);
        glVertex2f(getPosition().x + getSize().x, getPosition().y);
        glVertex2f(getPosition().x + getSize().x, getPosition().y);
        glVertex2f(getPosition().x + getSize().x, getPosition().y + getSize().y);
        glVertex2f(getPosition().x + getSize().x, getPosition().y + getSize().y);
        glVertex2f(getPosition().x, getPosition().y + getSize().y);
        glVertex2f(getPosition().x, getPosition().y + getSize().y);
        glVertex2f(getPosition().x, getPosition().y);
        glEnd();

        glPopMatrix();
    }

	public Vector4f getColor() {
		return color;
	}

	public void setColor(int r, int g, int b, float a) {
		this.color = new Vector4f(RGBtoColor(r), RGBtoColor(g), RGBtoColor(b), a);
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}
}

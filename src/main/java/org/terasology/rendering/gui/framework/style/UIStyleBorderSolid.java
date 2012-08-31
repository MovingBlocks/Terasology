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
        setCrop(false);
    }
    
    public UIStyleBorderSolid(float width, String color, float a) {
        this.width = width;
        setColor(color, a);
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
    
    public void renderSolid() {
        glPushMatrix();
        glLoadIdentity();
        glTranslatef(getAbsolutePosition().x, getAbsolutePosition().y, 0);

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
    
    public void setColor(String color, float a) {
        color = color.trim().toLowerCase();
        
        int r = 0;
        int g = 0;
        int b = 0;
        
        if (color.matches("^#[a-f0-9]{1,6}$")) {
            color = color.replace("#", "");
            
            int sum = Integer.parseInt(color, 16);

            r = (sum & 0x00FF0000) >> 16;
            g = (sum & 0x0000FF00) >> 8;
            b = sum & 0x000000FF;
        }
        
        this.color = new Vector4f(RGBtoColor(r), RGBtoColor(g), RGBtoColor(b), a);
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }
}

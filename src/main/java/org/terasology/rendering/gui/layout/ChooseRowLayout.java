package org.terasology.rendering.gui.layout;


import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glVertex2f;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

public class ChooseRowLayout implements Layout{
    private Vector2f size;
    private float borderWidth;
    private Color borderColor;
    private Vector2f position;
    
    public ChooseRowLayout(Vector2f position, Vector2f size, Color borderColor, float borderWidth){
        this.size = size;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.position = position;
    }

    @Override
    public void layout(UIDisplayContainer container, boolean fitSize) {

    }

    @Override
    public void render() {
        glLineWidth(borderWidth);
        glBegin(GL11.GL_LINE_LOOP);
        glColor4f(borderColor.r, borderColor.g, borderColor.b, borderColor.a);

        glVertex2f(position.x, position.y);
        glVertex2f(position.x + size.x, position.y);
        glVertex2f(position.x + size.x, position.y + size.y);

        glVertex2f(position.x,  position.y + size.y);

        glEnd();

    }

    public void setPosition(Vector2f position){
        this.position = position;
    }

    public Vector2f getPosition(){
        return position;
    }
}

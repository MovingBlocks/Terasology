package org.terasology.rendering.gui.components;


import org.terasology.rendering.gui.framework.UIDisplayElement;
import static org.lwjgl.opengl.GL11.*;
/**
 * A simple graphical text cursor
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UITextCursor extends UIDisplayElement {

    public void render(){
        glPushMatrix();
        glColor4f(0.0f,0.0f,0.0f,1.0f);
        glBegin(GL_QUADS);
        glVertex2f( getPosition().x,      getPosition().y);
        glVertex2f( getPosition().x + 2f, getPosition().y);
        glVertex2f( getPosition().x + 2f, getPosition().y + 15f);
        glVertex2f( getPosition().x,   getPosition().y + 15f);
        glEnd();
        glPopMatrix();
    }

    public void update(){

    }
}

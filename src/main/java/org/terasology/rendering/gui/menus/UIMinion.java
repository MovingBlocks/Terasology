package org.terasology.rendering.gui.menus;

import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UITransparentOverlay;
import org.terasology.rendering.gui.framework.*;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 8/05/12
 * Time: 20:25
 * To change this template use File | Settings | File Templates.
 */
public class UIMinion extends UIDisplayRenderer{

    private UIButton buttonMove;
    private final UIGraphicsElement _background;

    public UIMinion(){

        setSize(new Vector2f(60f,60f));
        _background = new UIGraphicsElement("guiMinion");
        _background.setVisible(true);
        _background.getTextureSize().set(new Vector2f(60f / 256f, 60f / 256f));
        _background.getTextureOrigin().set(new Vector2f(30.0f, 20.0f));
        _background.setSize(getSize());
        _background.setPosition(new Vector2f(0f,0f));
        addDisplayElement(_background);

        UITransparentOverlay overlay = new UITransparentOverlay();
        overlay.setSize(new Vector2f(60,60));
        overlay.setVisible(true);

        buttonMove = new UIButton(new Vector2f(100,24));
        buttonMove.getLabel().setText("Move");
        buttonMove.setVisible(true);
        buttonMove.setPosition(new Vector2f(Display.getWidth() - 150,(Display.getHeight()/2) - 96));
        buttonMove.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                setMinionMoveBehaviour();
            }
        });
        addDisplayElement(buttonMove);
        //
        setVisible(true);
    }

    private void setMinionMoveBehaviour(){}

    @Override
    public void update() {
        setPosition(new Vector2f(0,0));
        //setPosition(new Vector2f(Display.getWidth()-150,(Display.getHeight()/2) -96));

        super.update();
    }

    @Override
    public void render(){

        super.render();
        renderOverlay();
    }

    private void test(){
        buttonMove.setFocus(true);
    }

    public void renderOverlay(){
        glPushMatrix();
        glLoadIdentity();
        glColor4f(0, 0, 0, 0.75f);
        glBegin(GL_QUADS);
        glVertex2f((float)Display.getWidth()-150, (float)(Display.getHeight() / 2) -96);
        glVertex2f((float) Display.getWidth()-50, (float)(Display.getHeight() / 2) -96);
        glVertex2f((float) Display.getWidth()-50, (float)(Display.getHeight() / 2) +96);
        glVertex2f((float)Display.getWidth()-150, (float)(Display.getHeight() / 2) +96);
        glEnd();
        glPopMatrix();
    }
}

package org.terasology.rendering.gui.menus;

import org.lwjgl.opengl.Display;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.MinionBarComponent;
import org.terasology.components.MinionComponent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
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
    private final UIGraphicsElement _selectionRectangle;

    private MinionComponent.MinionBehaviour _Ohbehave;
    private int _selectedMinion;

    public UIMinion(){

        setSize(new Vector2f(60f,80f));
        _background = new UIGraphicsElement("guiMinion");
        _background.getTextureSize().set(new Vector2f(60f / 256f, 80f / 256f));
        _background.getTextureOrigin().set(new Vector2f(30.0f / 256f, 20.0f / 256f));
        _background.setSize(getSize());
        addDisplayElement(_background);
        _background.setVisible(true);

        _selectionRectangle = new UIGraphicsElement("guiMinion");
        _selectionRectangle.getTextureSize().set(new Vector2f(60f / 256f, 20f / 256f));
        _selectionRectangle.getTextureOrigin().set(new Vector2f(30f / 256, 0.0f));
        _selectionRectangle.setSize(new Vector2f(60f, 20f));
        _selectionRectangle.setVisible(true);

        UITransparentOverlay overlay = new UITransparentOverlay();
        overlay.setSize(new Vector2f(60,60));
        overlay.setVisible(true);

        buttonMove = new UIButton(new Vector2f(100,24));
        buttonMove.getLabel().setText("Move");
        buttonMove.setVisible(true);
        buttonMove.setFocus(true);
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
        //setPosition(new Vector2f(0,0));
        //setPosition(new Vector2f(Display.getWidth()-150,(Display.getHeight()/2) -96));
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);



        MinionBarComponent inventory = localPlayer.getEntity().getComponent(MinionBarComponent.class);
        if (inventory == null)
            return;
        LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        if (localPlayerComp != null) {
            _selectedMinion = localPlayerComp.selectedMinion;
        }

        //setPosition(new Vector2f(2f, (getSize().y - 8f) * _selectedMinion - 2f));
        _background.setPosition(new Vector2f(Display.getWidth()-(100),(Display.getHeight()/2) - (25 *(6-(_selectedMinion+1))))); //(25 *(6-(_selectedMinion+1)))
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

    public void setParams(MinionComponent.MinionBehaviour behaviour, int selectedminion){
        _Ohbehave = behaviour;
        _selectedMinion = selectedminion;
    }
}

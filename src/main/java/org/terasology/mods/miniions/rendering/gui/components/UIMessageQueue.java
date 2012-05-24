package org.terasology.mods.miniions.rendering.gui.components;

import org.lwjgl.opengl.Display;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.mods.miniions.events.MinionMessageEvent;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 24/05/12
 * Time: 3:34
 * Queue for message icons containing miniion messages
 */
public class UIMessageQueue extends UIDisplayContainer implements EventHandlerSystem{

    private final int iconSize = 16;
    private final int minionbarSize = 101; // 101 = minionbarheight /2 (=91) + 10 pixel border

    public UIMessageQueue(){
        float height = Display.getHeight() /2 - minionbarSize;
        height -= height % iconSize; // 16 = icon height
        setSize(new Vector2f(iconSize, height));
        setPosition(new Vector2f(Display.getWidth() - iconSize - 4,Display.getHeight() / 2 - minionbarSize));
    }

    public void addIconToQueue(MinionMessageEvent.MessageType messagetype, String[] messagecontent){
    }

    @Override
    public void initialise() {
    }
}

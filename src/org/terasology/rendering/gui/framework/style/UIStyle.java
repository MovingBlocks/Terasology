package org.terasology.rendering.gui.framework.style;

import org.lwjgl.opengl.GL11;
import org.terasology.game.Terasology;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector4f;
import javax.vecmath.Vector2f;
import java.util.HashMap;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

public class UIStyle extends UIDisplayContainer {

    //background
    private UIPropertyBackground _background = new UIPropertyBackground();
    //borders
    UIPropertyBorder _border                 = new UIPropertyBorder();

    //Corners
    //private final HashMap<String, UIGraphicsElement> _cornersTexture = new HashMap<String, UIGraphicsElement>();
    public UIStyle(Vector2f size){
        setSize(size);
        _background.setSize(getSize());
        _border.setSize(getSize());
        addDisplayElement(_background);
        addDisplayElement(_border);
    }


    public void parse(String property, String value){
        if(property.indexOf("border")>=0){
            _border.parse(property, value);
        }else if(property.indexOf("background")>=0){
            _background.parse(property, value);
        }
    }

}

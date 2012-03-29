package org.terasology.rendering.gui.framework;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIWindowTitle;

import javax.vecmath.Vector2f;

/**
 * Created by IntelliJ IDEA.
 * User: kireev
 * Date: 29.03.12
 * Time: 13:08
 * To change this template use File | Settings | File Templates.
 */
public class UIDisplayWindow extends UIScrollableDisplayContainer{
    private UIButton _close;
    private Vector2f _prevMousePos = null;
    private UIWindowTitle _title;
    private boolean _dragged = false;

    public UIDisplayWindow(String title, Vector2f size){
        super();
        setSize(size);
        //this.getClass().getName();
        setStyle("border-image-top", "gui_menu 168/512 5/512 260/512 89/512 5");
        setStyle("border-image-right",  "gui_menu 4/512 81/512 428/512 94/512 4");
        setStyle("border-image-bottom", "gui_menu 168/512 4/512 260/512 175/512 4");
        setStyle("border-image-left",   "gui_menu 4/512 81/512 256/512 94/512 4");

        setStyle("border-corner-topleft",     "gui_menu 256/512 89/512");
        setStyle("border-corner-topright",    "gui_menu 428/512 89/512");
        setStyle("border-corner-bottomright", "gui_menu 428/512 175/512");
        setStyle("border-corner-bottomleft",  "gui_menu 256/512 175/512");

        setStyle("background-image","gui_menu 168/512 76/512 260/512 94/512");

        _title = new UIWindowTitle(new Vector2f(getSize().x*0.6f, 19f), title);
        _title.setVisible(true);
        addDisplayElement(_title);

        _close = new UIButton(new Vector2f(19f, 19f));

        _close.setClassStyle("button","background-image: gui_menu 19/512 19/512 73/512 155/512");
        _close.setClassStyle("button-mouseover","background-image: gui_menu 19/512 19/512 92/512 155/512");
        _close.setClassStyle("button-mouseclick","background-image: gui_menu 19/512 19/512 92/512 155/512");

        _close.setPosition(new Vector2f(getSize().x-25f,getPosition().y+10f));
        _close.setVisible(true);
        _close.getLabel().setText("");

        addDisplayElement(_close);
    }

    public void update(){
     //   _title.getPosition().x = getPosition().x + getSize().x/2;
   //     _title.getSize().x = getSize().x*0.6f;
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
        if(intersects(mousePos)){
            if(_mouseDown){
                _dragged = true;
                if(_prevMousePos==null){
                   _prevMousePos =  new Vector2f(mousePos);
                }
            }

        }

        if(_dragged){
            //setPosition(mousePos);
            drag(new Vector2f(_prevMousePos.x - mousePos.x, _prevMousePos.y-mousePos.y));
            _prevMousePos =  new Vector2f(mousePos);
        }

        if(!_mouseDown||!_dragged || _mouseUp){
            _dragged     = false;
            _mouseDown    = false;
            _prevMousePos = null;
            _mouseUp      = false;
        }

        super.update();
    }

    private void drag(Vector2f value){
        System.out.println(value);
        getPosition().x -=value.x;
        getPosition().y -=value.y;
    }
}

package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import javax.vecmath.Vector2f;


public class UIDialogBox extends UIDisplayWindow{
    private UIWindowTitle _title;
    private UIButton _close;
    private Vector2f _prevMousePos = null;
    private boolean _dragged = false;

    public UIDialogBox(String title, Vector2f size){
        super();
        setSize(size);

        _title = new UIWindowTitle(new Vector2f(getSize().x*0.55f, 19f), title);
        _title.setVisible(true);
        _title.getPosition().x = (getPosition().x + size.x/2f) - _title.getSize().x/2;
        _title.setTitle(title);


        _close = new UIButton(new Vector2f(19f, 19f));

        _close.getPosition().x = getSize().x-25f;
        _close.setVisible(true);
        _close.getLabel().setText("");

        _close.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                close(true);
            }
        });

        windowStyleSetup();

        addDisplayElement(_close);
        addDisplayElement(_title);
    }

    public void update(){
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
        if(intersects(mousePos)){
            if(_mouseDown){
                _focused = true;
                if(_title.intersects(mousePos)){
                    _dragged = true;
                    if(_prevMousePos==null){
                        _prevMousePos =  new Vector2f(mousePos);
                    }
                }
            }
        }

        if(_dragged){
            drag(new Vector2f(_prevMousePos.x - mousePos.x, _prevMousePos.y-mousePos.y));
            _prevMousePos =  new Vector2f(mousePos);
        }

        if(!_mouseDown||!_dragged || _mouseUp){
            _dragged     = false;
            _mouseDown    = false;
            _prevMousePos = null;
            _mouseUp      = false;
            _focused = false;
        }

        super.update();
    }

    public void resize(){
        _title.setSize(new Vector2f(getSize().x*0.55f, 19f));
        _title.getPosition().x = getSize().x/2f - _title.getSize().x/2;
        _title.resize();
        _style = null;
        _close.getPosition().x = getSize().x-25f;
        windowStyleSetup();
    }

    public void windowStyleSetup(){
        setStyle("border-image-top",    "engine:gui_menu 168/512 5/512 260/512 89/512 5");
        setStyle("border-image-right",  "engine:gui_menu 4/512 81/512 428/512 94/512 4");
        setStyle("border-image-bottom", "engine:gui_menu 168/512 4/512 260/512 175/512 4");
        setStyle("border-image-left",   "engine:gui_menu 4/512 81/512 256/512 94/512 4");

        setStyle("border-corner-topleft",     "engine:gui_menu 256/512 89/512");
        setStyle("border-corner-topright",    "engine:gui_menu 428/512 89/512");
        setStyle("border-corner-bottomright", "engine:gui_menu 428/512 175/512");
        setStyle("border-corner-bottomleft",  "engine:gui_menu 256/512 175/512");

        setStyle("background-image","engine:gui_menu 168/512 76/512 260/512 94/512");

        _close.setClassStyle("button",            "background-image: engine:gui_menu 19/512 19/512 73/512 155/512");
        _close.setClassStyle("button-mouseover",  "background-image: engine:gui_menu 19/512 19/512 54/512 155/512");
        _close.setClassStyle("button-mouseclick", "background-image: engine:gui_menu 19/512 19/512 92/512 155/512");
    }

}

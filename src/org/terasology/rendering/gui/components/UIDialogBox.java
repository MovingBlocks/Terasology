package org.terasology.rendering.gui.components;

import org.terasology.rendering.gui.framework.UIScrollableDisplayContainer;

import javax.tools.DiagnosticListener;
import javax.vecmath.Vector2f;


public class UIDialogBox extends UIScrollableDisplayContainer{

    public static enum DialogType {
        MODAL
    }

    private DialogType _type  = DialogType.MODAL;
    private  String    _title = "";

    public UIDialogBox(Vector2f size){
        this(size, "", DialogType.MODAL);
    }

    public UIDialogBox(Vector2f size, String title){
        this(size, title, DialogType.MODAL);
    }

    public UIDialogBox(Vector2f size, String title, DialogType type){
        setSize(size);
        setTitle(title);
        setDialogType(type);
    }
    
    public void setTitle(String title){
        _title = title;
    }

    public void setDialogType(DialogType type){
        _type = type;
    }

}

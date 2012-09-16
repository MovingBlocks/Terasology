/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.gui.widgets;

import java.util.ArrayList;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.DialogListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

/**
 * 
 *
 * TODO clean this up. Remove UIDialogBackground -> should use the style UIStyleBackgroundSplit
 */
public class UIDialogBox extends UIWindow {
    
    //events
    private final ArrayList<DialogListener> dialogListeners = new ArrayList<DialogListener>();
    
    private final UIImage overlay;
    private final UIDialogBackground container;
    private final UIButton closeButton;
    private final Vector2f pressedOffset = new Vector2f(0f, 0f);
    private boolean dragged = false;
    private float titleWidth = 300f;
    protected Object returnValue;
    
    private class UIDialogBackground extends UIDisplayContainer {
        private UIImage leftBackground;
        private UIImage centerBackground;
        private UIImage rightBackground;
        private UILabel text;

        public UIDialogBackground(Vector2f size) {
            setSize(size);
            
            text = new UILabel();
            text.setColor(Color.orange);
            text.setVisible(true);

            leftBackground = new UIImage(AssetManager.loadTexture("engine:gui_menu"));
            leftBackground.setSize(new Vector2f(7f, 19f));
            leftBackground.setTextureSize(new Vector2f(7f, 19f));
            leftBackground.setTextureOrigin(new Vector2f(111f, 155f));
            leftBackground.setVisible(true);

            centerBackground = new UIImage(AssetManager.loadTexture("engine:gui_menu"));
            centerBackground.setSize(new Vector2f(getSize().x - 19f, 19f));
            centerBackground.setTextureSize(new Vector2f(51f, 19f));
            centerBackground.setTextureOrigin(new Vector2f(118f, 155f));
            centerBackground.setPosition(new Vector2f(centerBackground.getPosition().x + leftBackground.getSize().x, 0f));
            centerBackground.setVisible(true);

            rightBackground = new UIImage(AssetManager.loadTexture("engine:gui_menu"));
            rightBackground.setSize(new Vector2f(8f, 19f));
            rightBackground.setTextureSize(new Vector2f(8f, 19f));
            rightBackground.setTextureOrigin(new Vector2f(189f, 155f));
            rightBackground.setVisible(true);
            rightBackground.setPosition(new Vector2f(centerBackground.getPosition().x + centerBackground.getSize().x, 0f));
            addDisplayElement(leftBackground);
            addDisplayElement(centerBackground);
            addDisplayElement(rightBackground);
            addDisplayElement(text);
        }

        public void setTitle(String title) {
            text.setText(title);
            text.setPosition(new Vector2f(getSize().x / 2 - text.getSize().x / 2, 0f));
        }

        public void resize() {
            centerBackground.setSize(new Vector2f(getSize().x - 19f, 19f));
            centerBackground.setPosition(new Vector2f(leftBackground.getPosition().x + leftBackground.getSize().x, 0f));
            rightBackground.setPosition(new Vector2f(centerBackground.getPosition().x + centerBackground.getSize().x, 0f));
            text.setPosition(new Vector2f(getSize().x / 2 - text.getSize().x / 2, 0f));
        }
    }

    public UIDialogBox(Vector2f size) {
        super();
        setSize(size);
        setBackgroundImage("engine:gui_menu", new Vector2f(260f, 92f), new Vector2f(168f, 76f));
        setBorderImage("engine:gui_menu", new Vector2f(256f, 90f), new Vector2f(175f, 88f), new Vector4f(4f, 4f, 4f, 4f));
        setPositionType(EPositionType.ABSOLUTE);
        resetPosition();
        
        overlay = new UIImage(new Color(0, 0, 0, 200));
        overlay.setPositionType(EPositionType.ABSOLUTE);
        overlay.setSize("100%", "100%");
        overlay.setVisible(true);
        
        container = new UIDialogBackground(new Vector2f(titleWidth, 19f));
        container.addMouseButtonListener(new MouseButtonListener() {    
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }
            
            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
                dragged = false;
                pressedOffset.x = 0f;
                pressedOffset.y = 0f;
            }
            
            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                if (intersect) {
                    dragged = true;
                    Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
                    pressedOffset.x = mousePos.x - getPosition().x;
                    pressedOffset.y = mousePos.y - getPosition().y;
                }
            }
        });
        container.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void move(UIDisplayElement element) {
                if (dragged) {
                    drag(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()));
                }
            }

            @Override
            public void leave(UIDisplayElement element) {
                
            }
            
            @Override
            public void hover(UIDisplayElement element) {
                
            }
            
            @Override
            public void enter(UIDisplayElement element) {
                
            }
        });
        container.setHorizontalAlign(EHorizontalAlign.CENTER);
        container.setSize(new Vector2f(titleWidth, 19f));
        container.setVisible(true);
        
        closeButton = new UIButton(new Vector2f(19f, 19f), UIButton.eButtonType.NORMAL);
        closeButton.setPosition(new Vector2f(getSize().x - closeButton.getSize().x - 2, 2));
        closeButton.getLabel().setText("");
        closeButton.setTexture("engine:gui_menu");
        closeButton.setNormalState(new Vector2f(73f, 155f), new Vector2f(19f, 19f));
        closeButton.setHoverState(new Vector2f(54f, 155f), new Vector2f(19f, 19f));
        closeButton.setPressedState(new Vector2f(92f, 155f), new Vector2f(19f, 19f));
        closeButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                close();
            }
        });
        closeButton.setHorizontalAlign(EHorizontalAlign.RIGHT);
        closeButton.setPosition(new Vector2f(-2, 0f));
        closeButton.setVisible(true);

        addDisplayElementToPosition(0, overlay);
        addDisplayElement(closeButton);
        addDisplayElement(container);
    }
    
    @Override
    public void close() {
        super.close();
        
        notifyDialogListeners();
    }
    
    @Override
    public void layout() {
        super.layout();

        if (container != null)
            container.resize();
    }
    
    private void drag(Vector2f pos) {
        pos.x -= pressedOffset.x;
        pos.y -= pressedOffset.y;
        setPosition(pos);
    }
    
    public void resetPosition() {
        setPosition(new Vector2f((Display.getWidth() / 2) - (getSize().x / 2), (Display.getHeight() / 2) - (getSize().y / 2)));
    }
    
    public void setTitle(String title) {
        container.setTitle(title);
    }
    
    protected void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
    
    private void notifyDialogListeners() {
        for (DialogListener listener : dialogListeners) {
            listener.close(this, returnValue);
        }
    }
    
    public void addDialogListener(DialogListener listener) {
        dialogListeners.add(listener);
    }

    public void removeDialogListener(DialogListener listener) {
        dialogListeners.remove(listener);
    }

}

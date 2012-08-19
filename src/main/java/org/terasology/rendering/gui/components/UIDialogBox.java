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
package org.terasology.rendering.gui.components;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;


public class UIDialogBox extends UIDisplayWindow {
    private UIDialogBackground container;
    private UIButton closeButton;
    private Vector2f prevMousePos = null;
    private boolean dragged = false;
    
    private class UIDialogBackground extends UIDisplayContainer {
        private UIGraphicsElement leftBackground;
        private UIGraphicsElement centerBackground;
        private UIGraphicsElement rightBackground;
        private UIText text;

        public UIDialogBackground(Vector2f size, String title) {
            setSize(size);

            text = new UIText(getPosition());
            text.setColor(Color.orange);
            text.setVisible(true);
            
            setTitle(title);

            leftBackground = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));
            leftBackground.setSize(new Vector2f(7f, 19f));
            leftBackground.setTextureSize(new Vector2f(7f, 19f));
            leftBackground.setTextureOrigin(new Vector2f(111f, 155f));
            leftBackground.setVisible(true);

            centerBackground = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));
            centerBackground.setSize(new Vector2f(getSize().x - 19f, 19f));
            centerBackground.setTextureSize(new Vector2f(51f, 19f));
            centerBackground.setTextureOrigin(new Vector2f(118f, 155f));
            centerBackground.getPosition().x += leftBackground.getSize().x;
            centerBackground.setVisible(true);

            rightBackground = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));
            rightBackground.setSize(new Vector2f(8f, 19f));
            rightBackground.setTextureSize(new Vector2f(8f, 19f));
            rightBackground.setTextureOrigin(new Vector2f(189f, 155f));
            rightBackground.setVisible(true);
            rightBackground.getPosition().x = centerBackground.getPosition().x + centerBackground.getSize().x;
            addDisplayElement(leftBackground);
            addDisplayElement(centerBackground);
            addDisplayElement(rightBackground);
            addDisplayElement(text);
        }


        public void setTitle(String title) {
            text.setText(title);
            text.getPosition().x = getSize().x / 2 - text.getTextWidth() / 2;
        }

        public void resize() {
            centerBackground.setSize(new Vector2f(getSize().x - 19f, 19f));
            centerBackground.getPosition().x = leftBackground.getPosition().x + leftBackground.getSize().x;
            rightBackground.getPosition().x = centerBackground.getPosition().x + centerBackground.getSize().x;
            text.getPosition().x = getSize().x / 2 - text.getTextWidth() / 2;
        }
    }

    public UIDialogBox(String title, Vector2f size) {
        super();
        setSize(size);
    	setBorderSolid(2f, 150, 150, 150, 1f);
        setBackgroundImage("engine:gui_menu");
        setBackgroundImageSource(new Vector2f(260f, 94f), new Vector2f(168f, 76f));

        container = new UIDialogBackground(new Vector2f(getSize().x * 0.55f, 19f), title);
        container.setVisible(true);
        container.getPosition().set((getPosition().x + size.x / 2f) - container.getSize().x / 2, 5f);
        container.setTitle(title);
        container.addMouseButtonListener(new MouseButtonListener() {	
			@Override
			public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

			}
			
			@Override
			public void up(UIDisplayElement element, int button, boolean intersect) {
				dragged = false;
				prevMousePos = null;
			}
			
			@Override
			public void down(UIDisplayElement element, int button, boolean intersect) {
				if (intersect) {
					dragged = true;
                    if (prevMousePos == null) {
                        prevMousePos = new Vector2f(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()));
                    }
				}
			}
		});
        container.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void move(UIDisplayElement element) {
		        if (dragged) {
			        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
		            drag(new Vector2f(prevMousePos.x - mousePos.x, prevMousePos.y - mousePos.y));
		            prevMousePos = new Vector2f(mousePos);
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

        closeButton = new UIButton(new Vector2f(19f, 19f), UIButton.eButtonType.NORMAL);
        closeButton.getPosition().set(getSize().x - closeButton.getSize().x - 2, 2);
        closeButton.setVisible(true);
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

        addDisplayElement(closeButton);
        addDisplayElement(container);
    }

    public void resize() {
        container.setSize(new Vector2f(getSize().x * 0.55f, 19f));
        container.getPosition().set((getPosition().x + getSize().x / 2f) - container.getSize().x / 2, 5f);
        container.resize();
        closeButton.getPosition().set(getSize().x - closeButton.getSize().x - 2, 2);
    }
}

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

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

import javax.vecmath.Vector2f;


public class UIDialogBox extends UIDisplayWindow {
    private UIWindowTitle _title;
    private UIButton _close;
    private Vector2f _prevMousePos = null;
    private boolean _dragged = false;

    public UIDialogBox(String title, Vector2f size) {
        super();
        setSize(size);

        _title = new UIWindowTitle(new Vector2f(getSize().x * 0.55f, 19f), title);
        _title.setVisible(true);
        _title.getPosition().x = (getPosition().x + size.x / 2f) - _title.getSize().x / 2;
        _title.setTitle(title);
        _title.addMouseButtonListener(new MouseButtonListener() {	
			@Override
			public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

			}
			
			@Override
			public void up(UIDisplayElement element, int button, boolean intersect) {
				_dragged = false;
				_prevMousePos = null;
			}
			
			@Override
			public void down(UIDisplayElement element, int button, boolean intersect) {
				if (intersect) {
					_dragged = true;
                    if (_prevMousePos == null) {
                        _prevMousePos = new Vector2f(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()));
                    }
				}
			}
		});
        _title.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void move(UIDisplayElement element) {
		        if (_dragged) {
			        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
		            drag(new Vector2f(_prevMousePos.x - mousePos.x, _prevMousePos.y - mousePos.y));
		            _prevMousePos = new Vector2f(mousePos);
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

        _close = new UIButton(new Vector2f(19f, 19f), UIButton.eButtonType.NORMAL);
        _close.getPosition().x = getSize().x - 25f;
        _close.setVisible(true);
        _close.getLabel().setText("");

        _close.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				close(true);
			}
		});

        windowStyleSetup();

        addDisplayElement(_close);
        addDisplayElement(_title);
    }

    public void resize() {
        _title.setSize(new Vector2f(getSize().x * 0.55f, 19f));
        _title.getPosition().x = getSize().x / 2f - _title.getSize().x / 2;
        _title.resize();
        _style = null;
        _close.getPosition().x = getSize().x - 25f;
        windowStyleSetup();
    }

    public void windowStyleSetup() {
        setStyle("border-image-top", "engine:gui_menu 168/512 5/512 260/512 89/512 5");
        setStyle("border-image-right", "engine:gui_menu 4/512 81/512 428/512 94/512 4");
        setStyle("border-image-bottom", "engine:gui_menu 168/512 4/512 260/512 175/512 4");
        setStyle("border-image-left", "engine:gui_menu 4/512 81/512 256/512 94/512 4");

        setStyle("border-corner-topleft", "engine:gui_menu 256/512 89/512");
        setStyle("border-corner-topright", "engine:gui_menu 428/512 89/512");
        setStyle("border-corner-bottomright", "engine:gui_menu 428/512 175/512");
        setStyle("border-corner-bottomleft", "engine:gui_menu 256/512 175/512");

        setStyle("background-image", "engine:gui_menu 168/512 76/512 260/512 94/512");

        _close.setClassStyle("button", "background-image: engine:gui_menu 19/512 19/512 73/512 155/512");
        _close.setClassStyle("button-mouseover", "background-image: engine:gui_menu 19/512 19/512 54/512 155/512");
        _close.setClassStyle("button-mouseclick", "background-image: engine:gui_menu 19/512 19/512 92/512 155/512");
    }

}

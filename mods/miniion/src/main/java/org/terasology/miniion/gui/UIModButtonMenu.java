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
package org.terasology.miniion.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2f;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.widgets.UILabel;

/**
 * A simple graphical button usable for creating user interface.
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * 
 *         TODO program from scratch -> integrate state button here -> implement
 *         radio button?
 */
public class UIModButtonMenu extends UIDisplayContainer {

	private final UILabel _label;

	public enum ButtonType {
		NORMAL, TOGGLE
	};	

	private boolean _toggleState = false;
	private ButtonType _buttonType;

	private final List<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
	private final Map<String, Vector2f[]> states = new HashMap<String, Vector2f[]>();

	/**
	 * Create a simple button, where 2 types are possible. The normal button and
	 * the toggle button.
	 * 
	 * @param size
	 *            The size of the button.
	 * @param buttonType
	 *            The type of the button which can be normal or toggle.
	 */
	public UIModButtonMenu(Vector2f size, ButtonType buttonType) {
		setSize(size);

		_buttonType = buttonType;

		// default button
		setTexture("miniion:modularbutton");
		setNormalState(new Vector2f(0.0f, 0.0f), new Vector2f(100f, 20f));
		setHoverState(new Vector2f(0.0f, 20f), new Vector2f(100f, 20f));
		setPressedState(new Vector2f(0.0f, 80f), new Vector2f(100f, 20f));

		// default state
		setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);

		addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void leave(UIDisplayElement element) {
				if (_buttonType == ButtonType.TOGGLE) {
					if (_toggleState) {
						setBackgroundImage(states.get("pressed")[0],
								states.get("pressed")[1]);
					} else {
						setBackgroundImage(states.get("normal")[0],
								states.get("normal")[1]);
					}
				} else {
					setBackgroundImage(states.get("normal")[0],
							states.get("normal")[1]);
				}
			}

			@Override
			public void hover(UIDisplayElement element) {

			}

			@Override
			public void enter(UIDisplayElement element) {
				CoreRegistry.get(AudioManager.class).playSound(
                        Assets.getSound("engine:click"), 1.0f);
				if (_buttonType == ButtonType.NORMAL) {
					setBackgroundImage(states.get("hover")[0],
							states.get("hover")[1]);
				}
			}

			@Override
			public void move(UIDisplayElement element) {

			}
		});

		addMouseButtonListener(new MouseButtonListener() {
			@Override
			public void up(UIDisplayElement element, int button, boolean intersect) {
				if (_buttonType == ButtonType.NORMAL) {
					if(intersect){
						setBackgroundImage(states.get("hover")[0],	states.get("hover")[1]);
					}else{
						setBackgroundImage(states.get("normal")[0],	states.get("normal")[1]);
					}
				}
			}

			@Override
			public void down(UIDisplayElement element, int button,
					boolean intersect) {
				if (intersect) {
					if (_buttonType == ButtonType.TOGGLE) {
						setToggleState(!_toggleState);
					} else {
						setBackgroundImage(states.get("pressed")[0],
								states.get("pressed")[1]);
					}
				}
			}

			@Override
			public void wheel(UIDisplayElement element, int wheel,
					boolean intersect) {

			}
		});

		_label = new UILabel("Untitled");
		_label.addChangedListener(new ChangedListener() {
			@Override
			public void changed(UIDisplayElement element) {
				layout();
			}
		});
		_label.setHorizontalAlign(EHorizontalAlign.CENTER);
		_label.setVerticalAlign(EVerticalAlign.CENTER);
		_label.setVisible(true);
		_label.setTextShadow(true);

		addDisplayElement(_label);
	}
	
	public void hideLabel(){
		_label.setVisible(false);
	}

	public UILabel getLabel() {
		return _label;
	}

	public void setLabel(String label) {
		_label.setText(label);
	}

	public void setColorOffset(int offset) {
		setNormalState(new Vector2f(0.0f, offset), new Vector2f(256f, 30f));
	}

	/**
	 * Set the texture of the button. Use setNormalTexture, setHoverTexture and
	 * setPressedTexture to configure the texture origin and size of the
	 * different states.
	 * 
	 * @param texture
	 *            The texture to load by the AssetManager.
	 */
	public void setTexture(String texture) {
		setBackgroundImage(texture);
	}

	/**
	 * Set the normal states texture origin and size. Set the texture by using
	 * setTexture.
	 * 
	 * @param origin
	 *            The origin.
	 * @param size
	 *            The size.
	 */
	public void setNormalState(Vector2f origin, Vector2f size) {
		states.remove("normal");
		states.put("normal", new Vector2f[] { origin, size });

		// set default state
		setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
	}

	/**
	 * Set the hover states texture origin and size. Set the texture by using
	 * setTexture. In toggle mode this texture will be ignored.
	 * 
	 * @param origin
	 *            The origin.
	 * @param size
	 *            The size.
	 */
	public void setHoverState(Vector2f origin, Vector2f size) {
		states.remove("hover");
		states.put("hover", new Vector2f[] { origin, size });

		// set default state
		setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
	}

	/**
	 * Set the pressed states texture origin and size. Set the texture by using
	 * setTexture.
	 * 
	 * @param origin
	 *            The origin.
	 * @param size
	 *            The size.
	 */
	public void setPressedState(Vector2f origin, Vector2f size) {
		states.remove("pressed");
		states.put("pressed", new Vector2f[] { origin, size });

		// set default state
		setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
	}

	public boolean getToggleState() {
		return _toggleState;
	}

	/**
	 * Set the state of the toggle button. Only has an affect if the button was
	 * created as an toggle button.
	 * 
	 * @param state
	 *            True to set the pressed state.
	 */
	public void setToggleState(boolean state) {
		if (_toggleState != state) {
			_toggleState = state;

			if (_toggleState) {
				setBackgroundImage(states.get("pressed")[0],
						states.get("pressed")[1]);
			} else {
				setBackgroundImage(states.get("normal")[0],
						states.get("normal")[1]);
			}

			notifyChangedListeners();
		}
	}

	private void notifyChangedListeners() {
		for (ChangedListener listener : changedListeners) {
			listener.changed(this);
		}
	}

	public void addChangedListener(ChangedListener listener) {
		changedListeners.add(listener);
	}

	public void removeChangedListener(ChangedListener listener) {
		changedListeners.remove(listener);
	}
}

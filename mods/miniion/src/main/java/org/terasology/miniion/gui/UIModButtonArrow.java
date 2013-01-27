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
import org.terasology.logic.manager.AudioManager;
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
public class UIModButtonArrow extends UIDisplayContainer {

	public enum ButtonType {
		LEFT, RIGHT
	};
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
	public UIModButtonArrow(Vector2f size, ButtonType buttontype) {
		setSize(size);

		// default button
		switch(buttontype){
			case LEFT :{
				setTexture("miniion:ArrowLeft");
				break;
			}
			case RIGHT :{
				setTexture("miniion:ArrowRight");
				break;
			}
			default : {
				setTexture("miniion:ArrowLeft");
				break;
			} 
		}		
		setNormalState(new Vector2f(0.0f, 0.0f), new Vector2f(12f, 23f));
		setHoverState(new Vector2f(12f, 0.0f), new Vector2f(12f, 23f));
		setPressedState(new Vector2f(24f, 0.0f), new Vector2f(12f, 23f));

		// default state
		setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);

		addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void leave(UIDisplayElement element) {
				setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
			}

			@Override
			public void hover(UIDisplayElement element) {

			}

			@Override
			public void enter(UIDisplayElement element) {
				AudioManager.play(new AssetUri(AssetType.SOUND, "engine:click"), 1.0f);
				setBackgroundImage(states.get("hover")[0], states.get("hover")[1]);
			}

			@Override
			public void move(UIDisplayElement element) {

			}
		});

		addMouseButtonListener(new MouseButtonListener() {
			@Override
			public void up(UIDisplayElement element, int button, boolean intersect) {
				setBackgroundImage(states.get("normal")[0],	states.get("normal")[1]);
			}

			@Override
			public void down(UIDisplayElement element, int button,
					boolean intersect) {
				if (intersect) {
					setBackgroundImage(states.get("pressed")[0], states.get("pressed")[1]);
				}
			}

			@Override
			public void wheel(UIDisplayElement element, int wheel,
					boolean intersect) {

			}
		});

	}	

	public void setColorOffset(int offset) {
		setNormalState(new Vector2f(0.0f, offset), new Vector2f(12f, 23f));
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

	public void addChangedListener(ChangedListener listener) {
		changedListeners.add(listener);
	}

	public void removeChangedListener(ChangedListener listener) {
		changedListeners.remove(listener);
	}
}

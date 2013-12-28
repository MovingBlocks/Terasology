/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.logic.manager;

import java.util.List;

import org.terasology.rendering.gui.framework.UIDisplayElement;


/**
 * The HUDElement is implemented by all HUD DisplayElements
 *
 * @author Mike Kienenberger <mkienenb@gmail.com>
 */

public interface HUDElement {

	Object getId();

	List<UIDisplayElement> getDisplayElements();

	void initialise();
	void open();

	void update();

	void willShutdown();
	void shutdown();

}

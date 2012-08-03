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

import org.lwjgl.opengl.Display;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * Transparent fullscreen overlay.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIImageOverlay extends UIGraphicsElement {

    public UIImageOverlay(Texture texture) {
        super(texture);
        layout();
    }

    @Override
    public void layout() {
    	super.layout();
    	
    	setSize(new Vector2f((float) Display.getWidth(), (float) Display.getHeight()));
    }
}

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
import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;

/**
 * A small toolbar placed on the bottom of the screen.
 * TODO maybe get rid of this class. The UIItemContainer alone can do this job.
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIToolbar extends UIDisplayContainer {
	
    private final UIItemContainer tools;
    private int previousSelected = 0;
    
    public UIToolbar() {
        setSize(new Vector2f(364f, 44f));
        
        tools = new UIItemContainer(9, 1);
        tools.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity());
        tools.setVisible(true);

        addDisplayElement(tools);
        
        layout();
    }

	@Override
    public void layout() {
    	super.layout();
    			
        centerHorizontally();
        getPosition().y = Display.getHeight() - getSize().y;
    }
	
	@Override
	public void update() {
		super.update();
		//TODO solve this with events

		//this is a work around to set the entity when its there. constructor will execute before..
		if (tools != null && CoreRegistry.get(LocalPlayer.class).getEntity() != EntityRef.NULL) {
			if (tools.getEntity() == EntityRef.NULL) {
				tools.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity());
			}
		}

		//set the selection rectangle
		if (tools.getCells().size() > 0) {
			LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
			LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
			
			tools.getCells().get(previousSelected).setSelectionRectangleEnable(false);
			tools.getCells().get(localPlayerComp.selectedTool).setSelectionRectangleEnable(true);
			previousSelected = localPlayerComp.selectedTool;
		}
	}
}

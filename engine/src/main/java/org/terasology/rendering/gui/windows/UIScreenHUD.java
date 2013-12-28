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
package org.terasology.rendering.gui.windows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.logic.manager.HUD;
import org.terasology.rendering.logic.manager.HUDElement;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 *         <p/>
 *         TODO clean up -> remove debug stuff, move to debug window together with metrics
 */
public class UIScreenHUD extends UIWindow implements ComponentSystem, HUD {

    private List<HUDElement> hudElementList = new ArrayList<HUDElement>();

    /**
     * Init. the HUD.
     */
    public UIScreenHUD() {
        setId("hud");
        maximize();

        update();
        layout();
    }

	@Override
	public Collection<? extends HUDElement> getHUDElements() {
		return hudElementList;
	}

	@Override
	public HUDElement getHUDElementByHUDElementId(String hudElementId) {
		for (HUDElement hudElement : hudElementList) {
			if (hudElement.getId().equals(hudElementId)) {
				return hudElement;
			}
		}
		return null;
	}

	@Override
	public void addHUDElement(HUDElement hudElement) {
		if (null == hudElement) {
			// TODO: log error?
			return;
		}

		hudElement.initialise();

		hudElementList.add(hudElement);
		
		// TODO: seems like we should be able to do this without a dependency in hudElement to DisplayElement
		for (UIDisplayElement uiDisplayElement : hudElement.getDisplayElements()) {
			addDisplayElement(uiDisplayElement);
		}
		
//        update();
	}

	@Override
	public void removeHUDElement(HUDElement hudElement) {
		if (null == hudElement) {
			// TODO: log error?
			return;
		}

		hudElementList.remove(hudElement);
		
		// TODO: seems like we should be able to do this without a dependency in hudElement to DisplayElement
		for (UIDisplayElement uiDisplayElement : hudElement.getDisplayElements()) {
			removeDisplayElement(uiDisplayElement);
		}
		
        update();
	}

	@Override
	public void open() {
		super.open();
        
        for (HUDElement hudElement : hudElementList) {
        	hudElement.open();
		}
	}
	
	@Override
    public void update() {
        super.update();
        
        for (HUDElement hudElement : hudElementList) {
        	hudElement.update();
		}
    }

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {

    }

	public void toggleMapGrid() {
		// TODO Auto-generated method stub
		
	}

	public void toggleMapGridAxis() {
		// TODO Auto-generated method stub
		
	}

}

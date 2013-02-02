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

package org.terasology.miniion.componentsystem.action;

import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.miniion.components.actions.OpenUiActionComponent;
import org.terasology.miniion.gui.UICardBook;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class OpenUiAction implements EventHandlerSystem {

	@Override
	public void initialise() {
	}

	@Override
	public void shutdown() {
	}

	@ReceiveEvent(components = { ItemComponent.class,
			OpenUiActionComponent.class })
	public void onActivate(ActivateEvent event, EntityRef entity) {
		OpenUiActionComponent uiInfo = entity
				.getComponent(OpenUiActionComponent.class);
		if (uiInfo != null) {
			if (uiInfo.uiwindowid.matches("cardbook")) {
				UICardBook cardbookui = (UICardBook) CoreRegistry.get(
						GUIManager.class).openWindow("cardbook");
				cardbookui.openContainer(entity,
						CoreRegistry.get(LocalPlayer.class).getEntity());
			} else {
				CoreRegistry.get(GUIManager.class)
						.openWindow(uiInfo.uiwindowid);
			}
		}
	}
}

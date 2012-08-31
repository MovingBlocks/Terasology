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
package org.terasology.componentSystem.action;

import org.terasology.components.BookComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.gui.windows.UIScreenBook;


/**
 * Reading the Book calls the UI + Contents.
 *
 * @author bi0hax
 */
@RegisterComponentSystem
public class ReadBookAction implements EventHandlerSystem {

    private UIWindow bookScreen;

    public void initialise() {
        bookScreen = GUIManager.getInstance().addWindow(new UIScreenBook(), "engine:bookScreen");
    }

    @Override
    public void shutdown() {
    }

    public EntityRef entity;


    @ReceiveEvent(components = {BookComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        GUIManager.getInstance().setFocusedWindow(bookScreen);

    }
}

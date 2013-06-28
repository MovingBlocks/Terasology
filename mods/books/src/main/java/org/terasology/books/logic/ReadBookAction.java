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
package org.terasology.books.logic;

import org.terasology.books.rendering.gui.windows.UIScreenBook;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.GUIManager;


/**
 * Reading the Book calls the UI + Contents.
 *
 * @author bi0hax
 */
@RegisterComponentSystem
public class ReadBookAction implements EventHandlerSystem {

    public void initialise() {
        CoreRegistry.get(GUIManager.class).registerWindow("book", UIScreenBook.class);
    }

    @Override
    public void shutdown() {
    }

    public EntityRef entity;


    @ReceiveEvent(components = {BookComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        CoreRegistry.get(GUIManager.class).openWindow("book");
    }
}

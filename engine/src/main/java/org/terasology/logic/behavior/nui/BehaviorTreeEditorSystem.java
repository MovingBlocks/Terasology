/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.nui;

import org.lwjgl.input.Mouse;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.registry.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.ButtonState;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.input.events.MouseXAxisEvent;
import org.terasology.input.events.MouseYAxisEvent;
import org.terasology.network.ClientComponent;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.NUIManagerInternal;

/**
 * Opens the bt editor screen. Catches all user input events and consumes them, when editor is opened.
 * <p/>
 * TODO this will become obsolete, once nui can handle ingame popup screens
 *
 * @author synopia
 */
@RegisterSystem
public class BehaviorTreeEditorSystem implements ComponentSystem, RenderSystem, UpdateSubscriberSystem {
    @In
    private NUIManager nuiManager;

    private boolean editorVisible;

    @Override
    public void initialise() {
        nuiManager.closeScreens();
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleConsole(BTEditorButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            if (!editorVisible) {
                nuiManager.pushScreen("engine:behaviorEditorScreen");
                event.consume();
                editorVisible = true;
                Mouse.setGrabbed(false);
            } else {
                nuiManager.popScreen();
                event.consume();
                editorVisible = false;
                Mouse.setGrabbed(true);
            }
        }
    }

    @Override
    public void update(float delta) {
        nuiManager.update(delta);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderAlphaBlend() {

    }

    @Override
    public void renderOverlay() {
        nuiManager.render();
    }

    @Override
    public void renderFirstPerson() {

    }

    @Override
    public void renderShadows() {

    }

    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void mouseButtonEvent(MouseButtonEvent event, EntityRef entity) {
        if (editorVisible) {
            ((NUIManagerInternal) nuiManager).mouseButtonEvent(event, entity);
            event.consume();
        }
    }

    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void mouseWheelEvent(MouseWheelEvent event, EntityRef entity) {
        if (editorVisible) {
            ((NUIManagerInternal) nuiManager).mouseWheelEvent(event, entity);
            event.consume();
        }
    }

    //mouse movement events
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onMouseX(MouseXAxisEvent event, EntityRef entity) {
        if (editorVisible) {
            event.consume();
        }
    }

    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onMouseY(MouseYAxisEvent event, EntityRef entity) {
        if (editorVisible) {
            event.consume();
        }
    }

    //bind input events (will be send after raw input events, if a bind button was pressed and the raw input event hasn't consumed the event)
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void bindEvent(BindButtonEvent event, EntityRef entity) {
        if (editorVisible) {
            if (event instanceof BTEditorButton) {
                BTEditorButton editorButton = (BTEditorButton) event;
                onToggleConsole(editorButton, entity);
            }
            event.consume();
        }
    }

    //raw input events
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void keyEvent(KeyEvent event, EntityRef entity) {
        if (editorVisible) {
            ((NUIManagerInternal) nuiManager).keyEvent(event, entity);
        }
    }

}

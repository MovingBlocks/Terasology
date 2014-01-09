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
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.input.events.MouseXAxisEvent;
import org.terasology.input.events.MouseYAxisEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.network.ClientComponent;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.nui.NUIManager;

/**
 * @author synopia
 */
@RegisterSystem
public class BehaviorTreeEditorSystem implements ComponentSystem, RenderSystem, UpdateSubscriberSystem {
    @In
    private NUIManager nuiManager;

    @Override
    public void initialise() {
        nuiManager.closeScreens();
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleConsole(BTEditorButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.pushScreen("engine:behaviorEditorScreen");
            event.consume();
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
}

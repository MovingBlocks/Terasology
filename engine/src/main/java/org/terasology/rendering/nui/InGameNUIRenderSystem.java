/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui;

import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.registry.In;

/**
 * @author synopia
 */
@RegisterSystem
public class InGameNUIRenderSystem implements RenderSystem, UpdateSubscriberSystem {
    @In
    private NUIManager nuiManager;

    @In
    private EventSystem eventSystem;

    @Override
    public void initialise() {
        nuiManager.closeAllScreens();
        eventSystem.registerEventHandler(nuiManager);
    }

    @Override
    public void renderOverlay() {
        nuiManager.render();
    }

    @Override
    public void update(float delta) {
        nuiManager.update(delta);
    }

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderAlphaBlend() {

    }

    @Override
    public void renderFirstPerson() {

    }

    @Override
    public void renderShadows() {

    }

    @Override
    public void shutdown() {

    }
}

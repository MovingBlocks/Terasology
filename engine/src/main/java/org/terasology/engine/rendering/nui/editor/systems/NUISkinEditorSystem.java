/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.editor.systems;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.editor.binds.NUISkinEditorButton;
import org.terasology.rendering.nui.editor.layers.NUISkinEditorScreen;

/**
 * The back-end system for the NUI skin editor.
 */
@RegisterSystem
@Share(NUISkinEditorSystem.class)
public class NUISkinEditorSystem extends AbstractEditorSystem {
    @In
    private NUIManager nuiManager;

    private boolean editorActive;

    @ReceiveEvent(components = ClientComponent.class,
                  priority = EventPriority.PRIORITY_CRITICAL)
    public void showEditor(NUISkinEditorButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            toggleEditor();
            event.consume();
        }
    }

    @Override
    public void toggleEditor() {
        nuiManager.toggleScreen(NUISkinEditorScreen.ASSET_URI);
        editorActive = !editorActive;
    }

    @Override
    public boolean isEditorActive() {
        return editorActive;
    }
}

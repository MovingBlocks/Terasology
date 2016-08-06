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
package org.terasology.rendering.nui.editor;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;

@RegisterSystem
@Share(NUISkinEditorSystem.class)
public class NUISkinEditorSystem extends BaseComponentSystem {
    private static final String NUI_SKIN_EDITOR_URN = "engine:nuiSkinEditorScreen";

    @In
    private NUIManager nuiManager;

    /**
     * Whether the editor is currently active.
     */
    private boolean editorActive;

    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_CRITICAL)
    public void showEditor(NUISkinEditorButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            toggleEditor();
            event.consume();
        }
    }

    public void toggleEditor() {
        nuiManager.toggleScreen(NUI_SKIN_EDITOR_URN);
        editorActive = !editorActive;
    }

    /**
     * @return Whether the editor is currently active.
     */
    public boolean isEditorActive() {
        return editorActive;
    }
}
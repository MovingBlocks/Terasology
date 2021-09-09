// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.editor.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.editor.binds.NUISkinEditorButton;
import org.terasology.engine.rendering.nui.editor.layers.NUISkinEditorScreen;
import org.terasology.input.ButtonState;

/**
 * The back-end system for the NUI skin editor.
 */
@RegisterSystem
@Share(NUISkinEditorSystem.class)
public class NUISkinEditorSystem extends AbstractEditorSystem {
    @In
    private NUIManager nuiManager;

    private boolean editorActive;

    @Priority(EventPriority.PRIORITY_CRITICAL)
    @ReceiveEvent(components = ClientComponent.class)
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

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.editor.systems;

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;

/**
 * A base system for the NUI screen/skin editor systems.
 */
public abstract class AbstractEditorSystem extends BaseComponentSystem {
    /**
     * Toggles the editor's visibility state.
     */
    public abstract void toggleEditor();

    /**
     * @return Whether the editor is currently active.
     */
    public abstract boolean isEditorActive();
}

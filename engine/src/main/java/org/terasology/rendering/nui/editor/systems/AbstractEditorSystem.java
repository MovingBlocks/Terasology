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

import org.terasology.entitySystem.systems.BaseComponentSystem;

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

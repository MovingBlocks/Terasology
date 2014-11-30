/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;

/**
 * A base screen to help create screens with less plumbing.
 * This will use the InteractionTarget system to get the entity that the character activated.
 * Every time the screen is opened,  it ensures that the correct entity has been initialized in the UI.
 */
public abstract class BaseInteractionScreen extends CoreScreenLayer {
    @In
    private LocalPlayer localPlayer;

    protected EntityRef getInteractionTarget() {
        EntityRef characterEntity = localPlayer.getCharacterEntity();
        CharacterComponent characterComponent = characterEntity.getComponent(CharacterComponent.class);

        return characterComponent.predictedInteractionTarget;
    }

    @Override
    public void onOpened() {
        super.onOpened();
        initializeWithInteractionTarget(getInteractionTarget());
    }

    protected abstract void initializeWithInteractionTarget(EntityRef interactionTarget);
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;

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
        return characterEntity.getComponent(CharacterComponent.class).predictedInteractionTarget;
    }

    @Override
    public void onOpened() {
        super.onOpened();
        initializeWithInteractionTarget(getInteractionTarget());
    }

    protected abstract void initializeWithInteractionTarget(EntityRef interactionTarget);
}

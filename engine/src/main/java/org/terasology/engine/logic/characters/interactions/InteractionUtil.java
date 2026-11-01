// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters.interactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.context.annotation.API;

/**
 * Utility class for entities with the {@link CharacterComponent}.
 */
@API
public final class InteractionUtil {
    private static final Logger logger = LoggerFactory.getLogger(InteractionUtil.class);

    private InteractionUtil() {
        // Utility class: no instance required.
    }


    /**
     * This method can be used by clients to request the cancelation of an interaction.
     * The client will update it's
     */
    public static void cancelInteractionAsClient(EntityRef character) {
        cancelInteractionAsClient(character, true);
    }

    static void cancelInteractionAsClient(EntityRef character, boolean notifyServer) {
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Interaction instigator has no character component");
            return;
        }

        EntityRef oldTarget = characterComponent.predictedInteractionTarget;
        if (oldTarget.exists()) {
            characterComponent.predictedInteractionTarget = EntityRef.NULL;
            character.saveComponent(characterComponent);
            oldTarget.send(new InteractionEndPredicted(character));
            if (notifyServer) {
                character.send(new InteractionEndRequest());
            }
        }
    }


    public static void cancelInteractionAsServer(EntityRef character) {
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Interaction end request instigator has no character component");
            return;
        }
        int oldInteractionId = characterComponent.authorizedInteractionId;
        EntityRef oldTarget = characterComponent.authorizedInteractionTarget;
        if (oldTarget.exists()) {
            characterComponent.authorizedInteractionTarget = EntityRef.NULL;
            character.saveComponent(characterComponent);
        }

        character.send(new InteractionEndEvent(oldInteractionId));
    }

    /**
     * @return the active interaction screen uri of the specified character.
     * The method returns null if the player has no interaction screen open.
     * The method is only intended to be called for the own character.
     */
    public static ResourceUrn getActiveInteractionScreenUri(EntityRef character) {
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            return null;
        }
        EntityRef interactionTarget = characterComponent.predictedInteractionTarget;
        if (!interactionTarget.exists()) {
            return null;
        }
        InteractionScreenComponent screenComponent = interactionTarget.getComponent(InteractionScreenComponent.class);
        if (screenComponent == null) {
            return null;
        }
        return new ResourceUrn(screenComponent.screen);
    }

}

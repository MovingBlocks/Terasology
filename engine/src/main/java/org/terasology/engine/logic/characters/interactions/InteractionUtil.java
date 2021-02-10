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

package org.terasology.logic.characters.interactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.module.sandbox.API;

/**
 * Utility class for entities with the {@link org.terasology.logic.characters.CharacterComponent}.
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

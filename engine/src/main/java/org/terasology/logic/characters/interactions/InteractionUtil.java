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
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;

/**
 * Utility class for entities with the {@link org.terasology.logic.characters.CharacterComponent}.
 */
public class InteractionUtil {
    private static final Logger logger = LoggerFactory.getLogger(InteractionUtil.class);

    private InteractionUtil() {
        // Utility class: no instance required.
    }


    public static void setInteractionTarget(EntityRef instigator, EntityRef target) {
        CharacterComponent characterComponent = instigator.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Interaction instigator has no character component");
            return;
        }
        EntityRef oldTarget = characterComponent.interactionTarget;
        if (oldTarget.exists()) {
            instigator.send(new InteractionEndRequest(oldTarget));
        }
        if (target.exists()) {
            instigator.send(new InteractionStartRequest(target));
        }
    }

    /**
     *
     * @return the active interaction screen uri of the specified character. Due to network delays
     * it can happen that the returned dialog is already closed. The method returns null if the player
     * has no interaction screen open.
     */
    public static AssetUri getActiveInteractionScreenUri(EntityRef character) {
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            return null;
        }
        EntityRef interactionTarget = characterComponent.interactionTarget;
        if (!interactionTarget.exists()) {
            return null;
        }
        InteractionScreenComponent screenComponent = interactionTarget.getComponent(InteractionScreenComponent.class);
        if (screenComponent == null) {
            return null;
        }
        return new AssetUri(AssetType.UI_ELEMENT, screenComponent.screen);
    }

}

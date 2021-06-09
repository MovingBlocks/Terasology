// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.interactions.InteractionEndPredicted;
import org.terasology.engine.logic.characters.interactions.InteractionStartPredicted;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Information common to characters (the physical body of players and creatures)
 *
 */
public final class CharacterComponent implements Component<CharacterComponent> {
    /**
     * Recommended height from center at which name tags should be placed if there is one.
     */
    public float nameTagOffset = 1.0f;
    /**
     * Specifies the maximium range at which this character is able to interact with other objects.
     * When the player leaves the range the interaction gets canceled.
     */
    public float interactionRange = 5f;
    /**
     * The current interaction target of a character which has been authorized by the authority (e.g. the server).
     * <br><br>
     * Modules should not modify this field directly.
     */
    public EntityRef authorizedInteractionTarget = EntityRef.NULL;
    /**
     * This field is only set for clients (including clients that are servers).
     * <br><br>
     * It contains the number of the activationId that caused the interaction start.
     * <br><br>
     * The field is used to tell the client which interaction got canceled. Thus if the client has started another
     * interaction when it receives the old cancel, it won't wrongly cancel the new interaction.
     */
    public int authorizedInteractionId;

    /**
     * This field is only set for clients (including clients that are servers). The clients set it
     * best to their knowledge.
     * <br><br>
     * The events {@link InteractionStartPredicted} and
     * {@link InteractionEndPredicted} inform about changes of this
     * field.
     */
    public EntityRef predictedInteractionTarget = EntityRef.NULL;

    /**
     * This field is only set for clients (including clients that are servers).
     * <br><br>
     * It contains the number of the activationId that caused the interaction start.
     * <br><br>
     * The field is used to determine if a incoming interaction cancel is for the current interaction or not.
     */
    public int predictedInteractionId;

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    @Owns
    public EntityRef movingItem = EntityRef.NULL;

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public EntityRef controller = EntityRef.NULL;

    @Override
    public void copy(CharacterComponent other) {
        this.nameTagOffset = other.nameTagOffset;
        this.interactionRange = other.interactionRange;
        this.authorizedInteractionTarget = other.authorizedInteractionTarget;
        this.authorizedInteractionId = other.authorizedInteractionId;
        this.predictedInteractionTarget = other.predictedInteractionTarget;
        this.predictedInteractionId = other.predictedInteractionId;
        this.movingItem = other.movingItem;
        this.controller = other.controller;
    }
}

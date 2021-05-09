// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.interactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.characters.events.ActivationPredicted;
import org.terasology.engine.logic.characters.events.ActivationRequestDenied;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.ScreenLayerClosedEvent;

/**
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class InteractionSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(InteractionSystem.class);

    @In
    private NUIManager nuiManager;

    @ReceiveEvent(components = {InteractionTargetComponent.class}, netFilter = RegisterMode.AUTHORITY)
    public void onActivate(ActivateEvent event, EntityRef target) {
        EntityRef instigator = event.getInstigator();

        CharacterComponent characterComponent = instigator.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Interaction start request instigator has no character component");
            return;
        }
        if (characterComponent.authorizedInteractionTarget.exists()) {
            logger.error("Interaction wasn't finished at start of next interaction");
            instigator.send(new InteractionEndEvent(characterComponent.authorizedInteractionId));
        }

        characterComponent.authorizedInteractionTarget = target;
        characterComponent.authorizedInteractionId = event.getActivationId();
        instigator.saveComponent(characterComponent);

    }

    @ReceiveEvent(components = {InteractionTargetComponent.class})
    public void onActivationPredicted(ActivationPredicted event, EntityRef target) {
        EntityRef character = event.getInstigator();
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            return;
        }
        if (characterComponent.predictedInteractionTarget.exists()) {
            InteractionUtil.cancelInteractionAsClient(character);
        }
        if (target.exists()) {
            characterComponent.predictedInteractionTarget = target;
            characterComponent.predictedInteractionId = event.getActivationId();
            character.saveComponent(characterComponent);
            target.send(new InteractionStartPredicted(character));
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class}, netFilter = RegisterMode.AUTHORITY)
    public void onActivationRequestDenied(ActivationRequestDenied event, EntityRef character) {
        character.send(new InteractionEndEvent(event.getActivationId()));
    }

    @ReceiveEvent(components = {}, netFilter = RegisterMode.AUTHORITY)
    public void onInteractionEndRequest(InteractionEndRequest request, EntityRef instigator) {
        InteractionUtil.cancelInteractionAsServer(instigator);
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onInteractionEnd(InteractionEndEvent event, EntityRef character, CharacterComponent characterComponent) {
        if (event.getInteractionId() == characterComponent.predictedInteractionId) {
            InteractionUtil.cancelInteractionAsClient(character, false);
        }
    }

    @ReceiveEvent(components = {InteractionTargetComponent.class, InteractionScreenComponent.class})
    public void onInteractionEndPredicted(InteractionEndPredicted event, EntityRef target,
                                          InteractionScreenComponent screenComponent) {
        nuiManager.closeScreen(screenComponent.screen);
    }


    @ReceiveEvent(components = {InteractionScreenComponent.class})
    public void onInteractionStartPredicted(InteractionStartPredicted event, EntityRef container,
                                            InteractionScreenComponent interactionScreenComponent) {
        EntityRef investigator = event.getInstigator();
        CharacterComponent characterComponent = investigator.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Interaction start predicted for entity without character component");
            return;
        }
        ClientComponent controller = characterComponent.controller.getComponent(ClientComponent.class);
        if (controller != null && controller.local) {
            nuiManager.closeAllScreens();
            nuiManager.pushScreen(interactionScreenComponent.screen);
        }
    }

    /**
     * The method listens for the event that the user closes the screen of the current interaction target.
     * <p>
     * When it happens then it cancels the interaction.
     */
    @ReceiveEvent(components = {ClientComponent.class})
    public void onScreenLayerClosed(ScreenLayerClosedEvent event, EntityRef container, ClientComponent clientComponent) {
        EntityRef character = clientComponent.character;
        ResourceUrn activeInteractionScreenUri = InteractionUtil.getActiveInteractionScreenUri(character);

        if ((activeInteractionScreenUri != null) && (activeInteractionScreenUri.equals(event.getClosedScreenUri()))) {
            InteractionUtil.cancelInteractionAsClient(clientComponent.character);
        }
    }
}

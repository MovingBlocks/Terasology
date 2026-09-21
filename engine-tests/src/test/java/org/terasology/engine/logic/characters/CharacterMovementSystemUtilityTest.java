// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.engine.CharacterCollider;
import org.terasology.engine.physics.engine.PhysicsEngine;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Regression test for #4969: an entity mid-death can lose {@link CharacterMovementComponent} (or {@link
 * LocationComponent}) between being queued for a state update and that update actually running - see
 * {@link ServerCharacterPredictionSystem#onDestroy}. {@link CharacterMovementSystemUtility#setToExtrapolateState}
 * used to assume both components were always present and NPE'd the moment they weren't.
 */
public class CharacterMovementSystemUtilityTest {
    private PhysicsEngine physics;
    private CharacterMovementSystemUtility utility;
    private CharacterStateEvent state;

    @BeforeEach
    public void setup() {
        physics = Mockito.mock(PhysicsEngine.class);
        utility = new CharacterMovementSystemUtility(physics);
        state = new CharacterStateEvent(0, 1, new Vector3f(), new Quaternionf(), new Vector3f(),
                0, 0, MovementMode.WALKING, true);
    }

    @Test
    public void setToExtrapolateStateSkipsEntityMissingCharacterMovementComponent() {
        EntityRef entity = Mockito.mock(EntityRef.class);
        Mockito.when(entity.getComponent(LocationComponent.class)).thenReturn(new LocationComponent());
        Mockito.when(entity.getComponent(CharacterMovementComponent.class)).thenReturn(null);

        // Must not throw - this is the exact NPE from #4969's traceback.
        utility.setToExtrapolateState(entity, state, 100);

        verify(entity, never()).saveComponent(Mockito.any());
        verifyNoInteractions(physics);
    }

    @Test
    public void setToExtrapolateStateSkipsEntityMissingLocationComponent() {
        EntityRef entity = Mockito.mock(EntityRef.class);
        Mockito.when(entity.getComponent(LocationComponent.class)).thenReturn(null);
        Mockito.when(entity.getComponent(CharacterMovementComponent.class)).thenReturn(new CharacterMovementComponent());

        utility.setToExtrapolateState(entity, state, 100);

        verify(entity, never()).saveComponent(Mockito.any());
        verifyNoInteractions(physics);
    }

    @Test
    public void setToExtrapolateStateUpdatesEntityWithBothComponents() {
        EntityRef entity = Mockito.mock(EntityRef.class);
        Mockito.when(entity.getComponent(LocationComponent.class)).thenReturn(new LocationComponent());
        Mockito.when(entity.getComponent(CharacterMovementComponent.class)).thenReturn(new CharacterMovementComponent());
        CharacterCollider collider = Mockito.mock(CharacterCollider.class);
        Mockito.when(physics.getCharacterCollider(entity)).thenReturn(collider);

        utility.setToExtrapolateState(entity, state, 100);

        verify(entity).saveComponent(Mockito.isA(LocationComponent.class));
        verify(entity).saveComponent(Mockito.isA(CharacterMovementComponent.class));
        verify(collider).setLocation(Mockito.any());
    }
}

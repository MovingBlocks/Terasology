// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 */
public class KinematicCharacterMoverTest {

    @Test
    public void testUpdateMode() {
        CharacterStateEvent state = new CharacterStateEvent();
        state.setMode(MovementMode.WALKING);

        KinematicCharacterMover.updateMode(state, false, true, true, false);
        assertSame(MovementMode.DIVING, state.getMode());

        KinematicCharacterMover.updateMode(state, true, false, true, false);
        assertSame(MovementMode.SWIMMING, state.getMode());

        state.setMode(MovementMode.FLYING);
        KinematicCharacterMover.updateMode(state, false, true, true, false);
        assertSame(MovementMode.DIVING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, false, false);
        assertSame(MovementMode.WALKING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, true, false);
        assertSame(MovementMode.CLIMBING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, false, false);
        assertSame(MovementMode.WALKING, state.getMode());

        state.setMode(MovementMode.GHOSTING);
        KinematicCharacterMover.updateMode(state, false, true, false, false);
        assertSame(MovementMode.DIVING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, true, false);
        assertSame(MovementMode.CLIMBING, state.getMode());

        state.setMode(MovementMode.WALKING);

        KinematicCharacterMover.updateMode(state, false, true, true, true);
        assertSame(MovementMode.DIVING, state.getMode());

        KinematicCharacterMover.updateMode(state, true, false, true, true);
        assertSame(MovementMode.SWIMMING, state.getMode());

        state.setMode(MovementMode.FLYING);
        KinematicCharacterMover.updateMode(state, false, true, true, true);
        assertSame(MovementMode.DIVING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, false, true);
        assertSame(MovementMode.CROUCHING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, true, true);
        assertSame(MovementMode.CLIMBING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, false, true);
        assertSame(MovementMode.CROUCHING, state.getMode());

        state.setMode(MovementMode.GHOSTING);
        KinematicCharacterMover.updateMode(state, false, true, false, true);
        assertSame(MovementMode.DIVING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, true, true);
        assertSame(MovementMode.CLIMBING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, false, false);
        assertSame(MovementMode.WALKING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, false, true);
        assertSame(MovementMode.CROUCHING, state.getMode());
    }
}

/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.characters;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.logic.behavior.DebugNode;
import org.terasology.logic.behavior.tree.CounterNode;
import org.terasology.logic.behavior.tree.Interpreter;

/**
 */
public class KinematicCharacterMoverTest {

    @Test
    public void testUpdateMode() {
        CharacterStateEvent state = new CharacterStateEvent();
        state.setMode(MovementMode.WALKING);

        KinematicCharacterMover.updateMode(state, false, true, true);
        Assert.assertSame(MovementMode.DIVING, state.getMode());

        KinematicCharacterMover.updateMode(state, true, false, true);
        Assert.assertSame(MovementMode.SWIMMING, state.getMode());

        state.setMode(MovementMode.FLYING);
        KinematicCharacterMover.updateMode(state, false, true, true);
        Assert.assertSame(MovementMode.DIVING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, false);
        Assert.assertSame(MovementMode.WALKING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, true);
        Assert.assertSame(MovementMode.CLIMBING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, false);
        Assert.assertSame(MovementMode.WALKING, state.getMode());

        state.setMode(MovementMode.GHOSTING);
        KinematicCharacterMover.updateMode(state, false, true, false);
        Assert.assertSame(MovementMode.DIVING, state.getMode());

        KinematicCharacterMover.updateMode(state, false, false, true);
        Assert.assertSame(MovementMode.CLIMBING, state.getMode());
    }
}

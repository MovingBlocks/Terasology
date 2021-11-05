// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.terasology.engine.logic.behavior.core.BehaviorState;

import java.util.Arrays;

public class ParallelTest extends CountCallsTest {

    @Disabled("Ignored for the moment")
    @Test
    public void testAllSuccess() {
        assertBT("{ parallel:[success, success, success]}",
                Arrays.asList(BehaviorState.SUCCESS, BehaviorState.SUCCESS, BehaviorState.SUCCESS),
                Arrays.asList(4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3));
    }

    @Disabled("Ignored for the moment")
    @Test
    public void testAllFail() {
        assertBT("{ parallel:[failure, failure, failure]}",
                Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE),
                Arrays.asList(4, 1, 4, 1, 4, 1));
    }

    @Disabled("Ignored for the moment")
    @Test
    public void testAllRunning() {
        assertBT("{ parallel:[running, running, running]}",
                Arrays.asList(BehaviorState.RUNNING, BehaviorState.RUNNING, BehaviorState.RUNNING),
                Arrays.asList(4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3));
    }

    @Disabled("Ignored for the moment")
    @Test
    public void testFailSuccess() {
        assertBT("{ parallel:[failure, success, success]}",
                Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE),
                Arrays.asList(4, 1, 4, 1, 4, 1));
    }

    @Disabled("Ignored for the moment")
    @Test
    public void testSuccessFail() {
        assertBT("{ parallel:[success, failure, failure]}",
                Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE),
                Arrays.asList(4, 1, 2, 4, 1, 2, 4, 1, 2));
    }

    @Disabled("Ignored for the moment")
    @Test
    public void testRunningFail() {
        assertBT("{ parallel:[running, failure, failure]}",
                Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE),
                Arrays.asList(4, 1, 2, 4, 1, 2, 4, 1, 2));
    }

    @Disabled("Ignored for the moment")
    @Test
    public void testRunningSuccess() {
        assertBT("{ parallel:[running, success, success]}",
                Arrays.asList(BehaviorState.RUNNING, BehaviorState.RUNNING, BehaviorState.RUNNING),
                Arrays.asList(4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3));
    }

}

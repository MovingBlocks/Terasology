// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.behavior;

import org.junit.jupiter.api.Test;
import org.terasology.engine.logic.behavior.core.BehaviorState;

import java.util.Arrays;

public class SequenceTest extends CountCallsTest {
    @Test
    public void testAllSuccess() {
        assertBT("{ sequence:[success, success, success]}",
                Arrays.asList(BehaviorState.SUCCESS, BehaviorState.SUCCESS, BehaviorState.SUCCESS), Arrays.asList(4, 1, 2, 3, 4, 4));

    }

    @Test
    public void testAllFail() {
        assertBT("{ sequence:[failure, failure, failure]}",
                Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE), Arrays.asList(4, 1, 4, 1, 4, 1));
    }

    @Test
    public void testAllRunning() {
        assertBT("{ sequence:[running, running, running]}",
                Arrays.asList(BehaviorState.RUNNING, BehaviorState.RUNNING, BehaviorState.RUNNING), Arrays.asList(4, 1, 4, 1, 4, 1));
    }

    @Test
    public void testFailSuccess() {
        assertBT("{ sequence:[failure, success, success]}",
                Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE), Arrays.asList(4, 1, 4, 1, 4, 1));
    }

    @Test
    public void testSuccessFail() {
        assertBT("{ sequence:[success, failure, failure]}",
                Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE), Arrays.asList(4, 1, 2, 4, 2, 4, 2));
    }

    @Test
    public void testRunningFail() {
        assertBT("{ sequence:[running, failure, failure]}",
                Arrays.asList(BehaviorState.RUNNING, BehaviorState.RUNNING, BehaviorState.RUNNING), Arrays.asList(4, 1, 4, 1, 4, 1));
    }

    @Test
    public void testRunningSuccess() {
        assertBT("{ sequence:[running, success, success]}",
                Arrays.asList(BehaviorState.RUNNING, BehaviorState.RUNNING, BehaviorState.RUNNING), Arrays.asList(4, 1, 4, 1, 4, 1));
    }
}

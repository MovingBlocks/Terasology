/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.behavior;

import org.junit.jupiter.api.Test;
import org.terasology.logic.behavior.core.BehaviorState;

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

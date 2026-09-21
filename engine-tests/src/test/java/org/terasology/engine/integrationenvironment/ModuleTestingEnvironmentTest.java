// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import org.junit.jupiter.api.Test;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@IntegrationEnvironment
public class ModuleTestingEnvironmentTest {

    public static final int THE_ANSWER = 42;

    @Test
    public void runUntilWithUnsatisfiedFutureExplainsTimeout(MainLoop mainLoop) {
        SettableFuture<?> unsatisfiedFuture = SettableFuture.create();

        UncheckedTimeoutException exception = assertThrows(UncheckedTimeoutException.class,
                // TODO: change the timeout for this test so it doesn't always take
                //     a minimum of 30 seconds.
                () -> mainLoop.runUntil(unsatisfiedFuture));
        assertThat(exception).hasMessageThat().contains("default timeout");
    }

    @Test
    public void runUntilFutureHonoursItsOwnGameTimeTimeout(MainLoop mainLoop) {
        SettableFuture<?> unsatisfiedFuture = SettableFuture.create();
        long startRealTime = System.currentTimeMillis();

        UncheckedTimeoutException exception = assertThrows(UncheckedTimeoutException.class,
                () -> mainLoop.runUntil(200, unsatisfiedFuture));

        assertThat(exception).hasMessageThat().contains("200 ms");
        assertThat(unsatisfiedFuture.isCancelled()).isTrue();
        // Well short of DEFAULT_GAME_TIME_TIMEOUT, which is what the future-taking form always waited for.
        assertThat(System.currentTimeMillis() - startRealTime).isLessThan(ModuleTestingEnvironment.DEFAULT_GAME_TIME_TIMEOUT / 2);
    }

    @Test
    public void runUntilWithImmediateFutureReturnsValue(MainLoop mainLoop) {
        ListenableFuture<Integer> valueFuture = Futures.immediateFuture(THE_ANSWER);
        assertThat(mainLoop.runUntil(valueFuture)).isEqualTo(THE_ANSWER);
    }

    @Test
    public void awaitUntilReturnsQuietlyWhenConditionHolds(MainLoop mainLoop) {
        mainLoop.awaitUntil("a condition that is already true", () -> true);
    }

    @Test
    public void awaitUntilNamesWhatItWasWaitingFor(MainLoop mainLoop) {
        // A short game-time timeout keeps this well under the safety timeout, so we exercise the
        // game-time path - the one runUntil reports by returning true rather than throwing.
        AssertionError error = assertThrows(AssertionError.class,
                () -> mainLoop.awaitUntil(200, "the thing that never happens", () -> false));

        assertThat(error).hasMessageThat().contains("the thing that never happens");
        assertThat(error).hasMessageThat().contains("game time");
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.recording;

/**
 * An enum with the status of Record and Replay. There are six statuses:
 *
 * NOT_ACTIVATED: Neither Record nor Replay is activated.
 * PREPARING_RECORD: The game is in a state where the classes and variables necessary for a recording to work is being initialized.
 * RECORDING: Recording is activated and the events selected to record are being recorded as the game is played.
 * PREPARING_REPLAY: The game is in a state where the classes and variables necessary for a replay to work is being initialized.
 * REPLAYING: Replay is activated and a recording should be replayed during this state.
 * REPLAY_FINISHED: The Replay finished but the game world in which the Replay was played is still open.
 */
public enum RecordAndReplayStatus {
    NOT_ACTIVATED, PREPARING_RECORD, RECORDING, PREPARING_REPLAY, REPLAYING, REPLAY_FINISHED;
}

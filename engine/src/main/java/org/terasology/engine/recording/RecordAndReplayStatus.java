/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.recording;

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

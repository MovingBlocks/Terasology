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
 * The Status of Record and Replay.
 */
public enum RecordAndReplayStatus {
    NOT_ACTIVATED, PREPARING_RECORD, RECORDING, PREPARING_REPLAY, REPLAYING, REPLAY_FINISHED;

    /** Status of the Record and Replay in the current game */
    private static RecordAndReplayStatus currentStatus = NOT_ACTIVATED;

    public static RecordAndReplayStatus getCurrentStatus() {
        return currentStatus;
    }

    public static void setCurrentStatus(RecordAndReplayStatus status) {
        currentStatus = status;
    }
}

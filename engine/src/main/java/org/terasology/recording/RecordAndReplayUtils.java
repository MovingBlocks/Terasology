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
 * Saves data important for Record and Replay, such as the RecordAndReplayStatus, the title of the current game, if a
 * shutdown was requested, and the amount of files used to serialize RecordedEvents.
 */
public final class RecordAndReplayUtils {
    private static RecordAndReplayStatus recordAndReplayStatus = RecordAndReplayStatus.NOT_ACTIVATED;
    private static String gameTitle;
    private static boolean shutdownRequested;
    private static int fileCount = 1;
    private static int fileAmount;

    private RecordAndReplayUtils() {

    }

    public static RecordAndReplayStatus getRecordAndReplayStatus() {
        return recordAndReplayStatus;
    }

    public static void setRecordAndReplayStatus(RecordAndReplayStatus recordAndReplayStatus) {
        RecordAndReplayUtils.recordAndReplayStatus = recordAndReplayStatus;
    }

    public static String getGameTitle() {
        return gameTitle;
    }

    public static void setGameTitle(String gameTitle) {
        RecordAndReplayUtils.gameTitle = gameTitle;
    }

    public static boolean isShutdownRequested() {
        return shutdownRequested;
    }

    public static void setShutdownRequested(boolean shutdownRequested) {
        RecordAndReplayUtils.shutdownRequested = shutdownRequested;
    }

    static int getFileCount() {
        return fileCount;
    }

    static void setFileCount(int fileCount) {
        RecordAndReplayUtils.fileCount = fileCount;
    }

    static int getFileAmount() {
        return fileAmount;
    }

    static void setFileAmount(int fileAmount) {
        RecordAndReplayUtils.fileAmount = fileAmount;
    }

    /**
     * Resets shutdownRequested, fileCount and fileAmount. Should be called once a Recording ends.
     */
    public static void reset() {
        shutdownRequested = false;
        fileCount = 1;
        fileAmount = 0;
    }


}

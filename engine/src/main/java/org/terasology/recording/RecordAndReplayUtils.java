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
public class RecordAndReplayUtils {
    private String gameTitle;
    private boolean shutdownRequested;
    private int fileCount;
    private int fileAmount;

    public RecordAndReplayUtils() {
        this.shutdownRequested = false;
        this.fileCount = 1;
        this.fileAmount = 0;
        this.gameTitle = "";
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public boolean isShutdownRequested() {
        return shutdownRequested;
    }

    public void setShutdownRequested(boolean shutdownRequested) {
        this.shutdownRequested = shutdownRequested;
    }

    int getFileCount() {
        return fileCount;
    }

    void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    int getFileAmount() {
        return fileAmount;
    }

    void setFileAmount(int fileAmount) {
        this.fileAmount = fileAmount;
    }

    /**
     * Resets shutdownRequested, fileCount and fileAmount. Should be called once a Recording ends.
     */
    public void reset() {
        shutdownRequested = false;
        fileCount = 1;
        fileAmount = 0;
    }


}

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
package org.terasology.recording;

public class RecordAndReplayUtils {
    private static RecordAndReplayStatus recordAndReplayStatus = RecordAndReplayStatus.NOT_ACTIVATED;
    private static int recordCount; //begins as 0
    private static String gameTitle;

    private RecordAndReplayUtils() {

    }

    public static RecordAndReplayStatus getRecordAndReplayStatus() {
        return recordAndReplayStatus;
    }

    public static void setRecordAndReplayStatus(RecordAndReplayStatus recordAndReplayStatus) {
        RecordAndReplayUtils.recordAndReplayStatus = recordAndReplayStatus;
    }

    public static int getRecordCount() {
        return recordCount;
    }

    public static void setRecordCount(int recordCount) {
        RecordAndReplayUtils.recordCount = recordCount;
    }

    public static String getGameTitle() {
        return gameTitle;
    }

    public static void setGameTitle(String gameTitle) {
        RecordAndReplayUtils.gameTitle = gameTitle;
    }
}

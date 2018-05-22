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

public class EntityRefIdMapCell {
    private long originalId;
    private long replayId;

    public EntityRefIdMapCell() {

    }

    public EntityRefIdMapCell(long originalId) {
        this.originalId = originalId;
    }

    public long getOriginalId() {
        return originalId;
    }

    public void setOriginalId(long originalId) {
        this.originalId = originalId;
    }

    public long getReplayId() {
        return replayId;
    }

    public void setReplayId(long replayId) {
        this.replayId = replayId;
    }
}

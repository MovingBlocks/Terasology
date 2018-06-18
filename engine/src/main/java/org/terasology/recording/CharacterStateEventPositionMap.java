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

import org.terasology.math.geom.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class CharacterStateEventPositionMap {

    private Map<Integer, Vector3f> idToPosition;

    public CharacterStateEventPositionMap() {
        this.idToPosition = new HashMap<>();
    }

    public void add(int sequenceNumber, Vector3f position) {
        Vector3f pos = new Vector3f(position);
        idToPosition.put(sequenceNumber, pos);
    }

    public Vector3f get(int sequenceNumber) {
        return idToPosition.get(sequenceNumber);
    }

    Map<Integer, Vector3f> getIdToPosition() {
        return idToPosition;
    }

    void setIdToPosition(Map<Integer, Vector3f> idToPosition) {
        this.idToPosition = idToPosition;
    }

    public void reset() {
        this.idToPosition = new HashMap<>();
    }
}

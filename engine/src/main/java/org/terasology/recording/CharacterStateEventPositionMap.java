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

import org.terasology.logic.characters.CharacterStateEvent;
import org.terasology.math.geom.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class CharacterStateEventPositionMap {

    private Map<Integer, Vector3f[]> idToData;

    public CharacterStateEventPositionMap() {
        this.idToData = new HashMap<>();
    }

    public void add(int sequenceNumber, Vector3f position, Vector3f velocity) {
        Vector3f[] data = new Vector3f[2];
        data[0] = new Vector3f(position);
        data[1] = new Vector3f(velocity);
        idToData.put(sequenceNumber, data);
    }

    public Vector3f[] get(int sequenceNumber) {
        return idToData.get(sequenceNumber);
    }

    Map<Integer, Vector3f[]> getIdToData() {
        return idToData;
    }

    void setIdToData(Map<Integer, Vector3f[]> idToData) {
        this.idToData = idToData;
    }

    public void reset() {
        this.idToData = new HashMap<>();
    }

    public void updateCharacterStateEvent(CharacterStateEvent event) {
        Vector3f[] data = this.idToData.get(event.getSequenceNumber());
        event.setPosition(data[0]);
        event.setVelocity(data[1]);
    }
}

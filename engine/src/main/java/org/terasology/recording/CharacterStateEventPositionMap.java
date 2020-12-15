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

import org.joml.Vector3f;
import org.terasology.logic.characters.CharacterStateEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * When a CharacterMoveInputEvent is caught by ServerCharacterPredictionSystem, a new CharacterStateEvent is created and
 * processed without passing through the EventSystem. Since this event is important to reproduce the player's location,
 * this class is used to record its "position" and "velocity" attribute and update it during a Replay.
 */
public class CharacterStateEventPositionMap {

    /**
     * Map in which the key is the "sequenceNumber" of the CharacterStateEvent and the value is an array with the
     * "position" and "velocity" variables.
     */
    private Map<Integer, Vector3f[]> idToData;

    public CharacterStateEventPositionMap() {
        this.idToData = new HashMap<>();
    }

    /**
     * Add a new "position" and "velocity" to the map.
     *
     * @param sequenceNumber the sequenceNumber of the CharacterStateEvent.
     * @param position the position of the event.
     * @param velocity the velocity of the event.
     */
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

    /**
     * Used in a replay to update a CharacterStateEvent with the correct values of "position" and "velocity".
     *
     * @param event the event to be updated.
     */
    public void updateCharacterStateEvent(CharacterStateEvent event) {
        Vector3f[] data = this.idToData.get(event.getSequenceNumber());
        event.setPosition(data[0]);
        event.setVelocity(data[1]);
    }
}

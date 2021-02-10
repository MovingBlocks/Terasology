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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * During Record and Replay development, it was noticed that sometimes holding the right or left mouse button caused
 * some issues in a recording. The temporary solution to this problem was to record the "direction" and "originPos" variables
 * in the "activateTargetOrOwnedEntity" method of the LocalPlayer and "onAttackRequest" method in the CharacterSystem class.
 * This class is responsible for saving those variables and updating them during a replay.
 */
public class DirectionAndOriginPosRecorder {

    private Deque<Vector3f[]> directionAndOriginData;

    DirectionAndOriginPosRecorder() {
        this.directionAndOriginData = new ArrayDeque<>();
    }

    public void add(Vector3f direction, Vector3f originPos) {
        Vector3f[] data = new Vector3f[2];
        data[0] = new Vector3f(direction);
        data[1] = new Vector3f(originPos);
        this.directionAndOriginData.addLast(data);
    }

    public Vector3f[] poll() {
        return this.directionAndOriginData.pollFirst();
    }

    public void reset() {
        this.directionAndOriginData = new ArrayDeque<>();
    }
}

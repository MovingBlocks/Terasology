// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.recording;


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

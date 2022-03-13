// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.recording;

import java.util.ArrayList;
import java.util.List;

/**
 * Saves DirectionAndOriginPosRecorders used in LocalPlayer and CharacterSystem.
 */
public class DirectionAndOriginPosRecorderList {

    private List<DirectionAndOriginPosRecorder> list;

    public DirectionAndOriginPosRecorderList() {
        list = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            list.add(new DirectionAndOriginPosRecorder());
        }
    }

    /**
     * @return the DirectionAndOriginPosRecorder used to save AttackEvent data in CharacterSystem.
     */
    public DirectionAndOriginPosRecorder getAttackEventDirectionAndOriginPosRecorder() {
        return this.list.get(0);
    }

    /**
     * @return the DirectionAndOriginPosRecorder used to save TargetOrOwnedEntity data in LocalPlayer.
     */
    public DirectionAndOriginPosRecorder getTargetOrOwnedEntityDirectionAndOriginPosRecorder() {
        return this.list.get(1);
    }

    public void reset() {
        list = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            list.add(new DirectionAndOriginPosRecorder());
        }
    }

    public List<DirectionAndOriginPosRecorder> getList() {
        return list;
    }

    public void setList(List<DirectionAndOriginPosRecorder> list) {
        this.list = list;
    }
}

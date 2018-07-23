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

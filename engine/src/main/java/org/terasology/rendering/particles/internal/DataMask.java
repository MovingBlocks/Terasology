/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.particles.internal;

/**
 * Created by Linus on 7-3-2015.
 */
public enum DataMask {

    SIZE              (0b000001),
    ENERGY            (0b000010),
    POSITION          (0b000100),
    PREVIOUS_POSITION (0b001000),
    VELOCITY          (0b010000),
    COLOR             (0b100000),
    ALL               (0b111111);


    //Package private stuff
    DataMask(final int rawMask) {
        this.rawMask = rawMask;
    }

    boolean isEnabled(final int mask) {
        return (this.rawMask & mask) != 0;
    }

    int combine(DataMask dataMask, DataMask ... dataMasks) {
        int combinedMask = dataMask.rawMask;

        for(DataMask dataMaskI: dataMasks) {
            combinedMask |= dataMaskI.rawMask;
        }

        return combinedMask;
    }

    final int rawMask;
}

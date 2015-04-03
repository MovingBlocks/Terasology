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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by Linus on 7-3-2015.
 */
public enum DataMask {

    ENERGY            (0b000001),
    POSITION          (0b000010),
    PREVIOUS_POSITION (0b000100),
    VELOCITY          (0b001000),
    SCALE             (0b010000),
    COLOR             (0b100000),
    ALL               (0b111111);


    //Package private stuff
    DataMask(final int rawMask) {
        this.rawMask = rawMask;
    }

    public boolean isEnabled(final int mask) {
        return (this.rawMask & mask) != 0;
    }

    public static int toInt(DataMask dataMask, DataMask... dataMasks) {
        int combinedMask = dataMask.rawMask;

        for(DataMask dataMaskI: dataMasks) {
            combinedMask |= dataMaskI.rawMask;
        }

        return combinedMask;
    }

    final int rawMask;
}

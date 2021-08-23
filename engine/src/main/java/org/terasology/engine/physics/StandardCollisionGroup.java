// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics;

import java.util.Locale;

public enum StandardCollisionGroup implements CollisionGroup {
    NONE((short) 0b00000000),
    DEFAULT((short) 0b00000001),
    STATIC((short) 0b00000010),
    KINEMATIC((short) 0b00000100),
    DEBRIS((short) 0b00001000),
    SENSOR((short) 0b00010000),
    CHARACTER((short) 0b00100000),
    WORLD((short) 0b01000000),
    LIQUID((short) 0b10000000),
    ALL((short) 0b11111111);

    private short flag;

    StandardCollisionGroup(short flag) {
        this.flag = flag;
    }

    @Override
    public short getFlag() {
        return flag;
    }

    @Override
    public String getName() {
        return "engine:" + toString().toLowerCase(Locale.ENGLISH);
    }

}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible.constraints;

import org.terasology.nui.Color;

public class ColorConstraint implements SettingConstraint<Color> {

    @Override
    public boolean isSatisfiedBy(Color value) {
        return true;
    }

    @Override
    public void warnUnsatisfiedBy(Color value) {
    }
}

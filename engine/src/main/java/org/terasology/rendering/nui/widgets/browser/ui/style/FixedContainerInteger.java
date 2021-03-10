// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.ui.style;

public class FixedContainerInteger implements ContainerInteger {
    private int value;

    public FixedContainerInteger(int value) {
        this.value = value;
    }

    @Override
    public int getValue(int containerWidth) {
        return value;
    }
}

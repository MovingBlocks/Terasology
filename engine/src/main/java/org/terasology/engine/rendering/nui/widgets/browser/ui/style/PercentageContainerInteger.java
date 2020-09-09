// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.ui.style;

public class PercentageContainerInteger implements ContainerInteger {
    private final int percentage;

    public PercentageContainerInteger(int percentage) {
        this.percentage = percentage;
    }

    @Override
    public int getValue(int containerWidth) {
        return containerWidth * percentage / 100;
    }
}

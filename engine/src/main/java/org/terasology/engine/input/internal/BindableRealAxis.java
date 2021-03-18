// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.internal;

import org.terasology.engine.input.BindAxisEvent;

/**
 * This implementation is linked to a real axis.
 */
public class BindableRealAxis extends AbstractBindableAxis {

    private float targetValue;

    public BindableRealAxis(String id, BindAxisEvent event) {
        super(id, event);
    }

    @Override
    protected float getTargetValue() {
        return targetValue;
    }

    /**
     * @param targetValue
     */
    public void setTargetValue(float targetValue) {
        this.targetValue = targetValue;
    }
}

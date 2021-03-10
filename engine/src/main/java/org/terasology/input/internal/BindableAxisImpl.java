// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.internal;

import org.terasology.engine.input.BindAxisEvent;
import org.terasology.engine.input.BindableButton;
import org.terasology.input.ButtonState;

/**
 * This implementation is linked to a positive BindableButton (that pushes the axis towards 1)
 * and a negative BindableButton (that pushes it towards -1).
 */
public class BindableAxisImpl extends AbstractBindableAxis {
    private BindableButton positiveInput;
    private BindableButton negativeInput;

    public BindableAxisImpl(String id, BindAxisEvent event, BindableButton positiveButton, BindableButton negativeButton) {
        super(id, event);
        this.positiveInput = positiveButton;
        this.negativeInput = negativeButton;
    }

    @Override
    protected float getTargetValue() {
        boolean posInput = positiveInput.getState() == ButtonState.DOWN;
        boolean negInput = negativeInput.getState() == ButtonState.DOWN;

        float targetValue = 0;
        if (posInput) {
            targetValue += 1.0f;
        }
        if (negInput) {
            targetValue -= 1.0f;
        }

        return targetValue;
    }
}

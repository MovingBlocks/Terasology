// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.terasology.engine.entitySystem.event.AbstractValueModifiableEvent;

public class AffectJumpForceEvent extends AbstractValueModifiableEvent {
    public AffectJumpForceEvent(float baseValue) {
        super(baseValue);
    }
}

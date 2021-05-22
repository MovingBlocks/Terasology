// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.event;

import org.terasology.engine.network.NoReplicate;

public abstract class AbstractConsumableEvent implements ConsumableEvent {
    @NoReplicate
    protected boolean consumed;

    @Override
    public void consume() {
        consumed = true;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }
}

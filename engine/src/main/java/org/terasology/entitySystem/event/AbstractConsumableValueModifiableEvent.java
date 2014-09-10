/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.entitySystem.event;

import org.terasology.network.NoReplicate;

public abstract class AbstractConsumableValueModifiableEvent extends AbstractValueModifiableEvent implements ConsumableEvent {
    @NoReplicate
    protected boolean consumed;

    protected AbstractConsumableValueModifiableEvent(float baseValue) {
        super(baseValue);
    }

    @Override
    public void consume() {
        consumed = true;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }
}

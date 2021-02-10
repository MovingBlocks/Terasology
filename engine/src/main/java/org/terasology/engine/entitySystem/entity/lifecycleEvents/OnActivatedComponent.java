/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.entitySystem.entity.lifecycleEvents;

import org.terasology.entitySystem.event.Event;

/**
 * This event occurs after an entity is created, an entity is loaded or a component is added to an entity. This occurs
 * after OnAddedComponent where relevant.
 * <br><br>
 * Note that this event will only be received by @ReceiveEvent methods where all components in its list are present and
 * at least one is involved in the action causing the event.
 *
 */
public final class OnActivatedComponent implements Event {

    private static OnActivatedComponent instance = new OnActivatedComponent();

    private OnActivatedComponent() {
    }

    public static OnActivatedComponent newInstance() {
        return instance;
    }

}

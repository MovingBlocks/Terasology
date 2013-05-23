/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.entitySystem.lifecycleEvents;

import org.terasology.entitySystem.event.AbstractEvent;

/**
 * This event occurs whenever a component is changed.
 * @author Immortius <immortius@gmail.com>
 */
public class OnChangedEvent extends AbstractEvent {

    private static OnChangedEvent instance = new OnChangedEvent();

    public static OnChangedEvent newInstance() {
        return instance;
    }

    private OnChangedEvent() {
    }
}

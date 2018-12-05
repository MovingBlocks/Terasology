/*
 * Copyright 2018 MovingBlocks
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

package org.terasology.rendering.nui;

import com.google.common.collect.Lists;
import org.terasology.rendering.nui.widgets.ActivateEventListener;

import java.util.List;

/**
 * A widget that can be activated when tabbed to.
 */
public abstract class ActivatableWidget extends WidgetWithOrder {
    /**
     * A {@link List} of listeners subscribed to this button
     */
    protected List<ActivateEventListener> listeners = Lists.newArrayList();

    public ActivatableWidget() {
        this.setId("");
    }

    public ActivatableWidget(String id) {
        this.setId(id);
    }

    /**
     * Called when this is pressed to activate all subscribed listeners.
     */
    protected void activateWidget() {
        for (ActivateEventListener listener : listeners) {
            listener.onActivated(this);
        }
    }
}

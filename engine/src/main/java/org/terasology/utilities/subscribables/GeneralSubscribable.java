/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.utilities.subscribables;

import java.beans.PropertyChangeListener;

/**
 * Originally created to provide Observable-like functionality to config classes,
 * it can be used by any class that wishes to be monitored for any change in its properties.
 */
public interface GeneralSubscribable {

    /**
     * Subscribe a listener that gets notified when -any- property changes.
     */
    void subscribe(PropertyChangeListener changeListener);

    /**
     * Unsubscribe a listener that gets notified when -any- property changes.
     */
    void unsubscribe(PropertyChangeListener changeListener);

}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.subscribables;

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

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.subscribables;

import java.beans.PropertyChangeListener;

/**
 * Originally created to provide Observable-like functionality to config classes,
 * it can be used by any class that wishes to be monitored for change in specific properties.
 */
public interface SpecificSubscribable {

    /**
     * Subscribe a listener that gets notified when a -specific- property changes.
     */
    void subscribe(String propertyName, PropertyChangeListener changeListener);

    /**
     * Unsubscribe a listener that gets notified when a -specific- property changes.
     */
    void unsubscribe(String propertyName, PropertyChangeListener changeListener);

}

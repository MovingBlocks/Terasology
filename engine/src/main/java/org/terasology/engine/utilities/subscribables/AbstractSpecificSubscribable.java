// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.subscribables;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AbstractSpecificSubscribable implements SpecificSubscribable {

    protected transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    @Override
    public void subscribe(String propertyName, PropertyChangeListener changeListener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, changeListener);
    }

    @Override
    public void unsubscribe(String propertyName, PropertyChangeListener changeListener) {
        this.propertyChangeSupport.removePropertyChangeListener(propertyName, changeListener);
    }

}

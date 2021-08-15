// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.subscribables;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AbstractGeneralSubscribable implements GeneralSubscribable {

    protected transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    @Override
    public void subscribe(PropertyChangeListener changeListener) {
        this.propertyChangeSupport.addPropertyChangeListener(changeListener);
    }

    @Override
    public void unsubscribe(PropertyChangeListener changeListener) {
        this.propertyChangeSupport.removePropertyChangeListener(changeListener);
    }

}

/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.worldviewer.layers;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.terasology.worldviewer.config.FacetConfig;
import org.terasology.worldviewer.core.Observer;

/**
 * A set of general implementations for {@link FacetLayer}.
 * @author Martin Steiger
 */
public abstract class AbstractFacetLayer implements FacetLayer {

    private boolean isVisible;

    private final Collection<Observer<FacetLayer>> observers = new CopyOnWriteArrayList<>();

    @Override
    public final void addObserver(Observer<FacetLayer> obs) {
        observers.add(obs);
    }

    @Override
    public final void removeObserver(Observer<FacetLayer> obs) {
        observers.remove(obs);
    }

    @Override
    public final boolean isVisible() {
        return isVisible;
    }

    @Override
    public final void setVisible(boolean yesno) {
        if (isVisible != yesno) {
            isVisible = yesno;
            notifyObservers();
        }
    }

    @Override
    public void notifyObservers() {
        for (Observer<FacetLayer> obs : observers) {
            obs.update(this);
        }
    }

    @Override
    public String toString() {
        String name = getFacetClass().getSimpleName();
        name = name.replaceAll("Facet", "").replaceAll("(.)([A-Z])", "$1 $2");
        return name;
    }

    @Override
    public FacetConfig getConfig() {
        return null;
    }
}

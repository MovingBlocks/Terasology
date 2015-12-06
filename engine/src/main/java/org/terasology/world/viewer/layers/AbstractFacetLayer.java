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

package org.terasology.world.viewer.layers;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.terasology.engine.Observer;
import org.terasology.world.generation.WorldFacet;

/**
 * A set of general implementations for {@link FacetLayer}.
 */
public abstract class AbstractFacetLayer implements FacetLayer {

    private boolean isVisible = true;

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
        Class<? extends WorldFacet> facetClass = getFacetClass();
        if (facetClass == null) {
            return super.toString();
        } else {
            String name = facetClass.getSimpleName();
            name = name.replaceAll("Facet", "").replaceAll("(.)([A-Z])", "$1 $2");
            return name;
        }
    }

    private Class<? extends WorldFacet> getFacetClass() {
        Renders anno = getClass().getAnnotation(Renders.class);
        if (anno != null) {
            return anno.value();
        }
        return null;
    }

    @Override
    public FacetLayerConfig getConfig() {
        return null;
    }
}

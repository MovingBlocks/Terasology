// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.viewer.layers;

import org.terasology.engine.Observer;
import org.terasology.world.generation.WorldFacet;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

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

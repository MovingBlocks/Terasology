// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.layers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.world.generation.WorldFacet;
import org.terasology.gestalt.module.ModuleEnvironment;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Additional functionality around the {@link FacetLayer} interface that is
 * independent from any actual implementation.
 */
public final class FacetLayers {

    private static final Logger logger = LoggerFactory.getLogger(FacetLayers.class);

    private FacetLayers() {
        // no instances
    }

    /**
     * @param facets the facets that should be rendered
     * @param environment the module environment that contains the facet layers
     * @return an ordered list of facet layers
     */
    public static List<FacetLayer> createLayersFor(Set<Class<? extends WorldFacet>> facets, ModuleEnvironment environment) {
        Iterable<Class<? extends FacetLayer>> layers = environment.getSubtypesOf(FacetLayer.class);

        List<FacetLayer> loadedLayers = new ArrayList<>();

        for (Class<? extends WorldFacet> facet : facets) {
            loadedLayers.addAll(createLayersFor(facet, layers));
        }

        // sort by annotated z-order
        loadedLayers.sort((l1, l2) -> {
            int o1 = l1.getClass().getAnnotation(Renders.class).order();
            int o2 = l2.getClass().getAnnotation(Renders.class).order();

            return Integer.compare(o1, o2);
        });

        return loadedLayers;
    }


    private static Collection<FacetLayer> createLayersFor(Class<? extends WorldFacet> facetClass,
            Iterable<Class<? extends FacetLayer>> layers) {

        Collection<FacetLayer> result = new ArrayList<>();

        for (Class<? extends FacetLayer> layer : layers) {
            Renders anno = layer.getAnnotation(Renders.class);
            if (anno == null) {
                if (!Modifier.isAbstract(layer.getModifiers())) {
                    // ignore abstract classes
                    logger.warn("FacetLayer class {} is not annotated with @Renders", layer);
                }
            } else {
                Class<? extends WorldFacet> annotated = anno.value();
                if (facetClass.equals(annotated)) {
                    try {
                        FacetLayer instance = layer.newInstance();
                        result.add(instance);
                    } catch (InstantiationException | IllegalAccessException e) {
                        logger.warn("Could not call default constructor for {}", layer);
                    }
                }
            }
        }

        if (result.isEmpty()) {
            logger.warn("No layers found for facet {}", facetClass.getName());  //NOPMD
        }

        return result;
    }
}

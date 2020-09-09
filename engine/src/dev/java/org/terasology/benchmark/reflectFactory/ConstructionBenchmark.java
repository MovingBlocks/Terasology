// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.benchmark.reflectFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.benchmark.AbstractBenchmark;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.reflection.reflect.ReflectFactory;

/**
 *
 */
public class ConstructionBenchmark extends AbstractBenchmark {

    private static final Logger logger = LoggerFactory.getLogger(ConstructionBenchmark.class);
    private final ReflectFactory reflectFactory;
    private ObjectConstructor<LocationComponent> constructor;

    public ConstructionBenchmark(ReflectFactory reflectFactory) {
        super("Construction via " + reflectFactory.getClass().getSimpleName(), 100000000, new int[]{100000000,
                100000000});
        this.reflectFactory = reflectFactory;
    }

    @Override
    public void setup() {
        try {
            constructor = reflectFactory.createConstructor(LocationComponent.class);
        } catch (NoSuchMethodException e) {
            logger.error("Failed to establish constructor object", e);
        }
    }

    @Override
    public void run() {
        constructor.construct();
    }
}

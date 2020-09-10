// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.benchmark.reflectFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.benchmark.AbstractBenchmark;
import org.terasology.nui.reflection.reflect.FieldAccessor;
import org.terasology.nui.reflection.reflect.InaccessibleFieldException;
import org.terasology.nui.reflection.reflect.ReflectFactory;

/**
 *
 */
public class GetterSetterAccessBenchmark extends AbstractBenchmark {

    private static final Logger logger = LoggerFactory.getLogger(ConstructionBenchmark.class);
    private final ReflectFactory reflectFactory;
    private FieldAccessor accessor;
    private int i;
    private GetterSetterComponent comp;

    public GetterSetterAccessBenchmark(ReflectFactory reflectFactory) {
        super("Getter/Setter access via " + reflectFactory.getClass().getSimpleName(), 100000000, new int[]{100000000
                , 100000000});
        this.reflectFactory = reflectFactory;
    }

    @Override
    public void setup() {
        i = 0;
        comp = new GetterSetterComponent();
        try {
            accessor = reflectFactory.createFieldAccessor(GetterSetterComponent.class,
                    GetterSetterComponent.class.getDeclaredField("value"));
        } catch (InaccessibleFieldException | NoSuchFieldException e) {
            logger.error("Failed to establish field accessor object", e);
        }
    }

    @Override
    public void run() {
        accessor.setValue(comp, i++);
        int val = (int) accessor.getValue(comp);
        val++;
    }
}

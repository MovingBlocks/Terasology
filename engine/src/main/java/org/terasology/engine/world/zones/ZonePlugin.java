// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.zones;

import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPlugin;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.math.geom.BaseVector3i;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@API
public abstract class ZonePlugin extends Zone implements WorldGeneratorPlugin {

    public ZonePlugin(String name, BooleanSupplier regionFunction) {
        super(name, regionFunction);
    }

    public ZonePlugin(String name, Predicate<BaseVector3i> regionFunction) {
        super(name, regionFunction);
    }

    public ZonePlugin(String name, BiPredicate<BaseVector3i, Region> regionFunction) {
        super(name, regionFunction);
    }

    public ZonePlugin(String name, ZoneRegionFunction regionFunction) {
        super(name, regionFunction);
    }

}

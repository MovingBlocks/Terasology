// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.zones;

import org.joml.Vector3ic;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPlugin;
import org.terasology.context.annotation.API;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@API
public abstract class ZonePlugin extends Zone implements WorldGeneratorPlugin {

    public ZonePlugin(String name, BooleanSupplier regionFunction) {
        super(name, regionFunction);
    }

    public ZonePlugin(String name, Predicate<Vector3ic> regionFunction) {
        super(name, regionFunction);
    }

    public ZonePlugin(String name, BiPredicate<Vector3ic, Region> regionFunction) {
        super(name, regionFunction);
    }

    public ZonePlugin(String name, ZoneRegionFunction regionFunction) {
        super(name, regionFunction);
    }

}

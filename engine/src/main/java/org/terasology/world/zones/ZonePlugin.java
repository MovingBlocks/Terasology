/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.zones;

import org.terasology.math.geom.BaseVector3i;
import org.terasology.module.sandbox.API;
import org.terasology.world.generation.Region;
import org.terasology.world.generator.plugin.WorldGeneratorPlugin;

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

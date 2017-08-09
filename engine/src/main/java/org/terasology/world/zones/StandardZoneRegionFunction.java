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

import java.util.function.BiFunction;

/**
 * The function to determine whether or not a given block is inside the {@link Zone}.
 */
@API
public class StandardZoneRegionFunction extends ZoneRegionFunction {
    private Zone zone;
    private BiFunction<BaseVector3i, Region, Boolean> regionFunction;

    public StandardZoneRegionFunction(Zone zone, BiFunction<BaseVector3i, Region, Boolean> regionFunction) {
        this.zone = zone;
        this.regionFunction = regionFunction;
    }

    @Override
    public Boolean apply(BaseVector3i pos, Region region) {
        return regionFunction.apply(pos, region);
    }

    public Zone getZone() {
        return zone;
    }

    protected void setZone(Zone zone) {
        this.zone = zone;
    }
}

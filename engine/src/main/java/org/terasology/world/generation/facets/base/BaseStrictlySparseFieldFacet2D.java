/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.world.generation.facets.base;

import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.generation.Border3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class BaseStrictlySparseFieldFacet2D extends BaseFacet2D implements StrictlySparseFieldFacet2D {
    private ArrayList<Optional<Float>> data;

    public BaseStrictlySparseFieldFacet2D(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
        Vector2i size = getRelativeRegion().size();
        this.data = new ArrayList<>(Collections.nCopies(size.x * size.y, Optional.empty()));
    }

    @Override
    public Optional<Float> get(int x, int y) {
        return data.get(getRelativeIndex(x, y));
    }

    @Override
    public Optional<Float> get(BaseVector2i pos) {
        return get(pos.x(), pos.y());
    }

    @Override
    public Optional<Float> getWorld(int x, int y) {
        return data.get(getWorldIndex(x, y));
    }

    @Override
    public Optional<Float> getWorld(BaseVector2i pos) {
        return getWorld(pos.x(), pos.y());
    }

    @Override
    public void set(int x, int y, float value) {
        data.set(getRelativeIndex(x, y), Optional.of(value));
    }

    @Override
    public void set(BaseVector2i pos, float value) {
        set(pos.x(), pos.y(), value);
    }

    @Override
    public void setWorld(int x, int y, float value) {
        data.set(getWorldIndex(x, y), Optional.of(value));
    }

    @Override
    public void setWorld(BaseVector2i pos, float value) {
        setWorld(pos.x(), pos.y(), value);
    }

    @Override
    public void unset(int x, int y) {
        data.set(getRelativeIndex(x, y), Optional.empty());
    }

    @Override
    public void unset(BaseVector2i pos) {
        unset(pos.x(), pos.y());
    }

    @Override
    public void unsetWorld(int x, int y) {
        data.set(getWorldIndex(x, y), Optional.empty());
    }

    @Override
    public void unsetWorld(BaseVector2i pos) {
        unsetWorld(pos.x(), pos.y());
    }
}

/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.core.world.generator.facets;

import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;


public class RoughnessFacet extends Grid2DFloatFacet {


    public RoughnessFacet(Region3i targetRegion, Border3D border, int gridSize) {
        super(targetRegion, border, gridSize);
    }

    //Determines the std. deviation of the height in a given cell and saves it to the facet
    public void calcRoughness(Vector2i gridPoint, BaseFieldFacet2D facet) {

        int halfGridSize = Math.round(gridSize / 2);
        Rect2i gridCell = Rect2i.createFromMinAndMax(gridPoint.sub(halfGridSize, halfGridSize), gridPoint.add(halfGridSize, halfGridSize));
        float deviation = 0;
        float meanValue = meanHeight(gridCell, facet);
        for(BaseVector2i pos : gridCell.contents()) {
            deviation += Math.pow(facet.getWorld(pos) - meanValue,2);
        }

        deviation = TeraMath.sqrt(deviation / (gridCell.area()));
        setWorld(gridPoint, deviation);
    }

    //Speed up calculating mean value of the height in a grid cell
    private float meanHeight(Rect2i gridCell, BaseFieldFacet2D facet) {

        Vector2i max = gridCell.max();
        Vector2i min = gridCell.min();

        Vector2i[] positions = new Vector2i[5];
        positions[0] = new Vector2i(max.x(), max.y());
        positions[1] = new Vector2i(min.x(), min.y());
        positions[2] = new Vector2i(min.x() + gridCell.sizeX(), min.y());
        positions[3] = new Vector2i(min.x(), min.y() + gridCell.sizeY());
        positions[4] = new Vector2i(min.x() + Math.round(0.5f * gridCell.sizeX()), min.y() + Math.round(0.5f * gridCell.sizeY()));

        float mean = 0;

        for (int i = 0; i < positions.length; i++) {
            mean += facet.getWorld(positions[i]);
        }

        return mean / positions.length;
    }

    public float getMeanDeviation() {
        float mean = 0;
        for(BaseVector2i pos : getWorldRegion().contents()) {
            mean += getWorld(pos);
        }
        mean /= getGridRelativeRegion().area();

        return mean;
    }
}



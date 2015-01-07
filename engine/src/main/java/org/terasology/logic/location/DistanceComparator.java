/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.location;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3f;

import java.util.Comparator;

/**
 * Comparator that compares the distances to a location of two Entities.
 * Closer is smaller, hence return -1, which results in lower index for
 * closer the object when sorting.
 * Entities without a location component are assumed to be infinitely far
 * away from the location.
 * The location can be given in the constructor and set to a different value
 * afterwards.
 */
public class DistanceComparator implements Comparator<EntityRef> {
    /**
     * The distance to this point is taken for the comparison of
     * distances.
     */
    private final Vector3f origin;
    /**
     * Used to store the location of Entities temporarily. Having
     * this vector pre-allocated saves a lot of memory allocations for new
     * vectors.
     */
    private final Vector3f temp = new Vector3f();

    /**
     * The default constructor will set the location to calculate the
     * distances from to {0, 0, 0}.
     */
    /**
     * The default constructor will set the location to calculate the
     * distances from to {0, 0, 0}.
     */
    public DistanceComparator() {
        origin = new Vector3f();
    }

    /**
     * Creates this Distance comparator and sets the temp to the
     * given parameter.
     * The temp is used to calculate distances from.
     * @param temp used to calculate distances from when comparing entities.
     */
    /**
     * Creates this Distance comparator and sets the temp to the
     * given parameter.
     * The temp is used to calculate distances from.
     *
     * @param origin used to calculate distances from when comparing entities.
     */
    public DistanceComparator(Vector3f origin) {
        this.origin = new Vector3f(origin);
    }

    @Override
    public int compare(EntityRef o1, EntityRef o2) {
        LocationComponent loc1 = o1.getComponent(LocationComponent.class);
        LocationComponent loc2 = o2.getComponent(LocationComponent.class);
        if (loc1 == null && loc2 == null) {
            return 0;
        } else if (loc1 == null) {
            return 1;
        } else if (loc2 == null) {
            return -1;
        }
        loc1.getWorldPosition(temp);
        temp.sub(origin);
        float dis1 = temp.lengthSquared();
        loc2.getWorldPosition(temp);
        temp.sub(origin);
        float dis2 = temp.lengthSquared();
        if (dis1 < dis2) {
            return -1;
        } else if (dis2 < dis1) {
            return 1;
        } else {
            //dis1 == dis2
            return 0;
        }
    }

    /**
     * Sets the origin, which is used to calculate the distance from.
     * This method should not be called while sorting. If done anyway, the
     * contract of compare method will be broken and the sorting results
     * are undefined, if by chance no Exception is thrown.
     *
     * @param newOrigin the new location to calculate distances from.
     */
    public void setOrigin(Vector3f newOrigin) {
        origin.set(newOrigin);
    }

}

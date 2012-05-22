/*
 * Copyright 2012
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

package org.terasology.game.logic.world;

import org.junit.Before;
import org.junit.Test;
import org.terasology.logic.newWorld.LightPropagator;
import org.terasology.logic.newWorld.NewChunk;
import org.terasology.logic.newWorld.WorldView;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class LightPropagationTest {

    WorldView view;
    LightPropagator propagator;

    @Before
    public void setup() {
        NewChunk[] chunks = new NewChunk[] {new NewChunk(new Vector3i(-1,0,-1)), new NewChunk(new Vector3i(0,0,-1)), new NewChunk(new Vector3i(1,0,-1)),
                new NewChunk(new Vector3i(-1,0,0)), new NewChunk(new Vector3i(0,0,0)), new NewChunk(new Vector3i(1,0,0)),
                new NewChunk(new Vector3i(-1,0,1)), new NewChunk(new Vector3i(0,0,1)), new NewChunk(new Vector3i(1,0,1))};

        view = new WorldView(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1,1,1));
        propagator = new LightPropagator(view);
    }

    @Test
    public void testSunlightPropagationIntoDarkness() {
        view.setSunlight(0,128,0, NewChunk.MAX_LIGHT);
        propagator.propagateOutOfTargetChunk();
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(1 - NewChunk.CHUNK_DIMENSION_X, 0, 1 - NewChunk.CHUNK_DIMENSION_Z), new Vector3i(NewChunk.CHUNK_DIMENSION_X - 1, NewChunk.CHUNK_DIMENSION_Y - 1, NewChunk.CHUNK_DIMENSION_Z - 1))) {
            byte expected = (byte)Math.max(0, NewChunk.MAX_LIGHT - TeraMath.fastAbs(pos.x) - TeraMath.fastAbs(pos.z));
            assertEquals(pos.toString(), expected, view.getSunlight(pos));
        }
    }
}

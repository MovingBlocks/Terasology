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
package org.terasology.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class TeraMathTest {

    @Test
    public void getEdgeRegion() {
        Region3i region = Region3i.createFromMinAndSize(new Vector3i(16,0,16), new Vector3i(16,128,16));
        assertEquals(Region3i.createFromMinMax(new Vector3i(16, 0, 16), new Vector3i(16, 127, 31)), TeraMath.getEdgeRegion(region, Side.LEFT));
    }
}

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
package org.terasology.utilities;


import org.terasology.math.TeraMath;

public class TestMathHelper extends junit.framework.TestCase {
    public void testCantor() throws Exception {
        int test1 = TeraMath.mapToPositive(22);
        assertEquals(22, TeraMath.redoMapToPositive(test1));
        int test2 = TeraMath.mapToPositive(-22);
        assertEquals(-22, TeraMath.redoMapToPositive(test2));

        int cant = TeraMath.cantorize(TeraMath.mapToPositive(-22), TeraMath.mapToPositive(11));
        assertEquals(11, TeraMath.redoMapToPositive(TeraMath.cantorY(cant)));
        assertEquals(-22, TeraMath.redoMapToPositive(TeraMath.cantorX(cant)));
    }
}

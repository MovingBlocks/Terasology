/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.math;


import org.junit.Test;
import org.terasology.math.Lerpable;

import java.util.Random;

import static org.terasology.math.TeraMath.lerp;
import static org.junit.Assert.*;

/**
 * Created by Linus on 1/21/14.
 */
public class LerpableTest {

    private class LerpableDouble implements Lerpable<LerpableDouble> {

        public final double value;

        public LerpableDouble(double value) {
            this.value = value;
        }

        @Override
        public <T extends Lerpable<? super T>> T lerp(T other, double point) {
            return (T)new LerpableDouble(TeraMath.lerp(((LerpableDouble)this).value, ((LerpableDouble)other).value, point));
        }
    };

    @Test
    public void testCorrectness() {
        Random random = new Random(12368);

        for(int i = 0; i < 1000; i++)
        {
            double x1 = (random.nextDouble() - 0.5) * random.nextInt(1000000),
                   y1 = (random.nextDouble() - 0.5) * random.nextInt(1000000),
                   p =  (random.nextDouble() - 0.5) * random.nextInt(1000000);

            LerpableDouble x2 = new LerpableDouble(x1),
                           y2 = new LerpableDouble(y1);

            assertEquals(lerp(x1, y1, p), lerp(x2, y2, p).value, 0);
        }
    }
}

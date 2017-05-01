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

package org.terasology.benchmark;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests {@link Timer}
 * @author Martin Steiger
 */
public class TimerTest {

    @Test
    public void testGet() {
        int delta = 30;
        int w1 = 120;
        int w2 = 190;
        
        Timer p = Timer.start();
        sleep(w1);
        assertEquals(w1, p.get(), delta);

        sleep(w2);
        assertEquals(w1 + w2, p.get(), delta);
    }

    @Test
    public void testPauseResume() {
        int delta = 30;
        int w1 = 120;
        int w2 = 170;
        int w3 = 330;
        
        Timer p = Timer.start();
        sleep(w1);
        p.pause();
        sleep(w2);
        assertEquals(w1, p.get(), delta);
        
        p.resume();
        sleep(w3);
        assertEquals(w1 + w3, p.get(), delta);

        p.pause();
        sleep(w1);
        p.resume();
        sleep(w2);
        assertEquals(w1 + w2 + w3, p.get(), delta);
    }

    @Test
    public void testEvilDev() {
        int delta = 30;
        int w1 = 120;
        int w2 = 170;
        int w3 = 230;
        
        Timer p = Timer.start();
        p.pause();
        p.pause();
        p.pause();
        sleep(w1);
        p.pause();
        sleep(w2);
        assertEquals(0, p.get(), delta);
        
        p.resume();
        p.resume();
        sleep(w3);
        p.resume();
        assertEquals(w3, p.get(), delta);
    }

    /**
     * @param time in milli-secs
     */
    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

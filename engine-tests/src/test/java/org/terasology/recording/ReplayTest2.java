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
package org.terasology.recording;

import org.junit.Ignore;
import org.junit.Test;
import org.terasology.ReplayTestingEnvironment;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;

import static org.junit.Assert.assertEquals;

public class ReplayTest2 extends ReplayTestingEnvironment {

    private Thread t1 = new Thread() {

        @Override
        public void run() {
            try {
                ReplayTest2.super.openReplay("Game 1");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Ignore("Not ready yet, and should be ignored by jenkins")
    @Test
    public void testReplayGame1() {
        t1.start();
        try {
            while (RecordAndReplayStatus.getCurrentStatus() != RecordAndReplayStatus.REPLAY_FINISHED) {
                Thread.sleep(1000);
            }
            System.out.println("THREAD: REPLAY FINISHED! INITIALIZING TESTS...");
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
            EntityRef character = localPlayer.getCharacterEntity();
            LocationComponent location = character.getComponent(LocationComponent.class);
            Vector3f finalPosition = new Vector3f(5.336535f, 19.406195f, -61.026585f);
            assertEquals(finalPosition, location.getLocalPosition());
            System.out.println("TESTS FINISHED");
            super.getHost().shutdown();
            t1.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

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
package org.terasology.replayTests;

import org.junit.Test;
import org.terasology.ReplayTestingEnvironment;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.recording.EventSystemReplayImpl;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.WorldProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ExampleReplayTest extends ReplayTestingEnvironment {

    private Thread replayThread = new Thread() {

        @Override
        public void run() {
            try {
                String replayTitle = "Example";
                ExampleReplayTest.super.openReplay(replayTitle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Test
    public void testExampleRecordingPlayerPosition() {
        replayThread.start();
        try {

            while (RecordAndReplayStatus.getCurrentStatus() != RecordAndReplayStatus.REPLAYING) {
                Thread.sleep(1000); //wait for the replay to finish prepearing things before we get the data to test things.
            }

            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
            EntityRef character = localPlayer.getCharacterEntity();
            Vector3f initialPosition = new Vector3f(19.79358f, 13.511584f, 2.3982882f);
            LocationComponent location = character.getComponent(LocationComponent.class);
            assertEquals(initialPosition, location.getLocalPosition()); // check initial position.

            EventSystemReplayImpl eventSystem = (EventSystemReplayImpl) CoreRegistry.get(EventSystem.class);
            while (RecordAndReplayStatus.getCurrentStatus() != RecordAndReplayStatus.REPLAY_FINISHED) {
                //checks that after a certain point, the player is not on the starting position anymore.
                if (eventSystem.getLastRecordedEventPosition() >= 1810) {
                    location = character.getComponent(LocationComponent.class);
                    assertNotEquals(initialPosition, location.getLocalPosition());
                }
                Thread.sleep(1000);
            }//The replay is finished at this point

            location = character.getComponent(LocationComponent.class);
            Vector3f finalPosition = new Vector3f(25.189344f, 13.406443f, 8.6651945f);
            assertEquals(finalPosition, location.getLocalPosition()); // checks final position

            super.getHost().shutdown();
            replayThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExampleRecordingBlockPlacement() {
        replayThread.start();
        try {
            while (RecordAndReplayStatus.getCurrentStatus() != RecordAndReplayStatus.REPLAYING) {
                Thread.sleep(1000); //wait for the replay to finish prepearing things before we get the data to test things.
            }
            Vector3i blockLocation1 = new Vector3i(26, 12, -3);
            Vector3i blockLocation2 = new Vector3i(26, 13, -3);
            Vector3i blockLocation3 = new Vector3i(26, 12, -2);

            //checks the block initial type of three chunks that will be modified during the replay.
            WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
            assertEquals(worldProvider.getBlock(blockLocation1).getDisplayName(), "Grass");
            assertEquals(worldProvider.getBlock(blockLocation2).getDisplayName(), "Air");
            assertEquals(worldProvider.getBlock(blockLocation3).getDisplayName(), "Grass");

            while (RecordAndReplayStatus.getCurrentStatus() != RecordAndReplayStatus.REPLAY_FINISHED) {
                Thread.sleep(1000);
            }//The replay is finished at this point

            //checks the same blocks again after the replay.
            assertEquals(worldProvider.getBlock(blockLocation1).getDisplayName(), "Grass");
            assertEquals(worldProvider.getBlock(blockLocation2).getDisplayName(), "Grass");
            assertEquals(worldProvider.getBlock(blockLocation3).getDisplayName(), "Air");

            super.getHost().shutdown();
            replayThread.join();
        } catch (Exception e) {
            System.out.println("EXCEPTION!");
            e.printStackTrace();
        }
    }


}

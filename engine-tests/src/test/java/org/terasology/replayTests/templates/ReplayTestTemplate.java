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
package org.terasology.replayTests.templates;


import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.terasology.ReplayTestingEnvironment;
import org.terasology.recording.RecordAndReplayStatus;


/**
 * This is a template with comments to aid in the creation of a replay test.
 * For more information about Replay Tests, see https://github.com/MovingBlocks/Terasology/wiki/Replay-Tests
 */
public class ReplayTestTemplate extends ReplayTestingEnvironment { //Every replay test should extend ReplayTestingEnvironment

    /*
     * To test the replay while it is executing, it is necessary to create threads which will run the replays.
     */
    private Thread replayThread = new Thread() {

        @Override
        public void run() {
            try {
                //This is the title of the replay to be played. It is generally the name of the folder in the 'recordings' directory.
                String replayTitle = "REPLAY_TITLE";

                //This executes the game opening the replay desired for testing. It is always 'TEST_CLASS_NAME.super.openReplay(replayTitle)' .
                ReplayTestTemplate.super.openReplay(replayTitle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @After
    public void closeReplay() throws Exception {
        //these last two lines are important to correctly shutdown the game after the tests are done.
        super.getHost().shutdown();
        replayThread.join();
    }

    @Ignore("This is just a template and should be ignored by Jenkins.")
    @Test
    public void testTemplate() {
        replayThread.start(); //always start the thread before the test, so the replay can execute.
        try {

            /*
            This 'while' is useful because when it is over it means everything in the replay was loaded, which means it
            is possible to test the replay's initial state, such as the player character's initial position.
             */
            while (RecordAndReplayStatus.getCurrentStatus() != RecordAndReplayStatus.REPLAYING) {
                Thread.sleep(1000); //this sleep is optional and is used only to improve the game performance.
            }

            //the checks of initial states should be written here, between the 'while' statements.

            //The code inside this 'while' will execute while the replay is playing.
            while (RecordAndReplayStatus.getCurrentStatus() != RecordAndReplayStatus.REPLAY_FINISHED) {
                //Tests can be written here to check something in the middle of a replay.

                /*
                Example of test: The test in the comment below gets the event system and checks if the RecordedEvent of
                number 1000 was already processed. If it was, it tests something. This is useful to test, for example,
                if a block disappeared after its destruction, if the player location changed after a certain movement
                event was sent.
                 */
                /*
                EventSystemReplayImpl eventSystem = (EventSystemReplayImpl) CoreRegistry.get(EventSystem.class);
                if (eventSystem.getLastRecordedEventPosition() >= 1000) {
                    //test something
                }
                */

                //this sleep is optional and is used only to improve the game performance.
                Thread.sleep(1000);
            }
            //tests can be written here to test something at the end of a replay.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

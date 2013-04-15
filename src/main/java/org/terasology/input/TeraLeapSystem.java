/*
* Copyright 2013 Moving Blocks
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.terasology.input;

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Gesture;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.input.jitter.BufferedJitterSystem;
import org.terasology.input.jitter.JitterSystem;

/**
 * Terasology implementation for Leap Motion using the Jitter library.
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class TeraLeapSystem implements EventHandlerSystem {

    // Idea is that BufferedJitterSystem will store a bunch of batched Leap data
    // Here during update we then examine one batch of data, trigger input events, then reset/consume the buffer

    private JitterSystem jitter;
    private BufferedJitterSystem jitterBuffer;

    public void initialise() {
        jitterBuffer = new BufferedJitterSystem();
        jitter = new JitterSystem(jitterBuffer);

        jitter.enableGesture(Gesture.Type.TYPE_CIRCLE);
        //jitter.enableCircleGestures(true, 1) //"true" enables consuming gestures, 1 is minimal progress before reporting, could also include radius?)

        System.out.println("Jitter says hi");
    }

    @Override
    public void shutdown() {
        //TODO: Doesn't seem to happen. Not configured right with our ES? Had to add update() to StateSinglePlayer too
        System.out.println("TeraLeapSystem.stop()");
        jitter.stop();
    }

    public void update(float delta) {
        //System.out.println("TeraLeapSystem.update() with delta" + delta);
        // We're in the game loop running at game fps
        // Call the consumption methods in BufferedJitterSystem for each type of input that we care about

        for (CircleGesture circleGesture : jitterBuffer.nextCircleBatch(2)) { // only accept if circled entirely twice
            System.out.println("Got a valid circle gesture! " + circleGesture);
            if (JitterSystem.isClockwise(circleGesture)) {
                System.out.println("Enabling god mode!");
            } else {
                System.out.println("Disabling god mode!");
            }

            // Actually don't have anything to do with multiple circle gestures yet ... but ... for pony!
        }


    }

}

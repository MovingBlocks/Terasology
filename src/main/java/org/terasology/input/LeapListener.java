/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.input;

import com.leapmotion.leap.*;
import org.terasology.game.CoreRegistry;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.KeyUpEvent;
import org.terasology.logic.LocalPlayer;

/**
 * Test implementation for the Leap Motion Controller.
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class LeapListener extends Listener {

    int framesTotal = 0;

    public void onInit(Controller controller) {
        System.out.println("Initialized");
    }

    public void onConnect(Controller controller) {
        System.out.println("Connected");
    }

    public void onDisconnect(Controller controller) {
        System.out.println("Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Exited");
    }

    public void onFrame(Controller controller) {

        framesTotal++;

        // Start simple by turning the fps waaaay down. Only check the hand about once a second (at 60 fps)
        if (framesTotal % 59 != 0) {
            return;
        }

        // Grab the previous frame for comparisons
        Frame oldFrame = controller.frame(59);

        // Get the most recent frame and report some basic information
        Frame frame = controller.frame();
        System.out.println("Frame id: " + frame.id()
                + ", timestamp: " + frame.timestamp()
                + ", hands: " + frame.hands().count()
                + ", fingers: " + frame.fingers().count()
                + ", tools: " + frame.tools().count()
                + ", framesTotal: " + framesTotal);

        if (!frame.hands().empty()) {
            // Get the first hand
            Hand hand = frame.hands().get(0);

            // Check if the hand has any fingers
            FingerList fingers = hand.fingers();
            if (!fingers.empty()) {
                // Calculate the hand's average finger tip position
                Vector avgPos = Vector.zero();
                for (Finger finger : fingers) {
                    avgPos = avgPos.plus(finger.tipPosition());
                }
                avgPos = avgPos.divide(fingers.count());
                System.out.println("Hand has " + fingers.count()
                        + " fingers, average finger tip position: " + avgPos);
            }

            // Get the hand's sphere radius and palm position
            System.out.println("Hand sphere radius: " + hand.sphereRadius()
                    + " mm, palm position: " + hand.palmPosition());

            // Cheap dirty way to base an action off a change :D
            Hand oldHand = oldFrame.hands().get(0);
            float y = hand.palmPosition().getY();
            float oldY = oldHand.palmPosition().getY();
            float deltaY = y - oldY;
            System.out.println("Previous hand y was " + oldY + " while new is " + y + " so delta is " + deltaY);

            if (deltaY > 100) {
                // If the player's hand moved back a fair amount, start moving forwards (key 17, default 'w')!
                System.out.println("Detecting RAISED hand, triggering move forward");
                KeyEvent event = KeyDownEvent.create(17, 0f);
                CoreRegistry.get(LocalPlayer.class).getEntity().send(event);
            } else if (deltaY < -100) {
                // Alternatively if the hand moved forward, stop moving forwards!
                System.out.println("Detecting LOWERED hand, cancelling move forward");
                KeyEvent event = KeyUpEvent.create(17, 0f);
                CoreRegistry.get(LocalPlayer.class).getEntity().send(event);
            }

            // Get the hand's normal vector and direction
            Vector normal = hand.palmNormal();
            Vector direction = hand.direction();

            // Calculate the hand's pitch, roll, and yaw angles
            System.out.println("Hand pitch: " + Math.toDegrees(direction.pitch()) + " degrees, "
                    + "roll: " + Math.toDegrees(normal.roll()) + " degrees, "
                    + "yaw: " + Math.toDegrees(direction.yaw()) + " degrees\n");
        }
    }
}

/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.input.binds.ForwardsMovementAxis;
import org.terasology.input.binds.StrafeMovementAxis;
import org.terasology.logic.LocalPlayer;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.physics.character.CharacterMovementComponent;

/**
 * Test implementation for the Leap Motion Controller.
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class LeapSystem implements EventHandlerSystem {

    /** Min width of the space we want to be sensitive toward (closer is "dead space") */
    private static final int MOTION_SPACE_WIDTH_MIN = 40;

    /** Min height of the space we want to be sensitive toward (closer is "dead space") */
    private static final int MOTION_SPACE_HEIGHT_MIN = 200;

    /** Min depth of the space we want to be sensitive toward (closer is "dead space") */
    private static final int MOTION_SPACE_DEPTH_MIN = 40;

    /** Width of the motion space from either side beyond the dead space - 'x' */
    private static final int MOTION_SPACE_WIDTH = 150;

    /** Height of the motion space beyond the minimum height - 'y' */
    private static final int MOTION_SPACE_HEIGHT = 300;

    /** Depth of the motion space from either side beyond the dead space - 'z' */
    private static final int MOTION_SPACE_DEPTH = 150;

    /** Rate at which we'll consider frames from the Leap */
    private static final int LEAP_FRAME_RATE = 10;

    /** Counter for total frames processed */
    private int framesTotal = 0;

    public void initialise() {
        // Doesn't seem to persist variables right the way I use it? Have to set them again inside update() :-(
    }

    @Override
    public void shutdown() {
    }

    public void update(float delta) {
        // Hackity hacky hack - using the Listener directly only survives 56-59 frames then crashes/exits (something I'm missing)
        PerformanceMonitor.startActivity("Leap");

        framesTotal++;

        // Leap by default only stores 59 frames and we might want to process at a lower rate than that
        if (framesTotal % LEAP_FRAME_RATE != 0) {
            return;
        }

        Controller leapController = CoreRegistry.get(Controller.class);
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();

        // Grab the previous frame (that we care about) for comparisons
        //Frame oldFrame = leapController.frame(LEAP_FRAME_RATE);

        // Get the most recent frame and report some basic information
        Frame frame = leapController.frame();
        /*System.out.println("Frame id: " + frame.id()
                + ", timestamp: " + frame.timestamp()
                + ", hands: " + frame.hands().count()
                + ", fingers: " + frame.fingers().count()
                + ", tools: " + frame.tools().count()
                + ", framesTotal: " + framesTotal);
*/
        if (!frame.hands().empty()) {
            // Get the first hand
            Hand hand = frame.hands().get(0);
/*
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
            */

            /*
            // Cheap dirty way to base an action off a change :D
            Hand oldHand = oldFrame.hands().get(0);
            float y = hand.palmPosition().getY();
            float oldY = oldHand.palmPosition().getY();
            float deltaY = y - oldY;
            System.out.println("Previous hand y was " + oldY + " while new is " + y + " so delta is " + deltaY);

            if (deltaY > 100) {
                // If the player's hand moved back a fair amount, start moving forwards (key 17, default 'w')!
                System.out.println("Detecting RAISED hand, triggering move forward");
                BindAxisEvent event = new ForwardsMovementAxis();
                event.prepare("Whatisthisidonteven", 1f, 1f);
                CoreRegistry.get(LocalPlayer.class).getEntity().send(event);
            } else if (deltaY < -100) {
                // Alternatively if the hand moved forward, stop moving forwards!
                System.out.println("Detecting LOWERED hand, cancelling move forward");
                BindAxisEvent event = new ForwardsMovementAxis();
                event.prepare("Whatisthisidonteven", -1f, 1f);
                CoreRegistry.get(LocalPlayer.class).getEntity().send(event);
            }
            */

            // Get the hand's normal vector and direction
            Vector normal = hand.palmNormal();
            Vector direction = hand.direction();

            // Calculate the hand's pitch, roll, and yaw angles
            //System.out.println("Hand pitch: " + Math.toDegrees(direction.pitch()) + " degrees, "
            //        + "roll: " + Math.toDegrees(normal.roll()) + " degrees, "
            //        + "yaw: " + Math.toDegrees(direction.yaw()) + " degrees\n");


            // Use the hand's location relative to the controller to direct player's direction and speed
            float x = hand.palmPosition().getX();
            float y = hand.palmPosition().getY();
            float z = hand.palmPosition().getZ();

            // These ranges relate to how far in the sensitive space the player's hand was (for scaled velocity).
            // Negative results will be within the min space and thus be disqualified, likewise results beyond the max range
            float rangeX = Math.abs(x) - MOTION_SPACE_WIDTH_MIN;
            float rangeZ = Math.abs(z) - MOTION_SPACE_DEPTH_MIN;

            System.out.println("X = " + x + ", Y = " + y + ", Z = " + z);

            // TODO: Make half width/depth enough to reach max walk speed
            // TODO: Apply runFactor when more than half *depth* (no pure run-strafing?)
            // TODO: Use pitch/roll/yaw to change camera direction (impersonate mouse?)

            CharacterMovementComponent characterMovement = playerEntity.getComponent(CharacterMovementComponent.class);
            // Forwards / backwards - also responsible for triggering running or not (which then indirectly can increase strafing speed?)
            if (rangeZ > 0 && rangeZ < MOTION_SPACE_DEPTH) {
                float ratioZ = rangeZ / MOTION_SPACE_DEPTH;

                // If the hand is more than half way through the range we care about then the player is running.
                if (rangeZ > MOTION_SPACE_DEPTH / 2) {
                    // Base running on max walk speed and set the flag. Note that said flag may impact sideways strafing speed?
                    ratioZ = 1;
                    characterMovement.isRunning = true;
                } else {
                    // We're walking, but still need to adjust up speed to max walk speed at half the max range we care about
                    ratioZ *= 2;
                }

                // Reverse direction in case the coordinate was negative
                if (z > 0) {
                    ratioZ *= -1;
                }

                if (characterMovement.isRunning) {
                    System.out.println("ratioZ is " + ratioZ + ", which was based on a range of " + rangeZ + " out of " + MOTION_SPACE_DEPTH + ". Player IS running");
                } else {
                    System.out.println("ratioZ is " + ratioZ + ", which was based on a range of " + rangeZ + " out of " + MOTION_SPACE_DEPTH + " then doubled as the player is NOT running?");
                }

                BindAxisEvent event = new ForwardsMovementAxis();

                // The first and last variables here aren't really needed (refactor?) - first is unused and third relates to delta time which may not matter here?
                event.prepare("leap:forwardbackwards", ratioZ, 1f);
                playerEntity.send(event);

            } else {
                BindAxisEvent event = new ForwardsMovementAxis();
                //System.out.println("Stopping z velocity");
                event.prepare("leap:stopforwardbackwards", 0f, 1f);
                playerEntity.send(event);
            }

            // Left / right
            if (rangeX > 0 && rangeX < MOTION_SPACE_WIDTH) {
                float ratioX = rangeX / MOTION_SPACE_WIDTH;

                // Reverse direction in case the coordinate was negative
                if (x > 0) {
                    ratioX *= -1;
                }

                System.out.println("ratioX is " + ratioX + ", which was based on a range of " + rangeX + " out of " + MOTION_SPACE_WIDTH);
                BindAxisEvent event = new StrafeMovementAxis();

                // The first and last variables here aren't really needed (refactor?) - first is unused and third relates to delta time which may not matter here?
                event.prepare("leap:sideways", ratioX, 1f);
                playerEntity.send(event);
            } else {
                BindAxisEvent event = new StrafeMovementAxis();
                //System.out.println("Stopping x velocity");
                event.prepare("leap:stopsideways", 0f, 1f);
                playerEntity.send(event);
            }

            // Jump
            if (y > MOTION_SPACE_HEIGHT_MIN && y < MOTION_SPACE_HEIGHT) {
                System.out.println("Tank: Load the jump program");
                characterMovement.jump = true;
            }

            characterMovement.isRunning = false;
        }

        PerformanceMonitor.endActivity();
    }


}


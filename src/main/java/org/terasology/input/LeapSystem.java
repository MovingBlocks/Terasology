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

    /** Max width of the motion space from the *center* (so times 2 total) - 'x' */
    private static final int MOTION_SPACE_WIDTH_MAX = 150;

    /** Max height of the motion space from the *ground* (total) - 'y' */
    private static final int MOTION_SPACE_HEIGHT_MAX = 500;

    /** Max depth of the motion space from the *center* (so times 2 total) - 'z' */
    private static final int MOTION_SPACE_DEPTH_MAX = 150;

    /** Min width of the space we want to be sensitive toward (closer is "dead space") */
    private static final int MOTION_SPACE_WIDTH_MIN = 40;

    /** Min height of the space we want to be sensitive toward (closer is "dead space") */
    private static final int MOTION_SPACE_HEIGHT_MIN = 200;

    /** Min depth of the space we want to be sensitive toward (closer is "dead space") */
    private static final int MOTION_SPACE_DEPTH_MIN = 40;

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

        // Start simple by turning the Leap fps waaaay down. Only check the hand about once a second (at 60 fps)
        // Leap by default only stores 59 frames
        if (framesTotal % 59 != 0) {
            return;
        }

        Controller leapController = CoreRegistry.get(Controller.class);
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();

        // Grab the previous frame (that we care about) for comparisons
        //Frame oldFrame = leapController.frame(59);

        // Get the most recent frame and report some basic information
        Frame frame = leapController.frame();
        System.out.println("Frame id: " + frame.id()
                + ", timestamp: " + frame.timestamp()
                + ", hands: " + frame.hands().count()
                + ", fingers: " + frame.fingers().count()
                + ", tools: " + frame.tools().count()
                + ", framesTotal: " + framesTotal);

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
            float absX = Math.abs(x);
            float absZ = Math.abs(z);

            System.out.println("X = " + x + ", Y = " + y + ", Z = " + z);

            // TODO: Make half width/depth enough to reach max walk speed
            // TODO: Use runFactor when more than half *depth* (no pure run-strafing?), but scale it up to max depth
            // TODO: Use pitch/roll/yaw to change camera direction (impersonate mouse?)

            // Left / right
            if (absX < MOTION_SPACE_WIDTH_MAX && absX > MOTION_SPACE_WIDTH_MIN) {
                float xVelocity = (absX - MOTION_SPACE_WIDTH_MIN) / (MOTION_SPACE_WIDTH_MAX - MOTION_SPACE_WIDTH_MIN);
                BindAxisEvent event = new StrafeMovementAxis();
                if (x > 0) {
                    xVelocity *= -1;
                }
                // The first and last variable here aren't really needed - first is unused and third gets reset
                System.out.println("xVelocity is" + xVelocity);
                event.prepare("leap:stopsideways", xVelocity, 1f);
                playerEntity.send(event);
            } else {
                BindAxisEvent event = new StrafeMovementAxis();
                System.out.println("Stopping x velocity");
                event.prepare("leap:stopsideways", 0f, 1f);
                playerEntity.send(event);
            }

            // Jump
            if (y > MOTION_SPACE_HEIGHT_MIN && y < MOTION_SPACE_HEIGHT_MAX) {
                System.out.println("Tank: Load the jump program");
                CharacterMovementComponent characterMovement = playerEntity.getComponent(CharacterMovementComponent.class);
                characterMovement.jump = true;
            }

            // Forwards / backwards
            if (absZ < MOTION_SPACE_DEPTH_MAX && absZ > MOTION_SPACE_DEPTH_MIN) {
                float zVelocity = (absZ - MOTION_SPACE_DEPTH_MIN) / (MOTION_SPACE_DEPTH_MAX - MOTION_SPACE_DEPTH_MIN);
                BindAxisEvent event = new ForwardsMovementAxis();
                if (z > 0) {
                    zVelocity *= -1;
                }
                // The first and last variable here aren't really needed - first is unused and third gets reset
                System.out.println("zVelocity is" + zVelocity);
                event.prepare("leap:stopforwardbackwards", zVelocity, 1f);
                playerEntity.send(event);
            } else {
                BindAxisEvent event = new ForwardsMovementAxis();
                System.out.println("Stopping z velocity");
                event.prepare("leap:stopforwardbackwards", 0f, 1f);
                playerEntity.send(event);
            }

        }

        PerformanceMonitor.endActivity();
    }


}


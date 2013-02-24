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
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.input.binds.AttackButton;
import org.terasology.input.binds.ForwardsMovementAxis;
import org.terasology.input.binds.StrafeMovementAxis;
import org.terasology.input.binds.VerticalMovementAxis;
import org.terasology.input.events.MouseXAxisEvent;
import org.terasology.input.events.MouseYAxisEvent;
import org.terasology.logic.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.physics.character.CharacterMovementComponent;

import javax.vecmath.Vector3f;
import java.util.*;

/**
 * First draft for the LEAP motion player controls.
 *
 * How to get started?
 * -----------------
 *
 * Just put one hand on the front left side of the controller, and one hand on the front right side of the controller.
 * Make sure that no more and no less than one finger tip (clearly pointing in the direction
 * of your computer screen) is visible per hand and that both palms are facing downwards.
 *
 * Tell me... How does it work?
 * -----------------
 *
 * The player is controlled using two hands placed on the front left and the front right
 * of the LEAP Motion controller. The left hand is responsible for moving the player
 * around using forwards/backwards, sidewards upwards/downwards movement. The right hand
 * acts as an virtual mouse which can be used to adjust the player's viewing direction.
 * The input is controlled by analyzing the hand positions relatively to two virtual origin points
 * in the coordinate space of the LEAP Motion controller. If the player's left hand is placed to the
 * right of the left origin point, the player will strafe to the right. If the player's right hand is placed to
 * the left of the right origin point, the player will look to the left. The speed of the view adjustment and
 * the speed of the movement is determined using a specified region around those origin points. The more the
 * hands move away from those origin points, the more intense the action will be.
 *
 * How are the hands detected?
 * -----------------
 *
 * If only one hand is detected by the LEAP Motion controller, it is interpreted as a right hand
 * used for looking around. If a second hand comes into play and is placed on the left side of the controller, it is interpreted as a left hand.
 * Both hands are cached so they can keep their initial task until the controller might eventually loose track of them and they have to
 * be reassigned.
 *
 * NOTE: The hands HAVE to be placed on the left and right side to ensure a less error prone separation of the hands. If
 * a previous left hand moves to the right side of the controller (or if a right hand...), both cached hands get invalidated
 * and reassigned to ensure less sudden movements and overall artifacts.
 *
 * How does the movement work?
 * -----------------
 *
 * Movement works as stated above. Additionally jumping can be triggered by a sudden movement of the left palm
 * along the positive y-axis. The player can be stopped by moving the left hand out of the range of the controller
 * or by forming a stop sign (point the palm of the left hand in direction of the screen.)
 *
 * How does looking around work?
 * -----------------
 *
 * Looking around works as stated above. An attack movement can be triggered by wiggling your right finger up and down.
 *
 * Summary
 * -----------------
 *
 * Left hand: Movement (forwards/backwards, strafing, upwards(downwards)) and jumping
 * Right hand: Looking around and attacking
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public class LeapSystem implements EventHandlerSystem {

    /** Rate at which we'll consider frames from the Leap */
    private static final int LEAP_FRAME_RATE = 1;

    /** The 'virtual' origin for the left hand */
    private static final Vector3f LEFT_HAND_ORIGIN = new Vector3f(-120.0f, 180.0f, 180.0f);
    /** The 'virtual' origin for the right hand */
    private static final Vector3f RIGHT_HAND_ORIGIN = new Vector3f(120.0f, 180.0f, 180.0f);
    /** The area around the origins to control the 'intensity' of the movement */
    private static final float MOVEMENT_ADAPTATION_LENGTH = 50.0f;
    /** The relative hand position to start running (tracked along the y-axis) */
    private static final float RUNNING_REL_THRESHOLD = 0.5f;
    /** The velocity threshold to start a jump (tracked by the left hand) */
    private static final float JUMPING_THRESHOLD = 750.0f;
    /** The velocity threshold to start an attack (first finger of the right hand is tracked) */
    private static final float ATTACK_THRESHOLD = 500.0f;
    /** The sensitivity for looking around in the first person perspective (right hand) */
    private static final float LOOKING_SENSITIVITY = 10.0f;
    /** Palm normal threshold for stopping movement */
    private static final float PALM_NORMAL_THRESHOLD_MOVEMENT = -0.8f;
    /** The amount of frames to consider for averaging the position, normal and velocity. */
    private static final int DEFAULT_PAST_FRAMES_TO_CONSIDER = 10;
    /** Time to disable looking during attacking. */
    private static final float ATTACK_TIMEOUT = 0.1f;

    /** Various components that are used to control the game via the LEAP controller  */
    private LocalPlayer localPlayer;
    private CameraTargetSystem cameraTargetSystem;
    private Controller leapController;

    /** Stores the IDs of the currently tracked left and right hand */
    private int currentLeftHand = -1;
    private int currentRightHand = -1;

    /** The time since the last attack by the player. */
    private float timeSinceLastAttack = 0.0f;

    /** Counter for total frames processed */
    private int framesTotal = 0;

    /** Flag for not halting movement more than once (when a hand is lost) */
    private boolean shouldLostHandCancelMovement = true;

    public void initialise() {
        leapController = CoreRegistry.get(Controller.class);
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
        leapController.enableGesture(Gesture.Type.TYPE_CIRCLE);
        leapController.enableGesture(Gesture.Type.TYPE_KEY_TAP);
    }

    @Override
    public void shutdown() {
    }

    public void update(float delta) {
        framesTotal++;

        // Leap by default only stores 59 frames and we might want to process at a lower rate than that
        if (framesTotal % LEAP_FRAME_RATE != 0) {
            return;
        }

        PerformanceMonitor.startActivity("Leap");

        // There are certain scenarios where the currently tracked hands should be invalidated...
        // One nasty example is when the left hand has moved over in the area of the right hand (or reverse)
        // In that case the LEAP controller looses track of one hand and one of the hands can suddenly become
        // interpreted as the opposite one (left hand is suddenly used for looking around or the right hand suddenly controls
        // the movement very firmly...). Since the origins are not altered dynamically the player might suddenly
        // start to look in the sky and rotate or walk from a mountain with high-speed.
        // For this case...

        Frame frame = leapController.frame();

        // Check if the current left hand is in the area of the right hand...
        if (currentLeftHand != -1
                && frame.hand(currentLeftHand).isValid()
                && frame.hand(currentLeftHand).palmPosition().getX() > 0.0f) {
            currentLeftHand = -1;
        }

        // Or if the current right hand is in the area of the left hand...
        if (currentRightHand != -1
                && frame.hand(currentRightHand).isValid()
                && frame.hand(currentRightHand).palmPosition().getX() < 0.0f) {
            currentRightHand = -1;
        }

        // The right hand is used for looking around in the first person view...

        LinkedList<Hand> rightHandCandidates = new LinkedList<Hand>();

        // Start out by putting all right hand candidates in a list ( they have to be on the right side of the leap controller)
        // DO NOT add hands that are currently being tracked!
        for (int i=0; i<frame.hands().count(); ++i) {
            if (frame.hands().get(i).id() != currentLeftHand
                    && frame.hands().get(i).id() != currentRightHand) {

                Hand hand = frame.hands().get(i);
                if (hand.isValid() && hand.palmPosition().getX() > 0.0f) {
                    rightHandCandidates.add(hand);
                }

            }
        }

        // If we've got multiple hands in the right hand area... Take the one closest to the virtual origin
        Collections.sort(rightHandCandidates, new Comparator<Hand>() {
            @Override
            public int compare(Hand o1, Hand o2) {
                Vector3f handPositionA = TeraMath.convert(o1.palmPosition());
                Vector3f originToHandPosA = new Vector3f();
                originToHandPosA.sub(handPositionA, RIGHT_HAND_ORIGIN);

                Vector3f handPositionB = TeraMath.convert(o2.palmPosition());
                Vector3f originToHandPosB = new Vector3f();
                originToHandPosB.sub(handPositionB, RIGHT_HAND_ORIGIN);

                float distA = originToHandPosA.length();
                float distB = originToHandPosB.length();

                if (distA > distB) {
                    return 1;
                } else if (distA < distB) {
                    return -1;
                }

                return 0;
            }
        });

        // If there are valid right hand candidates and the current right hand is invalid (or has never been used before)...
        if (!rightHandCandidates.isEmpty()) {
            if (currentRightHand == -1 || !frame.hand(currentRightHand).isValid()) {
                System.out.println("New 'LOOKING' hand found!");

                // .. replace the current hand with the best right hand candidate
                Hand h = rightHandCandidates.removeFirst();
                currentRightHand = h.id();
            }
        }

        // The same goes for the left hand...

        LinkedList<Hand> leftHandCandidates = new LinkedList<Hand>();

        // Start out by putting all left hand candidates in a list ( they have to be on the left side of the leap controller)
        // DO NOT add hands that are currently being tracked!
        for (int i=0; i<frame.hands().count(); ++i) {
            // Only add hands that are not used for something, yet...
            if (frame.hands().get(i).id() != currentLeftHand
                    && frame.hands().get(i).id() != currentRightHand) {

                Hand hand = frame.hands().get(i);
                if (hand.isValid() && hand.palmPosition().getX() < 0.0f) {
                    leftHandCandidates.add(hand);
                }

            }
        }

        // If we've got multiple hands in the left hand area... Take the one closest to the virtual origin
        Collections.sort(leftHandCandidates, new Comparator<Hand>() {
            @Override
            public int compare(Hand o1, Hand o2) {
                Vector3f handPositionA = TeraMath.convert(o1.palmPosition());
                Vector3f originToHandPosA = new Vector3f();
                originToHandPosA.sub(handPositionA, LEFT_HAND_ORIGIN);

                Vector3f handPositionB = TeraMath.convert(o2.palmPosition());
                Vector3f originToHandPosB = new Vector3f();
                originToHandPosB.sub(handPositionB, LEFT_HAND_ORIGIN);

                float distA = originToHandPosA.length();
                float distB = originToHandPosB.length();

                if (distA > distB) {
                    return 1;
                } else if (distA < distB) {
                    return -1;
                }

                return 0;
            }
        });

        // If there are valid left hand candidates and the current left hand is invalid (or has never been used before)...
        if (!leftHandCandidates.isEmpty()) {
            if (currentLeftHand == -1 || !frame.hand(currentLeftHand).isValid()) {
                System.out.println("New 'MOVEMENT' hand found!");

                // .. replace the current hand with the best left hand candidate
                Hand h = leftHandCandidates.removeFirst();
                currentLeftHand = h.id();
            }
        }

        // Check if we've got a valid right hand available...
        if (currentRightHand != -1 && frame.hand(currentRightHand).isValid()) {
            // ... and use it for letting the player look around (like a virtual mouse)
            doPlayerLooking(currentRightHand, delta);
        }

        // Check if we've got a valid left hand available...
        if (currentLeftHand != -1 && frame.hand(currentLeftHand).isValid()) {
            // ... and use it for moving the player around (walking forwards/backwards, upwards/downwards or left and right)
            doPlayerMovement(currentLeftHand, delta);
            shouldLostHandCancelMovement = true;
        } else if (shouldLostHandCancelMovement) {
            // No hand available? Halt the machine! But only once, so player can switch to keyboard movement if desired
            doPlayerMovement(-1, delta);
            shouldLostHandCancelMovement = false;
        }

        // Check if we've got valid gestures (for either hand) to process
        GestureList gestures = frame.gestures();
        for (int i = 0; i < gestures.count(); i++) {
            Gesture gesture = gestures.get(i);

            switch (gesture.type()) {
                case TYPE_CIRCLE:
                    CircleGesture circle = new CircleGesture(gesture);

                    // Calculate clock direction using the angle between circle normal and pointable
                    boolean clockwise = circle.pointable().direction().angleTo(circle.normal()) <= Math.PI/4;

                    // When a circle is completed act on its clock direction
                    if (circle.state() == Gesture.State.STATE_STOP) {
                        CharacterMovementComponent movComp = localPlayer.getEntity().getComponent(CharacterMovementComponent.class);
                        if (clockwise) {
                            // Enable God Mode (ghosting)
                            System.out.println("Enabling god mode");
                            movComp.isGhosting = true;

                        } else {
                            // Disable God Mode (ghosting)
                            System.out.println("Disabling god mode");
                            movComp.isGhosting = false;
                        }
                    }

                    break;
                case TYPE_KEY_TAP:
                    BindButtonEvent eventAttack = new AttackButton();
                    eventAttack.prepare("leap:attack", ButtonState.DOWN, 0.0f);
                    eventAttack.setTarget(cameraTargetSystem.getTarget(), cameraTargetSystem.getTargetBlockPosition(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal());
                    localPlayer.getEntity().send(eventAttack);

                    break;
                default:
                    System.out.println("Unknown gesture type.");
                    break;
            }
        }

        PerformanceMonitor.endActivity();
    }

    public void doPlayerMovement(int leftHandId, float delta) {

        Hand hand = leapController.frame().hand(leftHandId);
        CharacterMovementComponent movComp = localPlayer.getEntity().getComponent(CharacterMovementComponent.class);

        BindAxisEvent eventForwardsAndBackwards = new ForwardsMovementAxis();
        BindAxisEvent eventLeftAndRight = new StrafeMovementAxis();
        BindAxisEvent eventUpAndDown = new VerticalMovementAxis();

        boolean stopMovement = !hand.isValid();

        Vector3f palmNormal = calcAveragePalmNormal(leftHandId, DEFAULT_PAST_FRAMES_TO_CONSIDER);
        stopMovement |= palmNormal.y > PALM_NORMAL_THRESHOLD_MOVEMENT;

        if (stopMovement) {
            eventForwardsAndBackwards.prepare("leap:forwardAndBackwards", 0.0f, 1f);
            localPlayer.getEntity().send(eventForwardsAndBackwards);

            eventLeftAndRight.prepare("leap:leftAndRight", 0.0f, 1f);
            localPlayer.getEntity().send(eventLeftAndRight);

            eventUpAndDown.prepare("leap:eventUpAndDown", 0.0f, 1f);
            localPlayer.getEntity().send(eventUpAndDown);

            movComp.isRunning = false;

            return;
        }

        // Use the hand's location relative to the controller to direct player's direction and speed
        Vector3f absPosition = calcAveragePalmPosition(leftHandId, DEFAULT_PAST_FRAMES_TO_CONSIDER);

        // Use this palm's velocity to determine whether or not the player should jump
        Vector3f velocity = calcAveragePalmVelocity(leftHandId, DEFAULT_PAST_FRAMES_TO_CONSIDER);

        Vector3f relPosition = new Vector3f();
        relPosition.sub(absPosition, LEFT_HAND_ORIGIN);

        Vector3f normOffset = new Vector3f(
               -relPosition.x / MOVEMENT_ADAPTATION_LENGTH,
               relPosition.y / MOVEMENT_ADAPTATION_LENGTH,
               -relPosition.z / MOVEMENT_ADAPTATION_LENGTH
        );

        normOffset.clamp(-1.0f, 1.0f);

        // Trigger running
        movComp.isRunning = normOffset.y > RUNNING_REL_THRESHOLD;

        // Trigger a jump if the vertical velocity is high enough
        movComp.jump = velocity.y > JUMPING_THRESHOLD;

        //if (!suddenMovement) {
            eventForwardsAndBackwards.prepare("leap:forwardAndBackwards", normOffset.z, 1f);
            localPlayer.getEntity().send(eventForwardsAndBackwards);

            eventLeftAndRight.prepare("leap:leftAndRight", normOffset.x, 1f);
            localPlayer.getEntity().send(eventLeftAndRight);

            eventUpAndDown.prepare("leap:eventUpAndDown", normOffset.y, 1f);
            localPlayer.getEntity().send(eventUpAndDown);
        //}
    }

    public void doPlayerLooking(int rightHandId, float delta) {
        Hand hand = leapController.frame().hand(rightHandId);

        if (!hand.isValid()) {
            return;
        }

        // Use the hand's location relative to the controller to direct player's direction and speed
        Vector3f absPosition = calcAveragePalmPosition(rightHandId, DEFAULT_PAST_FRAMES_TO_CONSIDER);

        Vector3f relPosition = new Vector3f();
        relPosition.sub(absPosition, RIGHT_HAND_ORIGIN);

        Vector3f normOffset = new Vector3f(
                -relPosition.x / MOVEMENT_ADAPTATION_LENGTH,
                relPosition.y / MOVEMENT_ADAPTATION_LENGTH,
                -relPosition.z / MOVEMENT_ADAPTATION_LENGTH
        );

        normOffset.clamp(-1.0f, 1.0f);

        MouseXAxisEvent mouseXAxisEvent = new MouseXAxisEvent(relPosition.x / LOOKING_SENSITIVITY, 0f);
        localPlayer.getEntity().send(mouseXAxisEvent);
        MouseYAxisEvent mouseYAxisEvent = new MouseYAxisEvent(relPosition.y / LOOKING_SENSITIVITY, 0f);
        localPlayer.getEntity().send(mouseYAxisEvent);
    }

    Vector3f calcAveragePalmPosition(int handId, int pastFrames) {
        float count = 1.0f;
        Vector3f position = TeraMath.convert(leapController.frame(0).hand(handId).palmPosition());
        for (int i=0; i<pastFrames; ++i) {
            Hand handFromThePast = leapController.frame(i+1).hand(handId);

            if (handFromThePast.isValid()) {
                position.add(TeraMath.convert(handFromThePast.palmPosition()));
                count += 1.0f;
            }
        }

        position.scale(1.0f / count);
        return position;
    }

    Vector3f calcAveragePalmNormal(int handId, int pastFrames) {
        float count = 1.0f;
        Vector3f velocity = TeraMath.convert(leapController.frame(0).hand(handId).palmNormal());
        for (int i=0; i<pastFrames; ++i) {
            Hand handFromThePast = leapController.frame(i+1).hand(handId);

            if (handFromThePast.isValid()) {
                velocity.add(TeraMath.convert(handFromThePast.palmNormal()));
                count += 1.0f;
            }
        }

        velocity.scale(1.0f / count);
        return velocity;
    }

    Vector3f calcAveragePalmVelocity(int handId, int pastFrames) {
        float count = 1.0f;
        Vector3f velocity = TeraMath.convert(leapController.frame(0).hand(handId).palmVelocity());
        for (int i=0; i<pastFrames; ++i) {
            Hand handFromThePast = leapController.frame(i+1).hand(handId);

            if (handFromThePast.isValid()) {
                velocity.add(TeraMath.convert(handFromThePast.palmVelocity()));
                count += 1.0f;
            }
        }

        velocity.scale(1.0f / count);
        return velocity;
    }

    Vector3f calcAverageTipVelocity(int handId, int fingerId, int pastFrames) {
        float count = 1.0f;
        Vector3f velocity = TeraMath.convert(leapController.frame(0).hand(handId).finger(fingerId).tipVelocity());
        for (int i=0; i<pastFrames; ++i) {
            Hand handFromThePast = leapController.frame(i+1).hand(handId);
            Finger fingerFromThePast = handFromThePast.finger(fingerId);

            if (handFromThePast.isValid() && fingerFromThePast.isValid()) {
                velocity.add(TeraMath.convert(fingerFromThePast.tipVelocity()));
                count += 1.0f;
            }
        }

        velocity.scale(1.0f / count);
        return velocity;
    }
}


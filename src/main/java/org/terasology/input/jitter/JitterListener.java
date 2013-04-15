//TODO: Add license header when properly externalized as a library - already discussed with Marcel

package org.terasology.input.jitter;

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.SwipeGesture;

/**
 * "External" listener for Leap Motion Controller input provided by the Leap software.
 * Used by Jitter to forward gesture calls from to an internal listener to an implementer.
 *
 * Based on gesture_recognition.pde by Marcel Schwittlick for LeapMotionP5 - https://github.com/mrzl/LeapMotionP5
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public interface JitterListener {

    /**
     * Called when the Leap Listener detects a circle gesture.
     * @param gesture the CircleGesture detected
     */
    void circleGestureRecognized(CircleGesture gesture);

    /**
     * Called when the Leap Listener detects a swipe gesture.
     * @param gesture the SwipeGesture detected
     */
    void swipeGestureRecognized(SwipeGesture gesture);

    /**
     * Called when the Leap Listener detects a screen tap gesture (a finger tap directly toward the screen).
     * @param gesture the ScreenTapGesture detected
     */
    void screenTapGestureRecognized(ScreenTapGesture gesture);

    /**
     * Called when the Leap Listener detects a key tap gesture (a finger tap downwards as if hitting a key).
     * @param gesture the KeyTapGesture detected
     */
    void keyTapGestureRecognized(KeyTapGesture gesture);
}

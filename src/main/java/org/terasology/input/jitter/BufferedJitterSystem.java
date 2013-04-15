//TODO: Add license header when properly externalized as a library - already discussed with Marcel

package org.terasology.input.jitter;

import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.SwipeGesture;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.ScreenTapGesture;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Second layer to Jitter providing higher level functionality based on receiving processed input from JitterListener.
 * Buffers the input read at "Leap FPS" for easy consumption at a lower "Application FPS" without missing frames.
 * Application-specific implementations can simply request batched data through very exact method calls.
 * Some methods additionally support filtering the batched data further.
 *
 * General design notes:
 * Leap gestures come in three stages - started, updated, stopped. Discrete gestures only have the stopped state.
 * - Started: first frame making up a continuous gesture. Add it to the buffer since we know it is brand new.
 * - Updated: later frame in a continuous gesture. Replace any existing buffer entry with the latest frame.
 *      If a gesture can be "consumed" by use in an implementation then it gets removed and is ignored if seen again.
 * - Stopped: final/only gesture frame. Unless already consumed add/overwrite in buffer and remove from "consumed" list.
 *
 * Buffers are filled by calls coming from JitterListener and are consumed by calls to the batch return methods.
 * Those methods may be picky and not accept all buffered gestures and should remove "stopped" gestures from the buffer.
 *
 * To hide more technical Leap details this class could offer "user friendly" gesture enabling methods that include
 * details on the minimum sensitivity of gestures as well as whether said gestures are "consumed" when returned.
 *
 * Based on gesture_recognition.pde by Marcel Schwittlick for LeapMotionP5 - https://github.com/mrzl/LeapMotionP5
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class BufferedJitterSystem implements JitterListener {

    /** Buffer for CircleGestures with their IDs as keys */
    private ConcurrentSkipListMap<Integer, CircleGesture> circleGestures = new ConcurrentSkipListMap<Integer, CircleGesture>();

    /** List of consumed IDs for circleGestures (gestures that have been marked as "spent") */
    private ConcurrentSkipListSet<Integer> consumedCircles = new ConcurrentSkipListSet<Integer>();

    /**
     * Accepts input via JitterListener and adds to a local buffer when appropriate.
     *
     * @param gesture the CircleGesture detected
     */
    @Override
    public void circleGestureRecognized(CircleGesture gesture) {
        if (gesture.state() == Gesture.State.STATE_STOP) {
            // A gesture in STOP state may have previously existed, we effectively treat it as an update
            // The processing method will be responsible for removing it from the buffer later
            //System.out.println("Circle stopped, adding/updating gesture in buffer one last time");

            // For stops we check if the gesture has been consumed previously in which case we won't re-add it
            // If consumption isn't enabled this if will always pass since the consumedCircles will be empty
            if (!consumedCircles.contains(gesture.id())) {
                circleGestures.put(gesture.id(), gesture);
            }

            System.out.println("//////////////////////////////////////");
            System.out.println("Gesture type: " + gesture.type().toString());
            System.out.println("ID: " + gesture.id());
            System.out.println("Radius: " + gesture.radius());
            System.out.println("Normal: " + gesture.normal());
            System.out.println("Clockwise: " + JitterSystem.isClockwise(gesture));
            System.out.println("Turns: " + gesture.progress());
            System.out.println("Center: " + gesture.center());
            System.out.println("Duration: " + gesture.durationSeconds() + "s");
            System.out.println("//////////////////////////////////////");
        } else if (gesture.state() == Gesture.State.STATE_START) {
            //System.out.println("Circle started, adding gesture to buffer");

            // A gesture in START state has never been seen before, so we just add it to the buffer blindly
            circleGestures.put(gesture.id(), gesture);

        } else if (gesture.state() == Gesture.State.STATE_UPDATE) {
            //System.out.println("Circle updated, updating it in the buffer (unless previously consumed)");

            // For updates we check if the gesture has been consumed previously in which case we won't re-add it
            // If consumption isn't enabled this if will always pass since the consumedCircles will be empty
            if (!consumedCircles.contains(gesture.id())) {
                circleGestures.put(gesture.id(), gesture);
            }
        }
    }

    //TODO: Refactor to follow a similar approach as circle gestures
    @Override
    public void swipeGestureRecognized(SwipeGesture gesture) {
        if (gesture.state() == Gesture.State.STATE_STOP) {
            System.out.println("//////////////////////////////////////");
            System.out.println("Gesture type: " + gesture.type());
            System.out.println("ID: " + gesture.id());
            System.out.println("Position: " + gesture.position());
            System.out.println("Direction: " + gesture.direction());
            System.out.println("Duration: " + gesture.durationSeconds() + "s");
            System.out.println("Speed: " + gesture.speed());
            System.out.println("//////////////////////////////////////");
        } else if (gesture.state() == Gesture.State.STATE_START) {
            System.out.println("Swipe started");
        } else if (gesture.state() == Gesture.State.STATE_UPDATE) {
            System.out.println("Swipe updated");
        }
    }

    //TODO: Refactor to follow a similar approach as circle gestures
    @Override
    public void screenTapGestureRecognized(ScreenTapGesture gesture) {
        if (gesture.state() == Gesture.State.STATE_STOP) {
            System.out.println("//////////////////////////////////////");
            System.out.println("Gesture type: " + gesture.type());
            System.out.println("ID: " + gesture.id());
            System.out.println("Position: " + gesture.position());
            System.out.println("Direction: " + gesture.direction());
            System.out.println("Duration: " + gesture.durationSeconds() + "s");
            System.out.println("//////////////////////////////////////");
        } else if (gesture.state() == Gesture.State.STATE_START) {
            System.out.println("Screen tap started");
        } else if (gesture.state() == Gesture.State.STATE_UPDATE) {
            System.out.println("Screen tap updated");
        }
    }

    //TODO: Refactor to follow a similar approach as circle gestures
    @Override
    public void keyTapGestureRecognized(KeyTapGesture gesture) {
        if (gesture.state() == Gesture.State.STATE_STOP) {
            System.out.println("//////////////////////////////////////");
            System.out.println("Gesture type: " + gesture.type());
            System.out.println("ID: " + gesture.id());
            System.out.println("Position: " + gesture.position());
            System.out.println("Direction: " + gesture.direction());
            System.out.println("Duration: " + gesture.durationSeconds() + "s");
            System.out.println("//////////////////////////////////////");
        } else if (gesture.state() == Gesture.State.STATE_START) {
            System.out.println("Key tap started");
        } else if (gesture.state() == Gesture.State.STATE_UPDATE) {
            System.out.println("Key tap updated");
        }
    }

    // CIRCLE NOTE: on enabling circle gestures should indicate whether they should be "consumed" on use
    // Usage of *more than one* variant of nextWhateverBatch at the same time may be bad and cause unexpected results

    //TODO: Make implementer set it instead. Enable gestures through here (through JitterSystem) including consumption?
    boolean consumptionEnabled = true;

    //TODO: Support filtering gestures by hand? But it would have to be a persistent hand ID or we'd lose buffers ...

    /**
     * Returns the next CircleGesture in buffer, if any.
     * Note that consuming buffered gestures does not respect different ways to use the gesture.
     * If it is returned for use just once it is considered spent and will not be returned again.
     * @return a CircleGesture or null if none are available
     */
    public Set<CircleGesture> nextCircleBatch() {
        Set<CircleGesture> circleBatch = new HashSet<CircleGesture>();

        for (CircleGesture circleGesture : circleGestures.values()) {

            // Automatically add every circle to the return batch since we have no constraints to test here
            circleBatch.add(circleGesture);

            // Consume (if that's enabled)
            consumeCircle(circleGesture);

            // Remove stopped circles from the buffer as they won't be seen again.
            removeStoppedCircles(circleGesture);
        }

        return circleBatch;
    }

    /**
     * Returns the next CircleGesture in buffer that has made progress to at least the supplied parameter (inclusive)
     * @param progress a float describing the number of circles (fractional or not) the gesture has completed
     * @return a CircleGesture matching the request or null if none are available
     */
    public Set<CircleGesture> nextCircleBatch(float progress) {
        Set<CircleGesture> circleBatch = new HashSet<CircleGesture>();

        //System.out.println("nextCircleBatch started with " + circleGestures.size() + " entries in the buffer");

        for (CircleGesture circleGesture : circleGestures.values()) {

            // Test against constraints here and add only if the gesture passes muster
            if (circleGesture.progress() >= progress) {
                System.out.println("Circle gesture has progressed sufficiently, allowing it to be processed");
                circleBatch.add(circleGesture);

                // Consume (if that's enabled)
                consumeCircle(circleGesture);

            } //else {
                //System.out.println("Circle gesture hasn't progressed enough to be considered yet");
            //}

            // Remove stopped circles (won't be seen again). With constraints some circles may never have been used
            removeStoppedCircles(circleGesture);
        }

        //System.out.println("nextCircleBatch returning " + circleBatch.size() + " entries, finished with " + circleGestures.size() + " entries in the buffer");

        return circleBatch;
    }

    /**
     * Returns the next CircleGesture in buffer that has made progress to at least the supplied parameters (inclusive)
     * @param progress a float describing the number of circles (fractional or not) the gesture has completed
     * @param radius a float for the minimum radius circles to consider (pass '0' progress to solely consider radius)
     * @return a CircleGesture matching the request or null if none are available
     */
    public Set<CircleGesture> nextCircleBatch(float progress, float radius) {
        Set<CircleGesture> circleBatch = new HashSet<CircleGesture>();

        for (CircleGesture circleGesture : circleGestures.values()) {

            // Test against constraints here and add only if the gesture passes muster
            if (circleGesture.progress() >= progress && circleGesture.radius() >= radius) {
                System.out.println("Circle gesture has progressed sufficiently, allowing it to be processed");
                circleBatch.add(circleGesture);

                // Consume (if that's enabled)
                consumeCircle(circleGesture);

            }// else {
                //System.out.println("Circle gesture hasn't progressed enough to be considered yet");
            //}

            // Remove stopped circles (won't be seen again). With constraints some circles may never have been used
            removeStoppedCircles(circleGesture);
        }

        return circleBatch;
    }

    /**
     * Helper method that processes a CircleGesture as consumed if that functionality is enabled.
     * @param circleGesture the CircleGesture to consume
     */
    private void consumeCircle(CircleGesture circleGesture) {
        // If gestures of this type are considered consumed when returned for processing then flag & remove
        if (consumptionEnabled) {
            System.out.println("Consuming circle gesture with id: " + circleGesture.id());
            consumedCircles.add(circleGesture.id());    // Mark circle as consumed so it won't get re-added
            circleGestures.remove(circleGesture.id());  // Remove it from the buffer so it won't be tested again
        } // Else then the gesture stays in the buffer till the STOP state 'if' later removes it
    }

    /**
     * Helper method that removes a CircleGesture from the buffer if it has reached a stopped state.
     * Also removes the circle's ID from the consumed list (if enabled) so we keep that list nice and tiny.
     * @param circleGesture the CircleGesture that just stopped
     */
    private void removeStoppedCircles(CircleGesture circleGesture) {
        if (circleGesture.state() == Gesture.State.STATE_STOP) {

            circleGestures.remove(circleGesture.id());

            // Additionally clear out the consumption status if enabled - even if we *just* consumed the gesture ;-)
            if (consumptionEnabled) {
                consumedCircles.remove(circleGesture.id());
                System.out.println("Just removed gesture with id " + circleGesture.id() + " from the 'consumed' list");
            }
        }
    }
}

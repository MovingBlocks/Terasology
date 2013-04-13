package org.terasology.input.jitter;

import com.leapmotion.leap.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * This system listens to the JitterListener wrapper around the Leap then buffers the data for consumption.
 */
public class BufferedJitterSystem implements JitterListener {

    // RECOGNIZED:
    // If START state then simply add the gesture to the buffer (we know it is brand new)
    // If UPDATE then check the ID against the consumed list, if not found then replace existing gesture
        // Even if concurrent processing of the SAME id is active, next pass will have item with correct consumed state
    // If STOP then check the consumed list, if there then remove from consumed list (should already be out of buffer)
        // If NOT consumed then add it - processing will remove it next pass (consumed never gets set)

    // PROCESSED:
    // Check EACH in buffer against threshold (progress, consumed, etc) - if not then ignore and continue
    // If passed then return the object for processing and mark it consumed if that's enabled
        // If processing a START or UPDATE item then take no additional action
        // If processing a STOP item then remove the object from the buffer

    /** Buffer for CircleGestures with their IDs as keys */
    private ConcurrentSkipListMap<Integer, CircleGesture> circleGestures = new ConcurrentSkipListMap<Integer, CircleGesture>();

    /** List of consumed IDs for circleGestures (gestures that have been marked as "spent") */
    private ConcurrentSkipListSet<Integer> consumedCircles = new ConcurrentSkipListSet<Integer>();

    public void circleGestureRecognized(CircleGesture gesture) {
        if (gesture.state() == Gesture.State.STATE_STOP) {
            // A gesture in STOP state may have previously existed, we effectively treat it as an update
            // The processing method will be responsible for removing it from the buffer later
            System.out.println("Circle stopped, adding/updating gesture in buffer one last time");

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
            System.out.println("Circle started, adding gesture to buffer");

            // A gesture in START state has never been seen before, so we just add it to the buffer blindly
            circleGestures.put(gesture.id(), gesture);

        } else if (gesture.state() == Gesture.State.STATE_UPDATE) {
            System.out.println("Circle updated, updating it in the buffer (unless previously consumed)");

            // For updates we check if the gesture has been consumed previously in which case we won't re-add it
            // If consumption isn't enabled this if will always pass since the consumedCircles will be empty
            if (!consumedCircles.contains(gesture.id())) {
                circleGestures.put(gesture.id(), gesture);
            }

        }
    }

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

    //TODO: Move up, make user set it somewhere
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

            // Would test against constraints here and add only if the gesture passes muster
            circleBatch.add(circleGesture);

            // If gestures of this type are considered consumed when returned for processing then flag & remove
            if (consumptionEnabled) {
                consumedCircles.add(circleGesture.id());    // Mark circle as consumed so it won't get re-added
                circleGestures.remove(circleGesture.id());  // Remove it from the buffer so it won't be tested again
            } // Else then the gesture stays in the buffer till the STOP state if below removes it

            // If we've gotten to a STOP state then remove as we won't see it again anyway
            // With constraints enabled this may mean the gesture has never been processed
            if (circleGesture.state() == Gesture.State.STATE_STOP) {
                circleGestures.remove(circleGesture.id());

                // Additionally clear out the consumption status if enabled - even if we *just* consumed the gesture ;-)
                if (consumptionEnabled) {
                    consumedCircles.remove(circleGesture.id());
                }
            }
        }

        return circleBatch;
    }

    /**
     * Returns the next CircleGesture in buffer that has made progress to at least the supplied parameter
     * @param progress a float describing the number of circles (fractional or not) the gesture has completed
     * @return a CircleGesture matching the request or null if none are available
     */
    public Set<CircleGesture> nextCircleBatch(float progress) {
        Set<CircleGesture> circleBatch = new HashSet<CircleGesture>();

        for (CircleGesture circleGesture : circleGestures.values()) {

            // Would test against constraints here and add only if the gesture passes muster
            if (circleGesture.progress() > progress) {
                System.out.println("Circle gesture has progressed sufficiently, allowing it to be processed");
                circleBatch.add(circleGesture);

                // If gestures of this type are considered consumed when returned for processing then flag & remove
                if (consumptionEnabled) {
                    consumedCircles.add(circleGesture.id());    // Mark circle as consumed so it won't get re-added
                    circleGestures.remove(circleGesture.id());  // Remove it from the buffer so it won't be tested again
                } // Else then the gesture stays in the buffer till the STOP state if below removes it

            } else {
                System.out.println("Circle gesture hasn't progressed enough to be considered yet");
            }

            // If we've gotten to a STOP state then remove as we won't see it again anyway
            // With constraints enabled this may mean the gesture has never been processed
            if (circleGesture.state() == Gesture.State.STATE_STOP) {
                System.out.println("Why does execution disregard the above if and instead jumps straight to inside the next if?");
                circleGestures.remove(circleGesture.id());

                // Additionally clear out the consumption status if enabled - even if we *just* consumed the gesture ;-)
                if (consumptionEnabled) {
                    consumedCircles.remove(circleGesture.id());
                }
            }
        }

        return circleBatch;
    }

    /**
     * Returns the next CircleGesture in buffer that has made progress to at least the supplied parameter
     * @param progress a float describing the number of circles (fractional or not) the gesture has completed
     * @return a CircleGesture matching the request or null if none are available
     */
    public Set<CircleGesture> nextCircleBatch(float progress, float radius) {

        return null;
    }
}

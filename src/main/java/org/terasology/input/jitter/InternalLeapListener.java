//TODO: Add license header when properly externalized as a library - already discussed with Marcel

package org.terasology.input.jitter;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Gesture.State;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.SwipeGesture;

/**
 * InternalLeapListener is a wrapped Leap Listener used by Jitter. It captures and stores Leap data in the JitterSystem.
 * Gestures are detected locally here and callbacks issued to the JitterListener supplied on instantiation
 *
 * Based on LeapMotionListener.java by Marcel Schwittlick for LeapMotionP5 - https://github.com/mrzl/LeapMotionP5
 *
 * @author Marcel Schwittlick
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
class InternalLeapListener extends Listener {
    protected int maxFramesToRecord = 1000;
    private JitterSystem jitterSystem;
    private JitterListener externalListener;

    /**
     * Instantiates the internal listener.
     * @param jitterSystem the JitterSystem to store data to
     * @param externalListener the JitterListener to send gesture callbacks to
     */
    public InternalLeapListener(JitterSystem jitterSystem, JitterListener externalListener) {
        this.jitterSystem = jitterSystem;
        this.externalListener = externalListener;
        jitterSystem.currentFrame = new Frame();
        jitterSystem.lastFrames = new LinkedList<Frame>();
        jitterSystem.lastFramesInclProperTimestamps = new ConcurrentSkipListMap<Date, Frame>();
        jitterSystem.oldFrames = new CopyOnWriteArrayList<Frame>();
        jitterSystem.oldControllers = new LinkedList<Controller>();
    }

    public void onInit(Controller controller) {
        System.out.println("Leap Motion Initialized");
    }

    public void onConnect(Controller controller) {
        System.out.println("Leap Motion Connected");
    }

    public void onDisconnect(Controller controller) {
        System.out.println("Leap Motion Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Leap Motion Exited");
    }

    /**
     * This method accepts input from the Leap software running at around 100-120 FPS ("Leap FPS").
     * Data is captured in the parent JitterSystem and gestures sent to the JitterListener.
     * @param controller the Leap controller supplied on initialization.
     */
    public void onFrame(Controller controller) {
        Frame frame = controller.frame();
        jitterSystem.currentFrame = frame;

        processGestures(controller);

        // adding frames the list. making sure that only the newest frames are saved in order
        if (jitterSystem.lastFrames.size() >= maxFramesToRecord) {
            jitterSystem.lastFrames.removeFirst();
        }
        jitterSystem.lastFrames.add(frame);

        // adding frames to the list. adding a proper timestamp to each frame object
        if (jitterSystem.lastFramesInclProperTimestamps.size() >= maxFramesToRecord) {
            jitterSystem.lastFramesInclProperTimestamps.remove(jitterSystem.lastFramesInclProperTimestamps.firstKey());
        }
        jitterSystem.lastFramesInclProperTimestamps.put(new Date(), frame);

        // adding old frames to different object
        if (jitterSystem.oldFrames.size() >= maxFramesToRecord) {
            jitterSystem.oldFrames.remove(0);
        }
        jitterSystem.oldFrames.add(frame);

        if (jitterSystem.oldControllers.size() >= maxFramesToRecord) {
            jitterSystem.oldControllers.removeLast();
        }
        jitterSystem.oldControllers.add(controller);
    }

    // Go through all gestures detected and invoke a callback for each
    private void processGestures(Controller controller) {
        GestureList list = controller.frame().gestures();
        //TODO: Hmm, this if isn't actually needed, is it? list.count() == 0 will skip the loop anyway
        if (!list.empty()) {
            for (int i = 0; i < list.count(); i++) {
                Gesture gesture = list.get(i);
                invokeCallback(gesture);
                printGestureDetails(gesture, controller);
            }
        }
    }

    // Send individual gestures straight to the external JitterListener
    private void invokeCallback(Gesture gesture) {
        switch (gesture.type()) {
            case TYPE_CIRCLE:
                CircleGesture circleGesture = new CircleGesture(gesture);

                try {
                    externalListener.circleGestureRecognized(circleGesture);
                } catch (Exception e) {
                    System.out.println(e.getMessage() + " CALLBACK ERROR");
                }
                break;
            case TYPE_SWIPE:
                SwipeGesture swipeGesture = new SwipeGesture(gesture);
                try {
                    externalListener.swipeGestureRecognized(swipeGesture);
                } catch (Exception e) {
                    System.out.println(e.getMessage() + " CALLBACK ERROR");
                }
                break;
            case TYPE_SCREEN_TAP:
                ScreenTapGesture screenTapGesture = new ScreenTapGesture(gesture);
                try {
                    externalListener.screenTapGestureRecognized(screenTapGesture);
                } catch (Exception e) {
                    System.out.println(e.getMessage() + " CALLBACK ERROR");
                }
                break;
            case TYPE_KEY_TAP:
                KeyTapGesture keyTapGesture = new KeyTapGesture(gesture);
                try {
                    externalListener.keyTapGestureRecognized(keyTapGesture);
                } catch (Exception e) {
                    System.out.println(e.getMessage() + " CALLBACK ERROR");
                }
                break;
            default:
                break;
        }
    }

    private void printGestureDetails(Gesture gesture, Controller controller) {
        switch (gesture.type()) {
            case TYPE_CIRCLE:
                CircleGesture circle = new CircleGesture(gesture);

                // Calculate clock direction using the angle between circle normal and pointable
                String clockwise;
                if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI / 4) {
                    // Clockwise if angle is less than 90 degrees
                    clockwise = "clockwise";
                } else {
                    clockwise = "counterclockwise";
                }

                // Calculate angle swept since last frame
                double sweptAngle = 0;
                if (circle.state() != State.STATE_START) {
                    CircleGesture previousUpdate =
                            new CircleGesture(controller.frame(1).gesture(circle.id()));
                    sweptAngle = (circle.progress() - previousUpdate.progress()) * 2 * Math.PI;
                }

                System.out.println("Circle id: " + circle.id() + ", " + circle.state() + ", progress: "
                        + circle.progress() + ", radius: " + circle.radius() + ", angle: "
                        + Math.toDegrees(sweptAngle) + ", " + clockwise);
                break;
            case TYPE_SWIPE:
                SwipeGesture swipe = new SwipeGesture(gesture);
                System.out.println("Swipe id: " + swipe.id() + ", " + swipe.state() + ", position: "
                        + swipe.position() + ", direction: " + swipe.direction() + ", speed: " + swipe.speed());
                break;
            case TYPE_SCREEN_TAP:
                ScreenTapGesture screenTap = new ScreenTapGesture(gesture);
                System.out.println("Screen Tap id: " + screenTap.id() + ", " + screenTap.state()
                        + ", position: " + screenTap.position() + ", direction: " + screenTap.direction());
                break;
            case TYPE_KEY_TAP:
                KeyTapGesture keyTap = new KeyTapGesture(gesture);
                System.out.println("Key Tap id: " + keyTap.id() + ", " + keyTap.state() + ", position: "
                        + keyTap.position() + ", direction: " + keyTap.direction());
                break;
            default:
                System.out.println("Unknown gesture type.");
                break;
        }
    }
}

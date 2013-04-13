package org.terasology.input.jitter;

/**
 * JitterSystem
 *
 * JitterSystem library for Processing. Copyright (c) 2012-2013 held jointly by the individual
 * authors.
 *
 * JitterSystem library for Processing is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * JitterSystem for Processing is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with JitterSystem library
 * for Processing. If not, see http://www.gnu.org/licenses/.
 *
 * Leap Developer SDK. Copyright (C) 2012-2013 Leap Motion, Inc. All rights reserved.
 *
 * NOTICE: This developer release of Leap Motion, Inc. software is confidential and intended for
 * very limited distribution. Parties using this software must accept the SDK Agreement prior to
 * obtaining this software and related tools. This software is subject to copyright.
 */

import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.Type;
import com.leapmotion.leap.Vector;

import javax.vecmath.Vector3f;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JitterSystem.java
 *
 * @author Marcel Schwittlick
 */
public class JitterSystem {
    private static final float LEAP_WIDTH = 200.0f; // in mm
    private static final float LEAP_HEIGHT = 500.0f; // in mm
    private static final float LEAP_DEPTH = 200.0f; // in mm
    protected Frame currentFrame;
    protected LinkedList<Frame> lastFrames;
    protected CopyOnWriteArrayList<Frame> oldFrames;
    protected LinkedList<Controller> oldControllers;
    protected ConcurrentSkipListMap<Date, Frame> lastFramesInclProperTimestamps;
    protected HashMap<Integer, Finger> lastDetectedFinger;
    protected HashMap<Integer, Pointable> lastDetectedPointable;
    protected HashMap<Integer, Hand> lastDetectedHand;
    protected HashMap<Integer, Tool> lastDetectedTool;
    private InternalLeapListener listener;
    private JitterListener externalListener;
    private Controller controller;
    private String sdkVersion = "0.7.7";
    private int activeScreenNr = 0;
    private Finger velocityOffsetTestFinger;

    /**
     * This class gives you some high level access to the data tracked and recorded by the leap.
     * It gives you a different way of access than the original leap sdk
     * @param externalListener the JitterListener to send callbacks to
     */
    public JitterSystem(JitterListener externalListener) {

        //TODO: Use passed in screen dimensions instead of expecting we're running in a Processing Applet?
        //this.p = p;

        listener = new InternalLeapListener(this, externalListener);
        controller = new Controller();

        controller.addListener(listener);

        lastDetectedFinger = new HashMap<Integer, Finger>();
        lastDetectedPointable = new HashMap<Integer, Pointable>();
        lastDetectedHand = new HashMap<Integer, Hand>();
        lastDetectedTool = new HashMap<Integer, Tool>();

        lastDetectedFinger.put(0, new Finger());
        lastDetectedPointable.put(0, new Pointable());
        lastDetectedHand.put(0, new Hand());
        lastDetectedTool.put(0, new Tool());

        // this is necessary because the velocity of all objects has an offset.
        velocityOffsetTestFinger = new Finger();
    }

    /**
     * Stop simply removes the listener
     */
    public void stop() {
        controller.removeListener(listener);
    }

    /**
     * @return the SDK version.
     */
    public String getSDKVersion() {
        return sdkVersion;
    }

    /**
     * @param max new value to set for maxFramesToRecord.
     */
    public void setMaxFramesToRecord(int max) {
        listener.maxFramesToRecord = max;
    }

    /**
     * this prints out the current offset of the vectors from the sdk. this is just for information
     * and will give you the position, velocity and acceleration offsets
     */
    public void printCorrectionOffset() {
        System.out.println("pos offset: " + getTip(velocityOffsetTestFinger));
        System.out.println("velocity offset: " + getVelocity(velocityOffsetTestFinger));
        System.out.println("acc offset: " + getAcceleration(velocityOffsetTestFinger));
    }

    /**
     * this returns a Vector3f containing the velocity offset. the problems the velocity offset has
     * with it, is that the velocity is slightly shifted. for example if you shouldn't have any
     * velocity, because the finger is standing still its returning a velocity that is initialized
     * with a new Finger object. this should be fixed with the upcoming sdk releases (i hope) *
     *
     * @return Vector3f containing the velocity offset
     */
    public Vector3f velocityOffset() {
        return vectorToVector3f(velocityOffsetTestFinger.tipVelocity());
    }

    public Vector3f positionOffset() {
        return vectorToVector3f(velocityOffsetTestFinger.tipPosition());
    }

    /**
     * this returns an Vector3f containing the acceleration offset, that has to be subtracted from
     * every vector returned from the library. unfortunately the leap sdk has a little bug there.
     *
     * @return Vector3f containing the acceleration offset
     */
    public Vector3f accelerationOffset() {
        return getAcceleration(velocityOffsetTestFinger);
    }

    public void enableGesture(Type gestureName) {
        controller.enableGesture(gestureName);
    }

    public void disableGesture(Type gesture) {
        controller.enableGesture(gesture, false);
    }

    public boolean isEnabled(Type gesture) {
        return controller.isGestureEnabled(gesture);
    }

    /**
     * returns the controller of the leap sdk
     *
     * @return Controller controller
     */
    public Controller getController() {
        try {
            return controller;
        } catch (Exception e) {
            System.err.println("Can not return controller not initialized. Returning new Controller object");
            System.out.println(e);
            return new Controller();
        }
    }

    /**
     * returns the most current frame from the leap sdk. a frame contains every tracked data from the
     * leap about your fingers.
     *
     * @return Frame the leap frame
     */
    public Frame getFrame() {
        try {
            return currentFrame;
        } catch (Exception e) {
            System.err.println("Can not return current frame. Returning new Frame object instead");
            System.err.println(e);
            return new Frame();
        }
    }

    /**
     * returns a frame by id.
     *
     * @param id the id of the frame you want
     * @return Frame the frame which id you passed as a parameter. if the frame with the id you asked
     *         for is not currently saved anymore you'll get a new Frame object.
     */
    public Frame getFrame(int id) {
        Frame returnFrame = new Frame();
        for (Frame frame : getFrames()) {
            if (frame.id() >= id) {
                returnFrame = frame;
            }
        }
        return returnFrame;
    }

    /**
     * returns the frame before the most current frame.
     *
     * @return the previous frame
     */
    public Frame getLastFrame() {
        return getFrames().get(getFrames().size() - 2);
    }

    public Controller getLastController() {
        return getLastControllers().get(getLastControllers().size() - 40);
    }

    /**
     * returns the frame that was before the frame you passed.
     *
     * @param frame the frame previous to the one supplied
     * @return the frame that was recorded right before the frame you passed.
     */
    public Frame getFrameBeforeFrame(Frame frame) {
        Frame frameBefore = null;

        for (Frame of : getFrames()) {
            if (of.id() == frame.id() - 1) {
                frameBefore = of;
            }
        }

        return frameBefore;
    }

    /**
     * Returns a CopyOnWriteArrayList<Frame> containing all recently buffered frames.
     *
     * @return a CopyOnWriteArrayList containing the newest elements
     */
    public CopyOnWriteArrayList<Frame> getFrames() {
        try {
            return oldFrames;
        } catch (Exception e) {
            System.err.println("Can not return list of last frames. Returning empty list instead.");
            System.err.println(e);
            return new CopyOnWriteArrayList<Frame>();
        }
    }

    public LinkedList<Controller> getLastControllers() {
        return oldControllers;
    }

    /**
     * Returns a LinkedList containing the last buffered frame.
     *
     * @param frameCount the number of last frames
     * @return a list containing all last frames
     */
    public LinkedList<Frame> getFrames(int frameCount) {
        LinkedList<Frame> frames = new LinkedList<Frame>();
        for (int i = 0; i < frameCount; i++) {
            if (getFrames().size() > frameCount) {
                Frame fr = getFrames().get(getFrames().size() - frameCount + i);
                frames.add(fr);
            }
        }
        return frames;
    }

    /**
     * @return a Vector3f containing the position of the XY plane for fingers
     */
    public Vector3f getFingerPositionXYPlane() {
        Vector3f fingerPositionXYPlane = new Vector3f();

        Frame frame = getFrame();
        if (!frame.hands().empty()) {
            Hand hand = frame.hands().get(0);
            if (!hand.fingers().empty()) {
                Finger finger = hand.fingers().get(0);
                fingerPositionXYPlane.x = transformLeapToScreenX(finger.tipPosition().getX());
                fingerPositionXYPlane.y = transformLeapToScreenY(finger.tipPosition().getY());
            }
        }

        return fingerPositionXYPlane;
    }

    /**
     * converts the x coordinate from the leap space into the processing window space
     *
     * @param x leap-space
     * @return processing-window space (zero until rewritten)
     */
    public float transformLeapToScreenX(float x) {
    /*
     * int startX = -243; int endX = 256; float valueMapped = PApplet.map(x, startX, endX, 0,
     * p.width); return valueMapped;
     */

        //TODO: Commented out for Jitter - rewrite using passed screen dimensions instead?
        /*
        float c = p.width / 2.0f;
        if (x > 0.0) {
            return PApplet.lerp(c, p.width, x / LEAP_WIDTH);
        } else {
            return PApplet.lerp(c, 0.0f, -x / LEAP_WIDTH);
        }
        */
        return 0;
    }

    /**
     * converts the y coordinate from the leap space into the processing window space
     *
     * @param y leap space
     * @return processing-window space (zero until rewritten)
     */
    public float transformLeapToScreenY(float y) {
    /*
     * int startY = 50; int endY = 350; float valueMapped = PApplet.map(y, startY, endY, 0,
     * p.height); return valueMapped;
     */
        //TODO: Commented out for Jitter - rewrite using passed screen dimensions instead?

        //return PApplet.lerp(p.height, 0.0f, y / LEAP_HEIGHT);

        return 0;
    }

    /**
     * converts the z coordinate from the leap space into the processing window space
     *
     * @param z leap space
     * @return processing window space
     */
    public float transformLeapToScreenZ(float z) {
    /*
     * int startZ = -51; int endZ = 149; float valueMapped = PApplet.map(z, startZ, endZ, 0,
     * p.width); return valueMapped;
     */

        //TODO: Commented out for Jitter - rewrite using passed screen dimensions instead?

        //return PApplet.lerp(0, p.width, z / LEAP_DEPTH);

        return 0;
    }

    /**
     * converts a vector from the leap space into the processing window space
     *
     * @param vector from the leap sdk containing a position in the leap space
     * @return the vector in Vector3f data type containing the same position in processing window space
     */
    public Vector3f vectorToVector3f(Vector vector) {
        return convertLeapToScreenDimension(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * converts x, y and z coordinates of the leap to the dimensions of your sketch
     *
     * @param x x position in leap world coordinate system
     * @param y y position in leap world coordinate system
     * @param z z position in leap world coordinate system
     * @return Vector3f the vector of the point you passed in converted to the dimensions of your window
     */
    public Vector3f convertLeapToScreenDimension(float x, float y, float z) {
        Vector3f positionRelativeToFrame = new Vector3f();
        positionRelativeToFrame.x = transformLeapToScreenX(x);
        positionRelativeToFrame.y = transformLeapToScreenY(y);
        positionRelativeToFrame.z = transformLeapToScreenZ(z);
        return positionRelativeToFrame;
    }

    /**
     * returns an arraylist containing all currently tracked hands
     *
     * @return ArrayList<Hand> an arraylist containing all currently tracked hands
     */
    public ArrayList<Hand> getHandList() {
        ArrayList<Hand> hands = new ArrayList<Hand>();
        Frame frame = getFrame();
        if (!frame.hands().empty()) {
            for (Hand hand : frame.hands()) {
                hands.add(hand);
            }
        }
        return hands;
    }

    /**
     * returns all hands tracked in the frame you passed
     *
     * @param frame the frame from which to find out all tracked hands
     * @return arraylist containing all hands from the passed frame
     */
    public ArrayList<Hand> getHandList(Frame frame) {
        ArrayList<Hand> hands = new ArrayList<Hand>();
        try {
            if (!frame.hands().empty()) {
                for (Hand hand : frame.hands()) {
                    if (hand.isValid()) {
                        hands.add(hand);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("This exception goes 'moo' (and is ignored)");
        }
        return hands;
    }

    /**
     * gets a hand by number. the number is indicated by the order the hand appeared in the leap. so
     * the first hand tracked has the nr 0 and the second one the number 1. once one hand of them two
     * leaves the leap the one hand left has the nr 0. this is implemented like that because the leap
     * is loosing track of the id's of hand to easily.
     *
     * @param handNr nr of the hand
     * @return the hand matching the given number
     */
    public Hand getHand(int handNr) {
        Hand returnHand = null;
        if (!getHandList().isEmpty()) {
            lastDetectedHand.put(handNr, getHandList().get(handNr));
        }
        // returnHand = lastDetectedHand.get(handNr);
        int downCounter = 0;
        while (returnHand == null) {
            returnHand = lastDetectedHand.get(handNr - downCounter);
            downCounter++;
        }
        return returnHand;
    }

    /**
     * returns a hand by id in the frame you passed.
     *
     * @param id id of the hand you want
     * @param frame the frame the hand should be in
     * @return the resulting hand or null if not found
     */
    public Hand getHandById(int id, Frame frame) {
        Hand returnHand = null;
        for (Hand hand : getHandList(frame)) {
            if (hand.id() == id) {
                returnHand = hand;
            }
        }
        return returnHand;
    }

    /**
     * @return the scale factor for the current frame
     */
    public float getScaleFactor() {
        return getFrame().scaleFactor(getLastFrame());
    }

    /**
     * @return the scale factor for the given frame
     */
    public float getScaleFactor(Frame frame) {
        return getFrame().scaleFactor(frame);
    }

    /**
     * returns averaged translation of all points tracked by the leap in comparison to the last frame
     *
     * @return
     */
    public Vector3f getTranslation() {
        Vector3f translation = vectorToVector3f(getFrame().translation(getLastFrame()));
        translation.sub(velocityOffset());
        return translation;
    }

    /**
     * returns averaged translation of all points tracked by the leap in comparison to the frame you
     * passed in the method
     *
     * @return
     */
    public Vector3f getTranslation(Frame frame) {
        Vector3f translation = vectorToVector3f(getFrame().translation(frame));
        translation.sub(velocityOffset());
        return translation;
    }

    /**
     * returns the pitch of the hand you passed
     *
     * @param hand the hand you want the pitch of
     * @return a float value containing the pitch of the hand
     */
    public float getPitch(Hand hand) {
        // return PApplet.map((float) Math.toDegrees(hand.direction().pitch()), 0, 22, 0,
        // PConstants.TWO_PI);
        return (float) Math.toDegrees(hand.direction().pitch());
    }

    /**
     * returns the roll of the hand you passed
     *
     * @param hand the hand you want the roll of
     * @return a float value containing the roll of the hand
     */
    public float getRoll(Hand hand) {
        // return -PApplet.map((float) Math.toDegrees(hand.direction().roll()), 0, 180, 0,
        // PConstants.TWO_PI);
        return (float) Math.toDegrees(hand.palmNormal().roll());
    }

    /**
     * returns the yaw of the hand you passed
     *
     * @param hand the hand you want the yaw of
     * @return a float value containing the yaw of the hand
     */
    public float getYaw(Hand hand) {
        return (float) Math.toDegrees(hand.direction().yaw());
    }

    /**
     * returns a Vector3f containing the direction of the hand
     *
     * @param hand the hand you want the direction of
     * @return Vector3f direction of the hand
     */
    public Vector3f getDirection(Hand hand) {

        Vector3f dir = vectorToVector3f(hand.direction());
        dir.sub(positionOffset());
        return dir;
    }

    /**
     * returns a Vector3f containing the position of the hand
     *
     * @param hand the hand you want the position of
     * @return Vector3f position of the hand
     */
    public Vector3f getPosition(Hand hand) {
        return vectorToVector3f(hand.palmPosition());
    }

    /**
     * returns the normal of the palm of the hand
     *
     * @param hand the hand you want the normal of the palm of
     * @return a Vector3f containing the normal of the palm of the hand
     */
    public Vector3f getNormal(Hand hand) {
        Vector3f normal = vectorToVector3f(hand.palmNormal());
        normal.sub(positionOffset());
        return normal;
    }

    /**
     * returns the velocity of the palm of the hand you passed in
     *
     * @param hand the hand of which palm you want the velocity of
     * @return a Vector3f containing the velocity of the hand
     */
    public Vector3f getVelocity(Hand hand) {
        Vector3f velo = vectorToVector3f(hand.palmVelocity());
        velo.sub(velocityOffset());
        return velo;
    }

    /**
     * access to the acceleration of the hand you passed in.
     *
     * @param hand the hand you want the acceleration of
     * @return a Vector3f containing the acceleration of the hand you passed in
     */
    public Vector3f getAcceleration(Hand hand) {
        Vector3f acceleration = null;

        Frame currentFrame = getFrame();
        Frame lastFrame = getFrameBeforeFrame(currentFrame);
        Vector3f currentVelo = new Vector3f();
        Vector3f lastVelo = new Vector3f();
        try {
            currentVelo = getVelocity(hand);
            lastVelo = getVelocity(getHandById(hand.id(), lastFrame));
        } catch (Exception e) {
            System.out.println("Ignoring exception thrown trying to get velocity of a given hand");
        }
        currentVelo.sub(lastVelo);
        //TODO: Vector division is available in Processing but not straight up in Java's Vector3f. Okay replacement?
        currentVelo = new Vector3f(currentVelo.x / 2, currentVelo.y / 2, currentVelo.z / 2);
        acceleration = currentVelo;

        return acceleration;
    }

    /**
     * @param hand hand to get the sphere center for
     * @return a Vector3f containing the center position
     */
    public Vector3f getSphereCenter(Hand hand) {
        return vectorToVector3f(hand.sphereCenter());
    }

    /**
     * @param hand hand to get the sphere radius for
     * @return the sphere radius as a float
     */
    public float getSphereRadius(Hand hand) {
        return hand.sphereRadius();
    }

    /**
     * access to all fingers that are currently tracked
     *
     * @return ArrayList<Finger> an ArrayList containing all currently tracked fingers
     */
    public ArrayList<Finger> getFingerList() {
        ArrayList<Finger> fingers = new ArrayList<Finger>();

        Frame frame = getFrame();
        if (!frame.hands().empty()) {
            for (Hand hand : frame.hands()) {
                fingers.addAll(getFingerList(hand));
            }
        }

        return fingers;
    }

    /**
     * access to all tracked fingers in the frame you passed in
     *
     * @param frame the frame you want all tracked fingers of
     * @return an ArrayList containing all tracked fingers
     */
    public ArrayList<Finger> getFingerList(Frame frame) {
        ArrayList<Finger> fingers = new ArrayList<Finger>();

        if (!frame.hands().empty()) {
            for (Hand hand : frame.hands()) {
                fingers.addAll(getFingerList(hand));
            }
        }

        return fingers;
    }

    /**
     * Get the list of all fingers of the hand passed in.
     *
     * @param hand the hand you want all tracked fingers of
     * @return an ArrayList containing all tracked fingers of the hand
     */
    public ArrayList<Finger> getFingerList(Hand hand) {
        ArrayList<Finger> fingers = new ArrayList<Finger>();

        for (Finger finger : hand.fingers()) {
            fingers.add(finger);
        }
        return fingers;
    }

    /**
     * returns the finger by number. the fingers are numbered by the occurrence in the leap.
     *
     * @param fingerNr number of the finger we want
     * @return the right finger or null if not found
     */
    public Finger getFinger(int fingerNr) {
        Finger returnFinger = null;
        if (getFingerList().size() > fingerNr) {
            lastDetectedFinger.put(fingerNr, getFingerList().get(fingerNr));
        }
        // returnFinger = lastDetectedFinger.get(fingerNr);
        int downCounter = 0;
        while (returnFinger == null) {
            returnFinger = lastDetectedFinger.get(fingerNr - downCounter);
            downCounter++;
        }
        return returnFinger;
    }

    /**
     * @param id id of the desired finger
     * @param frame the frame to look in
     * @return the finger or null of not found
     */
    public Finger getFingerById(int id, Frame frame) {
        Finger returnFinger = null;
        for (Finger finger : getFingerList(frame)) {
            if (finger.id() == id) {
                returnFinger = finger;
            }
        }
        return returnFinger;
    }

    /**
     * returns the tip position of the passed pointable
     *
     * @param pointable the pointable you want the tip position of
     * @return a Vector3f containing the position of the tip of the pointable
     */
    public Vector3f getTip(Pointable pointable) {
        return convertLeapToScreenDimension(pointable.tipPosition().getX(), pointable.tipPosition()
                .getY(), pointable.tipPosition().getZ());
    }

    /**
     * sets the current screen for getting the calibrated points. I should rewrite this, but nobody
     * is gonna read it anyway. arr.
     * Somebody else totally read it. Arr.
     * @param screenNr number to set the active screen to
     */
    public void setActiveScreen(int screenNr) {
        this.activeScreenNr = screenNr;
    }

    /**
     * to use this utility you have to have the leap calibrated to your screen.
     *
     * @param pointable the finger you want the intersection with your screen from
     * @return null until rewritten
     */
    public Vector3f getTipOnScreen(Pointable pointable) {
        Vector3f pos;

        ScreenList sl = controller.calibratedScreens();
        com.leapmotion.leap.Screen calibratedScreen = sl.get(activeScreenNr);
        Vector loc = calibratedScreen.intersect(pointable, true);

        //TODO: Processing specific code commented out for Jitter
        /*
        float x = PApplet.map(loc.getX(), 0, 1, 0, p.displayWidth);

        x -= p.getLocationOnScreen().x;
        float y = PApplet.map(loc.getY(), 0, 1, p.displayHeight, 0);
        y -= p.getLocationOnScreen().y;

        pos = new Vector3f(x, y, 0f);

        return pos;

        */

        return null;
    }

    /**
     * returns the velocity of a finger on the screen
     *
     * @param pointable the pointable to get velocity for
     * @return null until rewritten
     */

    public Vector3f getVelocityOnScreen(Pointable pointable) {
        Vector loc = new Vector();
        Vector oldLoc = new Vector();
        try {
            oldLoc = getLastController().calibratedScreens().get(activeScreenNr)
                            .intersect(getPointableById(pointable.id(), getLastFrame()), true);
            loc = controller.calibratedScreens().get(activeScreenNr).intersect(pointable, true);
        } catch (NullPointerException e) {
            // dirty dirty hack to keep the program running. i like it.
            System.out.println("Terribly unholy things are happening");
        }

        //TODO: Commented out for Jitter, needs rewrite - maybe initialize the system with screen dimensions?
        /*
        float x = PApplet.map(loc.getX(), 0, 1, 0, p.displayWidth);
        x -= p.getLocationOnScreen().x;
        float y = PApplet.map(loc.getY(), 0, 1, p.displayHeight, 0);
        y -= p.getLocationOnScreen().y;

        float x2 = PApplet.map(oldLoc.getX(), 0, 1, 0, p.displayWidth);
        x2 -= p.getLocationOnScreen().x;
        float y2 = PApplet.map(oldLoc.getY(), 0, 1, p.displayHeight, 0);
        y2 -= p.getLocationOnScreen().y;

        return new Vector3f(x - x2, y - y2, 0f);
        */

        return null;
    }

    /**
     * returns the origin of the pointable. the origin is the place where the pointable leaves the
     * body of the hand.
     *
     * @param pointable the pointable you want the origin of
     * @return a Vector3f containing the position of the origin of the passed pointable
     */
    public Vector3f getOrigin(Pointable pointable) {
        Vector anklePos;

        float length = pointable.length();
        Vector3f direction = new Vector3f();
        direction.x = pointable.direction().getX();
        direction.y = pointable.direction().getY();
        direction.z = pointable.direction().getZ();
        //TODO: Java's Vector3f doesn't have the fancy vector math of Processing. Acceptable replacement for multiply?
        direction = new Vector3f(direction.x * length, direction.y * length, direction.z * length);
        anklePos = new Vector(pointable.tipPosition().getX() - direction.x, pointable.tipPosition().getY()
                        - direction.y, pointable.tipPosition().getZ() - direction.z);

        return vectorToVector3f(anklePos);
    }

    /**
     * Returns the velocity of the pointable.
     *
     * @param pointable the pointable you want the velocity of
     * @return a Vector3f containing the velocity of the tip of the pointable
     */
    public Vector3f getVelocity(Pointable pointable) {
        Vector3f velocity = vectorToVector3f(pointable.tipVelocity());
        velocity.sub(velocityOffset());
        return velocity;
    }

    /**
     * Calculates the direction of the passed pointable.
     *
     * @param pointable the pointable you want the direction of
     * @return a Vector3f containing the direction of the pointable
     */
    public Vector3f getDirection(Pointable pointable) {
        return vectorToVector3f(pointable.direction());
    }

    /**
     * Returns the length of a pointable.
     *
     * @param pointable to return length for
     * @return the length of the pointable as a float
     */
    public float getLength(Pointable pointable) {
        return pointable.length();
    }

    /**
     * Returns the width of a pointable.
     *
     * @param pointable to return width for
     * @return the width of the pointable as a float
     */
    public float getWidth(Pointable pointable) {
        return pointable.width();
    }

    /**
     * Calculates the acceleration of the pointable according to the velocity of the current and the
     * last frame.
     *
     * @param pointable the pointable you want the acceleration of
     * @return a Vector3f containing the acceleration of the tip of the passed pointable
     */
    public Vector3f getAcceleration(Pointable pointable) {
        Frame currentFrame = getFrame();
        Frame lastFrame = getFrameBeforeFrame(currentFrame);
        Vector3f currentVelocity = new Vector3f();
        Vector3f lastVelocity = new Vector3f();
        try {
            currentVelocity = getVelocity(pointable);
            lastVelocity = getVelocity(getPointableById(pointable.id(), lastFrame));
        } catch (Exception e) {
            System.out.println("I like cheese and to ignore exceptions");
        }
        currentVelocity.sub(lastVelocity);
        //TODO: Java lacks fancy vector math, acceptable replacement for division?
        currentVelocity = new Vector3f(currentVelocity.x / 2, currentVelocity.y / 2, currentVelocity.z / 2);
        return currentVelocity;
    }

    /**
     * returns all pointables in the current frame
     *
     * @return ArrayList<Pointable> an ArrayList containing all currently tracked pointables
     */
    public ArrayList<Pointable> getPointableList() {
        ArrayList<Pointable> pointables = new ArrayList<Pointable>();

        Frame frame = getFrame();
        if (!frame.hands().empty()) {
            for (Hand hand : frame.hands()) {
                pointables.addAll(getPointableList(hand));
            }
        }

        return pointables;
    }

    /**
     * returns all pointables of the passed frame
     *
     * @return ArrayList<Pointable> an ArrayList containing all currently tracked pointables
     */
    public ArrayList<Pointable> getPointableList(Frame frame) {
        ArrayList<Pointable> pointables = new ArrayList<Pointable>();

        if (!frame.hands().empty()) {
            for (Hand hand : frame.hands()) {
                pointables.addAll(getPointableList(hand));
            }
        }

        return pointables;
    }

    /**
     * returns all pointables of the passed hand
     *
     * @param hand the hand you want the pointables of
     * @return an arraylist containing the pointables of the passed hand
     */
    public ArrayList<Pointable> getPointableList(Hand hand) {
        ArrayList<Pointable> pointables = new ArrayList<Pointable>();

        for (Pointable pointable : hand.pointables()) {
            pointables.add(pointable);
        }

        return pointables;
    }

    /**
     * returns a pointable by its number. look up to the equivalent methods for hand/finger for
     * documentation
     *
     * @param pointableNr the number of the pointable
     * @return
     */
    public Pointable getPointable(int pointableNr) {
        Pointable returnPointable = null;
        if (!getPointableList().isEmpty()) {
            lastDetectedPointable.put(pointableNr, getPointableList().get(pointableNr));
        }
        // returnPointable = lastDetectedPointable.get(pointableNr);
        int downCounter = 0;
        while (returnPointable == null) {
            returnPointable = lastDetectedPointable.get(pointableNr - downCounter);
            downCounter++;
        }
        return returnPointable;
    }

    /**
     * Returns a pointable by id in the passed frame.
     *
     * @param id the if of the pointable
     * @param frame the frame where to look for the pointable
     * @return the pointable desired or null if not found
     */
    public Pointable getPointableById(int id, Frame frame) {
        Pointable returnPointable = null;
        for (Pointable pointable : getPointableList(frame)) {
            if (pointable.id() == id) {
                returnPointable = pointable;
            }
        }
        return returnPointable;
    }

    /**
     * Calculates an ArrayList containing all tools in the current frame.
     *
     * @return ArrayList of tools, if any
     */
    public ArrayList<Tool> getToolList() {
        ArrayList<Tool> tools = new ArrayList<Tool>();

        Frame frame = getFrame();
        if (!frame.hands().empty()) {
            for (Hand hand : frame.hands()) {
                tools.addAll(getToolList(hand));
            }
        }

        return tools;
    }

    /**
     * Calculates an ArrayList containing all tools in the passed frame.
     *
     * @return an ArrayList of tools, if any
     */
    public ArrayList<Tool> getToolList(Frame frame) {
        ArrayList<Tool> tools = new ArrayList<Tool>();

        if (!frame.hands().empty()) {
            for (Hand hand : frame.hands()) {
                tools.addAll(getToolList(hand));
            }
        }

        return tools;
    }

    /**
     * Returns a ArrayList of tools attached to the passed hand.
     *
     * @param hand the hand we want tools from
     * @return an ArrayList of tools attached to the passed hand, if any
     */
    public ArrayList<Tool> getToolList(Hand hand) {
        ArrayList<Tool> tools = new ArrayList<Tool>();

        for (Tool tool : hand.tools()) {
            tools.add(tool);
        }

        return tools;
    }

    /**
     * Returns a tool by its number.
     *
     * @param toolNr number of the desired tool
     * @return the tool matching the number, if any
     */
    public Tool getTool(int toolNr) {
        Tool returnTool = null;
        if (!getToolList().isEmpty()) {
            lastDetectedTool.put(toolNr, getToolList().get(toolNr));
        }
        // returnTool = lastDetectedTool.get(toolNr);
        int downCounter = 0;
        while (returnTool == null) {
            returnTool = lastDetectedTool.get(toolNr - downCounter);
            downCounter++;
        }
        return returnTool;
    }

    /**
     * Calculates a proper timestamp of the passed frame.
     *
     * @param frame the frame you want the timestamp of
     * @return Date containing the timestamp when the frame was taken
     */
    public Date getTimestamp(Frame frame) {
        Date date = null;

        for (Entry<Date, Frame> entry : lastFramesInclProperTimestamps.entrySet()) {
            String stringOfTimestampInMap = entry.getValue().timestamp() + "";
            String stringOfTimestampPassedParameter = frame.timestamp() + "";
            if (stringOfTimestampInMap.equals(stringOfTimestampPassedParameter)) {
                date = entry.getKey();
            }
        }
        return date;
    }

    /**
     * Simple test for whether a CircleGesture is clockwise or counter-clockwise
     * @param circleGesture the CircleGesture to test
     * @return true if clockwise, false otherwise
     */
    public static boolean isClockwise(CircleGesture circleGesture) {
        return circleGesture.pointable().direction().angleTo(circleGesture.normal()) <= Math.PI / 4;
    }
}

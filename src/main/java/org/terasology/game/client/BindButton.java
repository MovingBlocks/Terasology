package org.terasology.game.client;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.input.BindButtonEvent;
import org.terasology.logic.manager.GUIManager;

import java.util.List;

/**
 * A BindButton is pseudo button that is controlled by one or more actual inputs (whether keys, mouse buttons or the
 * mouse wheel).
 * <p/>
 * When the BindButton changes state it sends out events like an actual key or button does. It also allows direct
 * subscription via the {@link BindButtonSubscriber} interface.
 */
public class BindButton {

    private BindButtonEvent buttonEvent;
    private int pressedButtons = 0;
    private String id;
    private List<BindButtonSubscriber> subscribers = Lists.newArrayList();
    private EventMode mode = EventMode.BOTH;

    public static enum EventMode {
        PRESS(true, false),
        RELEASE(false, true),
        BOTH(true, true);

        private boolean activatedOnPress;
        private boolean activatedOnRelease;

        private EventMode(boolean activatedOnPress, boolean activatedOnRelease) {
            this.activatedOnPress = activatedOnPress;
            this.activatedOnRelease = activatedOnRelease;
        }

        public boolean isActivatedOnPress() {
            return activatedOnPress;
        }

        public boolean isActivatedOnRelease() {
            return activatedOnRelease;
        }
    }

    /**
     * Creates the button. Package-private, as should be created through the ClientController
     *
     * @param id
     * @param event
     */
    BindButton(String id, BindButtonEvent event) {
        this.id = id;
        this.buttonEvent = event;
    }

    public void setMode(EventMode mode) {
        this.mode = mode;
    }

    public EventMode getMode() {
        return mode;
    }

    public ButtonState getState() {
        return (pressedButtons > 0) ? ButtonState.DOWN : ButtonState.UP;
    }

    /**
     * Register a subscriber to this bind
     *
     * @param subscriber
     */
    public void subscribe(BindButtonSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    /**
     * Removes a subscriber from this bind
     *
     * @param subscriber
     */
    public void unsubscribe(BindButtonSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Updates this bind with the new state of a bound button. This should be done whenever a bound button changes
     * state, so that the overall state of the bind can be tracked.
     *
     * @param pressed     Is the changing
     * @param delta       The length of the current frame
     * @param target      The current camera target
     * @param keyConsumed Has the changing button's event already been consumed
     * @param guiOnly     Is the gui consuming input
     * @return Whether the button's event has been consumed
     */
    boolean updateBindState(boolean pressed, float delta, EntityRef target, EntityRef localPlayer, boolean keyConsumed, boolean guiOnly) {
        if (pressed) {
            pressedButtons++;
            if (pressedButtons == 0 && mode.isActivatedOnPress()) {
                if (guiOnly) {
                    GUIManager.getInstance().processBindButton(id, pressed);
                    keyConsumed = true;
                }
                if (!keyConsumed) {
                    keyConsumed = triggerOnPress(delta, target);
                }
                if (!keyConsumed) {
                    buttonEvent.prepare(id, ButtonState.DOWN, delta, target);
                    localPlayer.send(buttonEvent);
                    keyConsumed = buttonEvent.isConsumed();
                }
            }
        } else if (pressedButtons != 0) {
            pressedButtons--;
            if (pressedButtons == 0  && mode.isActivatedOnRelease()) {
                if (guiOnly) {
                    GUIManager.getInstance().processBindButton(id, pressed);
                    keyConsumed = true;
                }
                if (!keyConsumed) {
                    keyConsumed = triggerOnRelease(delta, target);
                }
                if (!keyConsumed) {
                    buttonEvent.prepare(id, ButtonState.UP, delta, target);
                    localPlayer.send(buttonEvent);
                    keyConsumed = buttonEvent.isConsumed();
                }
            }
        }
        return keyConsumed;
    }

    private boolean triggerOnPress(float delta, EntityRef target) {
        for (BindButtonSubscriber subscriber : subscribers) {
            if (subscriber.onPress(delta, target)) {
                return true;
            }
        }
        return false;
    }

    private boolean triggerOnRelease(float delta, EntityRef target) {
        for (BindButtonSubscriber subscriber : subscribers) {
            if (subscriber.onRelease(delta, target)) {
                return true;
            }
        }
        return false;
    }
}

package org.terasology.logic.manager;

import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * First draft for a message manager to display messages on the in-game chat. All chat message will get passed to this manager.
 * Objects can subscribe/unsubscribe to get notified if a new message was added.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class MessageManager {

    private static MessageManager instance;

    public static enum EMessageScope
    {
        /**
         * Won't get passed to others than the local player. For messages from mods or something
         */
        PRIVATE,
        /**
         * Will get passed to all other players
         */
        PUBLIC,
        /**
         * (Could be passed to a specific group, which would be nice to have :P)
         */
        GROUP
    }


    private final List<MessageSubscription> subscribers = new ArrayList<MessageSubscription>();

    //log
    private final List<Message> log = new ArrayList<Message>();
    private final int logMax = 100;

    /**
     * A chat message which is defined by its actual message and a scope.
     *
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     */
    public class Message {

        private String message;
        private EMessageScope scope;
        private Color color;
        //...

        public Message(String message, EMessageScope scope) {
            this.message = message;
            this.scope = scope;
        }

        public String getMessage() {
            return message;
        }

        public EMessageScope getScope() {
            return scope;
        }

        public Color getColor() {
            return color;
        }
    }

    /**
     * Interface to subscribe to be notified if new chat messages arrive.
     *
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     */
    public interface MessageSubscription {
        void message(Message message);
    }

    private MessageManager() {

    }

    public static MessageManager getInstance() {
        if (instance == null) {
            instance = new MessageManager();
        }

        return instance;
    }

    /**
     * Add a message to the chat.
     *
     * @param message The message.
     */
    public void addMessage(Message message) {
        if (!message.getMessage().isEmpty()) {
            log.add(0, message);

            if (log.size() > logMax) {
                log.remove(log.size() - 1);
            }

            notifySubscribers(message);
        }
    }

    /**
     * Add a message to the chat in public scope.
     *
     * @param message The chat message.
     */
    public void addMessage(String message) {
        addMessage(message, EMessageScope.PUBLIC);
    }

    /**
     * Add a message to the chat in a specific scope.
     *
     * @param message The chat message.
     * @param scope   The scope.
     */
    public void addMessage(String message, EMessageScope scope) {
        addMessage(new Message(message, scope));
    }

    private void notifySubscribers(Message message) {
        for (MessageSubscription subscriber : subscribers) {
            subscriber.message(message);
        }
    }

    public void subscribe(MessageSubscription subscription) {
        subscribers.add(subscription);
    }

    public void unsubscribe(MessageSubscription subscription) {
        subscribers.remove(subscription);
    }
}

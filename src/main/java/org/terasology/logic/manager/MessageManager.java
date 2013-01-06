package org.terasology.logic.manager;

import org.newdawn.slick.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * First draft for a message manager to display messages on the in-game chat. All chat message will get passed to this manager.
 * Objects can subscribe/unsubscribe to get notified if a new message was added.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class MessageManager implements Iterable<MessageManager.Message> {

    private static MessageManager instance;

    private final List<MessageSubscription> subscribers = new ArrayList<MessageSubscription>();

    private final List<Message> log = new ArrayList<Message>();
    private final int logMax = 100;

    @Override
    public Iterator<Message> iterator() {
        return log.iterator();
    }

    /**
     * A chat message which is defined by its actual message and a scope.
     *
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     */
    public class Message {

        private String message;
        private Color color;

        public Message(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
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
     * Add a message to the chat.
     *
     * @param message The chat message.
     */
    public void addMessage(String message) {
        addMessage(new Message(message));
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

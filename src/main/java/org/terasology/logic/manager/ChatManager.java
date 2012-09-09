package org.terasology.logic.manager;

import java.util.ArrayList;
import java.util.List;

/**
 * First draft for a chat manager. All chat message will get passed to this manager.
 * Objects can subscribe/unsubscribe to get notified if a new chat message was added.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class ChatManager {
    
    private static ChatManager instance;
    private static enum EChatScope {PRIVATE, PUBLIC, GROUP};
    private final List<ChatSubscription> subscribers = new ArrayList<ChatSubscription>();
    
    //log
    private final List<Message> log = new ArrayList<Message>();
    private final int logMax = 100;
    
    /**
     * A chat message which is defined by its actual message and a scope. The scope can be<br /><br />
     *   private - Won't get passed to others than the local player. For messages from mods or something<br />
     *   public - Will get passed to all other players<br />
     *   (group - Could be passed to a specific group, which would be nice to have :P)
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     *
     */
    public class Message {
        
        private String message;
        private EChatScope scope;
        //...

        public Message(String message, EChatScope scope) {
            this.message = message;
            this.scope = scope;
        }
        
        public String getMessage() {
            return message;
        }

        public EChatScope getScope() {
            return scope;
        }
    }
    
    /**
     * Interface to subscribe to be notified if new chat messages arrive.
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     *
     */
    public interface ChatSubscription {
        void message(String message);
    }
    
    private ChatManager() {
        
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        
        return instance;
    }
    
    /**
     * Add a message to the chat in public scope.
     * @param message
     */
    public void addMessage(String message) {
        addMessage(message, EChatScope.PUBLIC);
    }
    
    public void addMessage(String message, EChatScope scope) {
        if (!message.trim().isEmpty()) {
            log.add(0, new Message(message, scope));
            
            if (log.size() > logMax) {
                log.remove(log.size() - 1);
            }
            
            notifiySubscribers(message);
        }
    }
    
    private void notifiySubscribers(String message) {
        for (ChatSubscription subscriber : subscribers) {
            subscriber.message(message);
        }
    }
    
    public void subscribe(ChatSubscription subscription) {
        subscribers.add(subscription);
    }
    
    public void unsubscribe(ChatSubscription subscription) {
        subscribers.remove(subscription);
    }
}

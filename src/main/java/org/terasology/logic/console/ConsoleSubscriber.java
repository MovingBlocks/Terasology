package org.terasology.logic.console;

/**
 * Interface for subscribers to messages being added to the console
 * @author Immortius
 */
public interface ConsoleSubscriber {

    /**
     * Called each time a message is added to the console
     * @param message
     */
    void onNewConsoleMessage(Message message);
}

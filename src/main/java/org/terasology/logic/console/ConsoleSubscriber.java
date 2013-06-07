package org.terasology.logic.console;

/**
 * @author Immortius
 */
public interface ConsoleSubscriber {

    void onNewConsoleMessage(Message message);
}

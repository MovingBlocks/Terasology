package org.terasology.utilities.concurrency;

/**
 * @author Immortius
 */
public interface Task {

    void enact();

    boolean isTerminateSignal();
}

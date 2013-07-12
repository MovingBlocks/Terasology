package org.terasology.utilities.concurrency;

/**
 * @author Immortius
 */
public class ShutdownTask implements Task {
    @Override
    public void enact() {
    }

    @Override
    public boolean isTerminateSignal() {
        return true;
    }
}

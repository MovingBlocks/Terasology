package org.terasology.core.debug;

/**
 * Describes an instance of a benchmark which can be run.
 *
 * <p>The reason for not simply using the {@link Runnable} interface is that we
 * may need something like a {@code prepareStep()} or {@code cleanupStep()}
 * method, which must not be measured, in the future.</p>
 */
abstract class AbstractBenchmarkInstance {

    public abstract void runStep();
}

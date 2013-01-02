package org.terasology.benchmark;

import com.google.common.base.Preconditions;

/**
 * BenchmarkError encapsulates an error that occurred during a benchmark. 
 * It stores the type of the error and the exception object. 
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public class BenchmarkError {
    
    public static enum Type {
        Setup(true), PreRun(true), Run(true), PostRun(true), Finish(false);

        public final boolean abort;

        private Type(boolean abort) {
            this.abort = abort;
        }
    }
    
    public final Type type;
    public final Exception error;
    
    public BenchmarkError(Type type, Exception error) {
        this.type = Preconditions.checkNotNull(type);
        this.error = Preconditions.checkNotNull(error);
    }
    
}
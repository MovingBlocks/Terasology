/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.benchmark;

import com.google.common.base.Preconditions;

/**
 * BenchmarkError encapsulates an error that occurred during a benchmark.
 * It stores the type of the error and the exception object.
 *
 */
public class BenchmarkError {

    public static enum Type {
        Setup(true), Warmup(true), PreRun(true), Run(true), PostRun(true), Finish(false);

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

/*
 * Copyright 2016 MovingBlocks
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

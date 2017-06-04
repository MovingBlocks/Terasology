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
package org.terasology.exception;

import org.terasology.module.sandbox.API;

/**
 * Used when a resource access has been blocked by the sandbox.
 */
@API
public class SandboxException extends Exception {
    /**
     * Constructs a new SandboxException with the given message and cause.
     *
     * @param message The message.
     * @param cause   The cause.
     */
    public SandboxException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new SandboxException with the given cause.
     *
     * @param cause The cause.
     */
    public SandboxException(Throwable cause) {
        super(cause);
    }
}

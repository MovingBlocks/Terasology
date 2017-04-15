/*
 * Copyright 2017 MovingBlocks
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
 * Thrown to indicate permission has been denied. This
 * is a checked exception because this is supposed to
 * happen. Users may deny permissions at any time and
 * you use this for that. You should gracefully handle
 * this.
 */
@API
public class SandboxException extends Exception {
    public SandboxException() {
    }

    public SandboxException(String message) {
        super(message);
    }

    public SandboxException(String message, Throwable cause) {
        super(message, cause);
    }

    public SandboxException(Throwable cause) {
        super(cause);
    }

    public SandboxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

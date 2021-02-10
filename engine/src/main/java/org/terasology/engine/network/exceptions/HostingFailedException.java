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

package org.terasology.network.exceptions;

/**
 * Exception when hosting a game fails.
 *
 */
public class HostingFailedException extends Exception {

    private static final long serialVersionUID = -7936928340463418712L;

    public HostingFailedException() {
        super();
    }

    public HostingFailedException(String message) {
        super(message);
    }

    public HostingFailedException(Throwable cause) {
        super(cause);
    }

    public HostingFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}

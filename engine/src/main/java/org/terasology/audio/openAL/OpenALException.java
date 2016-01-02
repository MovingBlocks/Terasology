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
package org.terasology.audio.openAL;

import org.lwjgl.openal.AL10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class OpenALException extends IllegalStateException {

    private static final long serialVersionUID = 8714084225307679407L;

    private OpenALException(String message, int errorCode) {
        super("OpenAL Error (" + errorCode + ") at " + message + " - " + AL10.alGetString(errorCode));
    }

    public static void checkState(String message) {
        int error = AL10.alGetError();

        if (error != AL10.AL_NO_ERROR) {
            OpenALException exception = new OpenALException(message, error);

            exception.fillInStackTrace();

            List<StackTraceElement> stackTrace = new ArrayList<>(Arrays.asList(exception.getStackTrace()));
            stackTrace.remove(0); // remove first element in stack
            exception.setStackTrace(stackTrace.toArray(new StackTraceElement[stackTrace.size()]));

            throw exception;
        }
    }


}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.openAL;

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

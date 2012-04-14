package org.terasology.audio;

import org.lwjgl.openal.AL10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenALException extends IllegalStateException {

    public static void checkState(String message) {
        int error = AL10.alGetError();

        if (error != AL10.AL_NO_ERROR) {
            OpenALException exception = new OpenALException(message, error);

            exception.fillInStackTrace();

            List<StackTraceElement> stackTrace = new ArrayList<StackTraceElement>(Arrays.asList(exception.getStackTrace()));
            stackTrace.remove(0); // remove first element in stack
            exception.setStackTrace(stackTrace.toArray(new StackTraceElement[stackTrace.size()]));

            throw exception;
        }
    }

    protected OpenALException(String message, int errorCode) {
        super("OpenAL Error (" + errorCode + ") at " + message + " - " + AL10.alGetString(errorCode));
    }
}

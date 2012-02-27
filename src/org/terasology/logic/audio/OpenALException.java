package org.terasology.logic.audio;

import org.lwjgl.openal.AL10;

public class OpenALException extends IllegalStateException {

    public static void checkState(String message) {
        int error = AL10.alGetError();
        
        if (error != AL10.AL_NO_ERROR) {
            throw new OpenALException(message, error);
        }
    }
    
    protected OpenALException(String message, int errorCode) {
        super("OpenAL Error at " + message + " - " + AL10.alGetString(errorCode));
    }
}

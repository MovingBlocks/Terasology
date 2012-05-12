package org.terasology.audio;

import org.terasology.asset.Asset;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public interface Sound extends Asset {

    /**
     * Returns sound sample length in seconds
     * Not available on streaming sounds (will return -1)
     *
     * @return
     */
    public int getLength();

    /**
     * Return channels amount of sound (1 - mono, 2 - stereo)
     *
     * @return
     */
    public int getChannels();

    /**
     * Returns sampling rate of sound (example 44100)
     *
     * @return
     */
    public int getSamplingRate();

    /**
     * Reset sound state (clears buffers, reset cached info)
     *
     * @return
     */
    public void reset();

    // TODO: Have these here?
    /**
     * @return the size of the sound buffer
     */
    public int getBufferSize();

    /**
     * @return the id of the sound buffer
     */
    public int getBufferId();

}

package org.terasology.logic.audio;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public interface Sound {

    /**
     * Load sound from input stream
     *
     * @param stream
     */
    public void load(InputStream stream);

    /**
     * Loads sound from file
     *
     * @param file
     */
    public void load(File file);

    /**
     * Loads sound from specified URL
     *
     * @param source
     */
    public void load(URL source);

    /**
     * Returns sound sample length in seconds
     * Not available on streaming sounds (will return -1)
     *
     * @return
     */
    public int getLength();

    /**
     * Returns sound associated name (example: Explosion1)
     *
     * @return
     */
    public String getName();

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
    public Sound reset();

}

package org.terasology.logic.audio;

import java.io.File;
import java.io.InputStream;

public interface Sound {

    public void load(InputStream stream);
    public void load(File file);

    public int getLength();

    public String getName();
    
    public int getChannels();

    public int getSamplingRate();

}

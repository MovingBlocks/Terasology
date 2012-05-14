package org.terasology.audio;

import org.terasology.asset.AssetUri;
import org.terasology.utilities.OggReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class OggStreamingSound extends AbstractStreamingSound {

    private ByteBuffer dataBuffer = ByteBuffer.allocateDirect(4096 * 8);
    private OggReader file = null;
    private Logger logger = Logger.getLogger(getClass().getName());

    public OggStreamingSound(AssetUri uri, URL source) {
        super(uri, source);
    }

    @Override
    public int getBufferBits() {
        return 16; // Ogg is always 16-bit
    }

    @Override
    public int getLength() {
        return -1; // not supported
    }

    @Override
    public int getChannels() {
        return this.file.getChannels();
    }

    @Override
    public int getSamplingRate() {
        return this.file.getRate();
    }

    @Override
    public void reset() {
        if (this.file != null) {
            try {
                this.file.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to close streaming sound: " + getURI(), e);
            }
        }

        try {
            this.file = new OggReader(audioSource.openStream());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load streaming sound: " + getURI(), e);
        }
    }

    @Override
    protected ByteBuffer fetchData() {
        try {
            int read = file.read(dataBuffer, 0, dataBuffer.capacity());
            dataBuffer.rewind();
            // do something :D
            if (read <= 0) {  // end of datastream
                return null;
            }

            return dataBuffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

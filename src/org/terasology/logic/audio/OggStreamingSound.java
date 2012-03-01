package org.terasology.logic.audio;

import org.terasology.utilities.OggReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;


public class OggStreamingSound extends AbstractStreamingSound {

    private ByteBuffer dataBuffer = ByteBuffer.allocateDirect(4096 * 8);
    private OggReader file = null;

    public OggStreamingSound(String name, URL source) {
        super(name, source);
    }

    public void load(InputStream stream) {
        if (this.file != null) {
            try {
                this.file.close();
            } catch (IOException e) {
                // @todo add logger warning
            }
        }

        this.file = new OggReader(stream);
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

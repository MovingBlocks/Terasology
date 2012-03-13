package org.terasology.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.terasology.utilities.OggReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class OggSound extends AbstractSound {
    private Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

    private int channels;

    public OggSound(String name, URL source) {
        super(name, source);
    }

    public void load(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("Sound stream is null");
        }

        try {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OggReader reader = new OggReader(stream);

            byte buffer[] = new byte[1024];
            int read;
            int totalRead = 0;

            do {
                read = reader.read(buffer, 0, buffer.length);

                if (read < 0) {
                    break;
                }

                totalRead += read;

                bos.write(buffer, 0, read);
            } while (read > 0);

            buffer = bos.toByteArray();

            ByteBuffer data = BufferUtils.createByteBuffer(totalRead).put(buffer);
            data.flip();

            this.channels = reader.getChannels();
            int sampleRate = reader.getRate();
            AL10.alBufferData(this.getBufferId(), this.channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, data, sampleRate);

            OpenALException.checkState("Uploading buffer");

            //data = null; // dispose data
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load sound: " + e.getMessage(), e);
        }
    }
}

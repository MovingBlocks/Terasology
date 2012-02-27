package org.terasology.logic.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import java.nio.IntBuffer;

public class BasicStreamingSoundSource extends BasicSoundSource {

    public BasicStreamingSoundSource() {
        super();
    }

    public BasicStreamingSoundSource(AbstractStreamingSound audio) {
        super(audio);
    }

    @Override
    public float getPlaybackPositionf() {
        // @todo add logger warning
        return -1.0f;
    }

    @Override
    public SoundSource setPlaybackPosition(int position) {
        // do nothing
        // @todo add logger warning
        return this;
    }

    @Override
    public int getPlaybackPosition() {
        // @todo add logger warning
        return -1;
    }

    @Override
    public SoundSource setPlaybackPosition(float position) {
        // do nothing
        // @todo add logger warning
        return this;
    }

    @Override
    public void update() {
        int buffersProcessed = AL10.alGetSourcei(this.getSourceId(), AL10.AL_BUFFERS_PROCESSED);

        while (buffersProcessed-- > 0) {
            int buffer = AL10.alSourceUnqueueBuffers(this.getSourceId());

            if(((AbstractStreamingSound) audio).updateBuffer(buffer)) {
                AL10.alSourceQueueBuffers(this.getSourceId(), buffer);
            } else {
                _playing = false; // we aren't playing anymore, because stream seems to end
                continue;  // do nothing, let source dequeue other buffers
            }

            OpenALException.checkState("Buffer refill");
        }

        // Start playing if playback for stopped by end of buffers
        int state = AL10.alGetSourcei(getSourceId(), AL10.AL_SOURCE_STATE);
        if (_playing && state != AL10.AL_PLAYING) {
            AL10.alSourcePlay(this.getSourceId());
        }

        super.update();
    }

    public SoundSource setAudio(Sound sound) {
        if (sound == null || sound.equals(this.audio)) {
            return this;
        }

        if (sound instanceof AbstractStreamingSound) {
            AbstractStreamingSound asa = (AbstractStreamingSound) sound;
            this.audio = asa;

            int[] buffers = asa.getBuffers();

            for (int buffer : buffers) {
                asa.updateBuffer(buffer);
            }

            AL10.alSourceQueueBuffers(this.getSourceId(), (IntBuffer) BufferUtils.createIntBuffer(buffers.length).put(buffers).flip());
        } else {
            throw new IllegalArgumentException("Unsupported sound object!");
        }

        return this;
    }
}

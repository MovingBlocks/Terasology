package org.terasology.logic.audio;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.*;

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
    public boolean isLooping() {
        return false;
    }

    @Override
    public SoundSource setLooping(boolean looping) {
        if (looping) {
            throw new UnsupportedOperationException("Looping is unsupported on streaming sounds!");
        }

        return this;
    }

    @Override
    public void update() {
        int buffersProcessed = alGetSourcei(this.getSourceId(), AL_BUFFERS_PROCESSED);

        while (buffersProcessed-- > 0) {
            int buffer = alSourceUnqueueBuffers(this.getSourceId());
            OpenALException.checkState("Buffer unqueue");

            if (((AbstractStreamingSound) audio).updateBuffer(buffer)) {
                alSourceQueueBuffers(this.getSourceId(), buffer);
                OpenALException.checkState("Buffer refill");
            } else {
                _playing = false; // we aren't playing anymore, because stream seems to end
                continue;  // do nothing, let source dequeue other buffers
            }
        }

        super.update();
    }

    @Override
    protected void updateState() {
        // Start playing if playback for stopped by end of buffers
        if (_playing && alGetSourcei(getSourceId(), AL_SOURCE_STATE) != AL_PLAYING) {
            alSourcePlay(this.getSourceId());
        }
    }

    public SoundSource setAudio(Sound sound) {
        boolean playing = this.isPlaying();
        if (playing) {
            alSourceStop(this.sourceId);
            alSourceRewind(this.sourceId);
        }

        if (sound instanceof AbstractStreamingSound) {
            alSourcei(this.getSourceId(), AL_BUFFER, 0);

            AbstractStreamingSound asa = (AbstractStreamingSound) sound;
            this.audio = asa;

            asa.reset();

            int[] buffers = asa.getBuffers();

            for (int buffer : buffers) {
                asa.updateBuffer(buffer);
            }

            alSourceQueueBuffers(this.getSourceId(), (IntBuffer) BufferUtils.createIntBuffer(buffers.length).put(buffers).flip());
        } else {
            throw new IllegalArgumentException("Unsupported sound object!");
        }

        if (playing) {
            this.play();
        }

        return this;
    }

}

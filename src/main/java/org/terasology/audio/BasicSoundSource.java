package org.terasology.audio;

import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_MAX_DISTANCE;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_REFERENCE_DISTANCE;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcef;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSource3f;
import static org.lwjgl.openal.AL10.alSourceRewind;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

import javax.vecmath.Vector3d;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.SoundManager;
import org.terasology.rendering.world.WorldRenderer;

public class BasicSoundSource implements SoundSource {

    protected Sound audio;
    protected int sourceId;

    protected float srcGain = 1.0f;
    protected float targetGain = 1.0f;
    protected boolean fade = false;

    protected Vector3d position = new Vector3d();
    protected Vector3d velocity = new Vector3d();
    protected Vector3d direction = new Vector3d();

    protected boolean absolutePosition = false;

    protected boolean _playing = false;

    public BasicSoundSource() {
        sourceId = alGenSources();
        OpenALException.checkState("Creating sound source");

        reset();
    }

    public BasicSoundSource(Sound audio) {
        this();

        setAudio(audio);
    }

    @Override
    public SoundSource play() {
        if (!isPlaying()) {
            AL10.alSourcePlay(getSourceId());
            _playing = true;
        }

        return this;
    }

    @Override
    public SoundSource stop() {
        if (audio != null) {
            audio.reset();
        }

        alSourceStop(getSourceId());

        OpenALException.checkState("Rewind");

        alSourceRewind(getSourceId());

        OpenALException.checkState("Stop playback");

        _playing = false;

        return this;
    }

    @Override
    public SoundSource pause() {
        if (isPlaying()) {
            AL10.alSourcePause(getSourceId());

            OpenALException.checkState("Pause playback");

            _playing = false;
        }

        return this;
    }

    @Override
    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING || _playing;
    }

    @Override
    public void update() {
        updateFade();

        if (absolutePosition) {
            updatePosition(position);
        }

        updateState();
    }

    protected void updateState() {
        if (_playing && alGetSourcei(sourceId, AL_SOURCE_STATE) != AL_PLAYING) {
            _playing = false; // sound stop playing
        }
    }

    @Override
    public SoundSource reset() {
        setPitch(1.0f);
        setLooping(false);
        setGain(1.0f);
        setAbsolute(false);

        Vector3d zeroVector = new Vector3d();
        setPosition(zeroVector);
        setVelocity(zeroVector);
        setDirection(zeroVector);

        // some additional settings
        alSourcef(getSourceId(), AL_MAX_DISTANCE, SoundManager.MAX_DISTANCE);
        alSourcef(getSourceId(), AL_REFERENCE_DISTANCE, 0.1f);

        fade = false;
        srcGain = 1.0f;
        targetGain = 1.0f;

        return this;
    }

    @Override
    public int getLength() {
        return audio.getLength();
    }

    @Override
    public SoundSource setPlaybackPosition(int position) {
        boolean playing = isPlaying();
        if (playing) {
            AL10.alSourceStop(getSourceId());
        }

        AL10.alSourceRewind(getSourceId());
        AL10.alSourcei(getSourceId(), AL11.AL_SAMPLE_OFFSET, audio.getSamplingRate() * position);

        OpenALException.checkState("Setting sound playback absolute position");

        if (playing) {
            play();
        }

        return this;
    }

    @Override
    public int getPlaybackPosition() {
        return AL10.alGetSourcei(getSourceId(), AL11.AL_SAMPLE_OFFSET) / audio.getSamplingRate();
    }

    @Override
    public SoundSource setPlaybackPosition(float position) {
        boolean playing = isPlaying();
        if (playing) {
            AL10.alSourceStop(getSourceId());
        }

        AL10.alSourceRewind(getSourceId());
        AL10.alSourcei(getSourceId(), AL11.AL_BYTE_OFFSET, (int) (audio.getBufferSize() * position));

        OpenALException.checkState("Setting sound playback relaive position");

        if (playing) {
            play();
        }

        return this;
    }

    @Override
    public float getPlaybackPositionf() {
        return AL10.alGetSourcei(getSourceId(), AL11.AL_BYTE_OFFSET) / audio.getBufferSize();
    }

    @Override
    public SoundSource setAbsolute(boolean absolute) {
        absolutePosition = absolute;

        return this;
    }

    @Override
    public boolean isAbsolute() {
        return absolutePosition;
    }

    @Override
    public Vector3d getVelocity() {
        return velocity;
    }

    @Override
    public SoundSource setVelocity(Vector3d velocity) {
        if (velocity == null || this.velocity.equals(velocity)) {
            return this;
        }

        this.velocity.set(velocity);

        AL10.alSource3f(getSourceId(), AL10.AL_VELOCITY, (float) velocity.x, (float) velocity.y, (float) velocity.z);

        OpenALException.checkState("Setting sound source velocity");

        return this;
    }

    @Override
    public Vector3d getPosition() {
        return position;
    }

    @Override
    public SoundSource setPosition(Vector3d position) {
        if (position == null || this.position.equals(position)) {
            return this;
        }

        this.position.set(position);

        updatePosition(position);

        return this;
    }

    @Override
    public SoundSource setDirection(Vector3d direction) {
        if (direction == null || this.direction.equals(direction)) {
            return this;
        }

        AL10.alSource3f(getSourceId(), AL10.AL_DIRECTION, (float) direction.x, (float) direction.y, (float) direction.z);

        OpenALException.checkState("Setting sound source direction");

        this.direction.set(direction);

        return this;
    }

    @Override
    public Vector3d getDirection() {
        return direction;
    }

    @Override
    public float getPitch() {
        return AL10.alGetSourcef(getSourceId(), AL10.AL_PITCH);
    }

    @Override
    public SoundSource setPitch(float pitch) {
        AL10.alSourcef(getSourceId(), AL10.AL_PITCH, pitch);

        OpenALException.checkState("Setting sound pitch");

        return this;
    }

    @Override
    public float getGain() {
        return alGetSourcef(getSourceId(), AL_GAIN);
    }

    @Override
    public SoundSource setGain(float gain) {
        alSourcef(getSourceId(), AL_GAIN, gain);

        OpenALException.checkState("Setting sound gain");

        return this;
    }

    @Override
    public boolean isLooping() {
        return alGetSourcei(getSourceId(), AL_LOOPING) == AL_TRUE;
    }

    @Override
    public SoundSource setLooping(boolean looping) {
        alSourcei(getSourceId(), AL_LOOPING, looping ? AL_TRUE : AL_FALSE);

        OpenALException.checkState("Setting sound looping");

        return this;
    }

    @Override
    public SoundSource setAudio(Sound sound) {
        boolean playing = isPlaying();
        if (playing) {
            stop();
        }

        reset();

        if (sound instanceof AbstractSound) {
            audio = sound;
            AL10.alSourcei(getSourceId(), AL10.AL_BUFFER, audio.getBufferId());

            OpenALException.checkState("Assigning buffer to source");
        } else {
            throw new IllegalArgumentException("Unsupported sound object!");
        }

        if (playing) {
            play();
        }

        return this;
    }

    @Override
    public Sound getAudio() {
        return audio;
    }

    @Override
    public SoundSource fade(float targetGain) {
        srcGain = getGain();
        this.targetGain = targetGain;
        fade = true;

        return this;
    }

    public int getSourceId() {
        return sourceId;
    }

    protected void updatePosition(Vector3d position) {
        float[] pos = new float[]{(float) position.x, (float) position.y, (float) position.z};

        if (isAbsolute()) {
            Vector3d cameraPos = getCameraPosition();
            pos[0] -= cameraPos.x;
            pos[1] -= cameraPos.y;
            pos[2] -= cameraPos.z;
        }

        alSource3f(getSourceId(), AL10.AL_POSITION, pos[0], pos[1], pos[2]);

        OpenALException.checkState("Changing sound position");
    }

    private void updateFade() {
        if (!fade) {
            return;
        }

        float delta = (srcGain - targetGain) / 100;
        setGain(getGain() - delta);

        if (getGain() >= targetGain) {
            if (targetGain == 0.0f) {
                stop();
            }

            fade = false;
        }
    }

    private Vector3d getCameraPosition() {
        return CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
    }

    @Override
    // TODO: This is no guaranteed to be executed at all – move to a safer place
    protected void finalize() throws Throwable {
        if (sourceId != 0) {
            AL10.alDeleteSources(sourceId);
        }
        super.finalize();
    }

    @Override
    public int hashCode() {
        return getSourceId();
    }


}

package org.terasology.logic.audio;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.terasology.game.Terasology;
import org.terasology.logic.characters.Player;

import javax.vecmath.Vector3d;

import static org.lwjgl.openal.AL10.*;

public class BasicSoundSource implements SoundSource {

    protected AbstractSound audio;
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
        this.sourceId = alGenSources();
        OpenALException.checkState("Creating sound source");

        this.reset();
    }

    public BasicSoundSource(AbstractSound audio) {
        this();

        this.setAudio(audio);
    }

    public SoundSource play() {
        if (!isPlaying()) {
            AL10.alSourcePlay(this.getSourceId());
            _playing = true;
        }

        return this;
    }

    public SoundSource stop() {
        if (this.audio != null) {
            this.audio.reset();
        }

        alSourceStop(this.getSourceId());
        alSourceRewind(this.getSourceId());

        OpenALException.checkState("Stop playback");

        _playing = false;

        return this;
    }

    public SoundSource pause() {
        if (isPlaying()) {
            AL10.alSourcePause(this.getSourceId());

            OpenALException.checkState("Pause playback");

            _playing = false;
        }

        return this;
    }

    public boolean isPlaying() {
        return (alGetSourcei(this.sourceId, AL_SOURCE_STATE) == AL_PLAYING) || _playing;
    }

    public void update() {
        this.updateFade();

        if (this.absolutePosition) {
            updatePosition(this.position);
        }

        this.updateState();
    }

    protected void updateState() {
        if (_playing && alGetSourcei(this.sourceId, AL_SOURCE_STATE) != AL_PLAYING) {
            _playing = false; // sound stop playing
        }
    }

    public SoundSource reset() {
        this.setPitch(1.0f);
        this.setLooping(false);
        this.setGain(1.0f);
        this.setAbsolute(false);

        Vector3d zeroVector = new Vector3d();
        this.setPosition(zeroVector);
        this.setVelocity(zeroVector);
        this.setDirection(zeroVector);

        // some additional settings
        alSourcef(this.getSourceId(), AL_MAX_DISTANCE, 15.0f);
        alSourcef(this.getSourceId(), AL_REFERENCE_DISTANCE, 1.0f);

        this.fade = false;
        this.srcGain = 1.0f;
        this.targetGain = 1.0f;

        return this;
    }

    public int getLength() {
        return audio.getLength();
    }

    public SoundSource setPlaybackPosition(int position) {
        boolean playing = isPlaying();
        if (playing) {
            AL10.alSourceStop(this.getSourceId());
        }

        AL10.alSourceRewind(this.getSourceId());
        AL10.alSourcei(this.getSourceId(), AL11.AL_SAMPLE_OFFSET, (this.audio.getSamplingRate() * position));

        OpenALException.checkState("Setting sound playback absolute position");

        if (playing) {
            this.play();
        }

        return this;
    }

    public int getPlaybackPosition() {
        return AL10.alGetSourcei(this.getSourceId(), AL11.AL_SAMPLE_OFFSET) / this.audio.getSamplingRate();
    }

    public SoundSource setPlaybackPosition(float position) {
        boolean playing = isPlaying();
        if (playing) {
            AL10.alSourceStop(this.getSourceId());
        }

        AL10.alSourceRewind(this.getSourceId());
        AL10.alSourcei(this.getSourceId(), AL11.AL_BYTE_OFFSET, (int) (this.audio.getBufferSize() * position));

        OpenALException.checkState("Setting sound playback relaive position");

        if (playing) {
            this.play();
        }

        return this;
    }

    public float getPlaybackPositionf() {
        return AL10.alGetSourcei(this.getSourceId(), AL11.AL_BYTE_OFFSET) / this.audio.getBufferSize();
    }

    public SoundSource setAbsolute(boolean absolute) {
        this.absolutePosition = absolute;

        return this;
    }

    public boolean isAbsolute() {
        return this.absolutePosition;
    }

    public Vector3d getVelocity() {
        return velocity;
    }

    public SoundSource setVelocity(Vector3d velocity) {
        if (velocity == null || this.velocity.equals(velocity)) {
            return this;
        }

        this.velocity.set(velocity);

        AL10.alSource3f(this.getSourceId(), AL10.AL_VELOCITY, (float) velocity.x, (float) velocity.y, (float) velocity.z);

        OpenALException.checkState("Setting sound source velocity");

        return this;
    }

    public Vector3d getPosition() {
        return position;
    }

    public SoundSource setPosition(Vector3d position) {
        if (position == null || this.position.equals(position)) {
            return this;
        }

        this.position.set(position);

        updatePosition(position);

        return this;
    }

    public SoundSource setDirection(Vector3d direction) {
        if (direction == null || this.direction.equals(direction)) {
            return this;
        }

        AL10.alSource3f(this.getSourceId(), AL10.AL_DIRECTION, (float) direction.x, (float) direction.y, (float) direction.z);

        OpenALException.checkState("Setting sound source direction");

        this.direction.set(direction);

        return this;
    }

    public Vector3d getDirection() {
        return direction;
    }

    public float getPitch() {
        return AL10.alGetSourcef(this.getSourceId(), AL10.AL_PITCH);
    }

    public SoundSource setPitch(float pitch) {
        AL10.alSourcef(this.getSourceId(), AL10.AL_PITCH, pitch);

        OpenALException.checkState("Setting sound pitch");

        return this;
    }

    public float getGain() {
        return AL10.alGetSourcef(this.getSourceId(), AL10.AL_GAIN);
    }

    public SoundSource setGain(float gain) {
        AL10.alSourcef(this.getSourceId(), AL10.AL_GAIN, gain);

        OpenALException.checkState("Setting sound gain");

        return this;
    }

    public boolean isLooping() {
        return alGetSourcei(this.getSourceId(), AL_LOOPING) == AL_TRUE;
    }

    public SoundSource setLooping(boolean looping) {
        alSourcei(this.getSourceId(), AL_LOOPING, looping ? AL_TRUE : AL_FALSE);

        OpenALException.checkState("Setting sound looping");

        return this;
    }

    public SoundSource setAudio(Sound sound) {
        boolean playing = isPlaying();
        if (playing) {
            this.stop();
        }

        this.reset();

        if (sound instanceof AbstractSound) {
            this.audio = (AbstractSound) sound;
            AL10.alSourcei(this.getSourceId(), AL10.AL_BUFFER, this.audio.getBufferId());

            OpenALException.checkState("Assigning buffer to source");
        } else {
            throw new IllegalArgumentException("Unsupported sound object!");
        }

        if (playing) {
            this.play();
        }

        return this;
    }

    public Sound getAudio() {
        return audio;
    }

    public SoundSource fade(float targetGain) {
        this.srcGain = this.getGain();
        this.targetGain = targetGain;
        this.fade = true;

        return this;
    }

    public int getSourceId() {
        return this.sourceId;
    }

    protected void updatePosition(Vector3d position) {
        float[] pos = new float[]{(float) position.x, (float) position.y, (float) position.z};

        if (this.isAbsolute()) {
            Vector3d playerPos = this.getPlayerPosition();
            pos[0] -= playerPos.x;
            pos[1] -= playerPos.y;
            pos[2] -= playerPos.z;
        }

        alSource3f(this.getSourceId(), AL10.AL_POSITION, pos[0], pos[1], pos[2]);

        OpenALException.checkState("Chaning sound position");
    }

    private void updateFade() {
        if (!fade) {
            return;
        }

        float delta = (this.srcGain - this.targetGain) / 100;
        this.setGain(this.getGain() - delta);

        if (this.getGain() >= this.targetGain) {
            if (this.targetGain == 0.0f) {
                this.stop();
            }

            fade = false;
        }
    }

    private Vector3d getPlayerPosition() {
        Player player = Terasology.getInstance().getActivePlayer();

        if (player != null)
            return player.getPosition();

        return new Vector3d();
    }

    @Override
    protected void finalize() throws Throwable {
        if (this.sourceId != 0) {
            AL10.alDeleteSources(this.sourceId);
        }
        super.finalize();
    }

    @Override
    public int hashCode() {
        return this.getSourceId();
    }


}

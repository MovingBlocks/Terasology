package org.terasology.logic.audio;


import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.*;
import org.terasology.game.Terasology;
import org.terasology.logic.characters.Player;
import org.terasology.logic.manager.AudioManager;

import javax.vecmath.Vector3d;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class OpenALManager extends AudioManager {

    /**
     * For faster distance check *
     */
    private final static float MAX_DISTANCE_SQUARED = (float) Math.pow(MAX_DISTANCE, 2);

    public static OpenALManager getInstance() {
        return (OpenALManager) AudioManager.getInstance();
    }

    @Override
    public void initialize() {
        logger.info("Initializing OpenAL audio manager");
        try {
            AL.create();
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }

        AL10.alGetError();

        logger.info("OpenAL " + AL10.alGetString(AL10.AL_VERSION) + " initialized!");

        ALCcontext context = ALC10.alcGetCurrentContext();
        ALCdevice device = ALC10.alcGetContextsDevice(context);

        logger.info("Using OpenAL: " + AL10.alGetString(AL10.AL_RENDERER) + " by " + AL10.alGetString(AL10.AL_VENDOR));
        logger.info("Using device: " + ALC10.alcGetString(device, ALC10.ALC_DEVICE_SPECIFIER));
        logger.info("Available AL extensions: " + AL10.alGetString(AL10.AL_EXTENSIONS));
        logger.info("Available ALC extensions: " + ALC10.alcGetString(device, ALC10.ALC_EXTENSIONS));

        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        ALC10.alcGetInteger(device, ALC11.ALC_MONO_SOURCES, buffer);
        logger.info("Max mono sources: " + buffer.get(0));
        buffer.rewind();

        ALC10.alcGetInteger(device, ALC11.ALC_STEREO_SOURCES, buffer);
        logger.info("Max stereo sources: " + buffer.get(0));
        buffer.rewind();

        ALC10.alcGetInteger(device, ALC10.ALC_FREQUENCY, buffer);
        logger.info("Mixer frequency: " + buffer.get(0));
        buffer.rewind();

        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);

        // Initialize sound pools
        this._pools.put("sfx", new BasicSoundPool(30)); // effects pool
        this._pools.put("music", new BasicStreamingSoundPool(2)); // music pool

        this.loadAssets();
    }

    @Override
    public void destroy() {
        AL.destroy();
    }

    @Override
    public void update() {
        Player player = Terasology.getInstance().getActivePlayer();

        if (player != null) {
            Vector3d velocity = player.getVelocity();
            Vector3d orientation = player.getViewingDirection();

            AL10.alListener3f(AL10.AL_VELOCITY, (float) velocity.x, (float) velocity.y, (float) velocity.z);

            OpenALException.checkState("Setting listener velocity");


            FloatBuffer listenerOri = BufferUtils.createFloatBuffer(6).put(new float[]{(float) orientation.x, (float) orientation.y, (float) orientation.z, 0.0f, 1.0f, 0.0f});
            listenerOri.flip();
            AL10.alListener(AL10.AL_ORIENTATION, listenerOri);

            OpenALException.checkState("Setting listener orientation");
        }

        for (SoundPool pool : _pools.values()) {
            pool.update();
        }
    }

    @Override
    protected Sound createAudio(String name, URL source) {
        return new OggSound(name, source);
    }

    @Override
    protected Sound createStreamingAudio(String name, URL source) {
        return new OggStreamingSound(name, source);
    }

    @Override
    protected boolean checkDistance(Vector3d soundSource) {
        Player player = Terasology.getInstance().getActivePlayer();

        if (player == null) {
            return false;
        }

        Vector3d soundPosition = new Vector3d(soundSource);
        soundPosition.sub(player.getPosition());

        return soundPosition.lengthSquared() < MAX_DISTANCE_SQUARED;
    }
}
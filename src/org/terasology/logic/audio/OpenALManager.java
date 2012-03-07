package org.terasology.logic.audio;


import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.terasology.game.Terasology;
import org.terasology.logic.characters.Player;
import org.terasology.logic.global.LocalPlayer;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.cameras.Camera;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.io.InputStream;
import java.net.URL;
import java.nio.FloatBuffer;

public class OpenALManager extends AudioManager {

    /** For faster distance check **/
    private final static float MAX_DISTANCE_SQUARED = (float)Math.pow(MAX_DISTANCE, 2);

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
        LocalPlayer player = Terasology.getInstance().getActivePlayer();

        if (player != null) {
            Vector3f velocity = player.getVelocity();
            // TODO: get this from camera
            Vector3f orientation = player.getViewDirection();

            AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);

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
        Camera camera = Terasology.getInstance().getActiveCamera();

        if (camera == null) {
            return false;
        }

        Vector3d soundPosition = new Vector3d(soundSource);
        soundPosition.sub(camera.getPosition());

        return soundPosition.lengthSquared() < MAX_DISTANCE_SQUARED;
    }
}
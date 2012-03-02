package org.terasology.logic.audio;


import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.terasology.game.Terasology;
import org.terasology.logic.characters.Player;
import org.terasology.logic.manager.AudioManager;

import javax.vecmath.Vector3d;
import java.io.InputStream;
import java.net.URL;
import java.nio.FloatBuffer;

public class OpenALManager extends AudioManager {

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

        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE);

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

}
/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.audio.openAL;

import static org.lwjgl.openal.AL10.AL_SIZE;
import static org.lwjgl.openal.AL10.alGetBufferi;

import java.net.URL;
import java.nio.ByteBuffer;

import org.lwjgl.openal.AL10;
import org.terasology.asset.AssetUri;
import org.terasology.audio.Sound;
import org.terasology.audio.openAL.OpenALException;

public abstract class OpenALStreamingSound implements Sound {
    private final static int BUFFER_POOL_SIZE = 3;

    private final AssetUri uri;
    protected final URL audioSource;

    protected int[] buffers;

    protected int lastUpdatedBuffer;

    public OpenALStreamingSound(AssetUri uri, URL source) {
        this.uri = uri;
        this.audioSource = source;

        this.initializeBuffers();

        this.reset();
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    protected abstract ByteBuffer fetchData();

    public int[] getBuffers() {
        return this.buffers;
    }

    public boolean updateBuffer(int buffer) {
        ByteBuffer bufferData = this.fetchData();

        if (bufferData == null || bufferData.limit() == 0) {
            return false; // no more data available
        }

        AL10.alBufferData(buffer, this.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, bufferData, this.getSamplingRate());
        OpenALException.checkState("Uploading buffer data");

        this.lastUpdatedBuffer = buffer;

        return true;
    }

    private void initializeBuffers() {
        buffers = new int[BUFFER_POOL_SIZE];

        for (int i = 0; i < buffers.length; i++) {
            buffers[i] = AL10.alGenBuffers();
            OpenALException.checkState("Creating buffer");
        }

        this.lastUpdatedBuffer = buffers[0];
    }

    @Override
    public int getBufferSize() {
        return alGetBufferi(lastUpdatedBuffer, AL_SIZE);
    }

    @Override
    public int getBufferId() {
        return lastUpdatedBuffer;
    }

    public abstract int getBufferBits();

    @Override
    public void dispose() {
        // TODO: Fix this
        for (int i = 0; i < buffers.length; i++) {
            if (buffers[i] != 0) {
               // AL10.alDeleteBuffers(buffers[i]);
            }
        }
        OpenALException.checkState("Deleting buffer data");
        buffers = new int[0];
    }
}

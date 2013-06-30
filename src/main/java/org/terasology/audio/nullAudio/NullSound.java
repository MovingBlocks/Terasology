/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.audio.nullAudio;

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.audio.StaticSoundData;
import org.terasology.audio.StaticSound;

/**
 * @author Immortius
 */
public class NullSound extends AbstractAsset<StaticSoundData> implements StaticSound {

    private int channels;
    private int sampleRate;
    private float length;

    public NullSound(AssetUri uri, StaticSoundData data) {
        super(uri);
        reload(data);
    }

    @Override
    public float getLength() {
        return length;
    }

    @Override
    public int getChannels() {
        return channels;
    }

    @Override
    public int getSamplingRate() {
        return sampleRate;
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void reload(StaticSoundData data) {
        this.channels = data.getChannels();
        this.sampleRate = data.getSampleRate();
        this.length = data.getData().limit() / getChannels() / (data.getBufferBits() / 8) / getSamplingRate();
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isDisposed() {
        return false;
    }
}
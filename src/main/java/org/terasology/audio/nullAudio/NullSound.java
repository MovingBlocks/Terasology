package org.terasology.audio.nullAudio;

import org.terasology.asset.AssetUri;
import org.terasology.audio.Sound;

/**
 * @author Immortius
 */
public class NullSound implements Sound {

    private AssetUri uri;

    public NullSound(AssetUri uri) {
        this.uri = uri;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public int getChannels() {
        return 0;
    }

    @Override
    public int getSamplingRate() {
        return 0;
    }

    @Override
    public void reset() {
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public int getBufferId() {
        return 0;
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public void dispose() {
    }
}

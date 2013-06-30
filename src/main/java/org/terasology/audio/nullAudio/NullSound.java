package org.terasology.audio.nullAudio;

import org.terasology.asset.AssetUri;
import org.terasology.asset.CompatibilityHackAsset;
import org.terasology.audio.Sound;

/**
 * @author Immortius
 */
// TODO: Provide as much information as possible without loading the sound proper
public class NullSound extends CompatibilityHackAsset implements Sound {

    public NullSound(AssetUri uri) {
        super(uri);
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
    public void dispose() {
    }
}
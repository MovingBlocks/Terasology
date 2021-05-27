// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.openAL.streamingSound;

import org.terasology.engine.audio.openAL.BaseSoundPool;

public class OpenALStreamingSoundPool extends BaseSoundPool<OpenALStreamingSound, OpenALStreamingSoundSource> {

    public OpenALStreamingSoundPool(int capacity) {
        super(capacity);
    }

    public OpenALStreamingSoundPool() {
    }

    @Override
    protected OpenALStreamingSoundSource createSoundSource() {
        return new OpenALStreamingSoundSource(this);
    }

}

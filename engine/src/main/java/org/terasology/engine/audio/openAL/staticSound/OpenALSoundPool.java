// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.openAL.staticSound;

import org.terasology.engine.audio.openAL.BaseSoundPool;

public class OpenALSoundPool extends BaseSoundPool<OpenALSound, OpenALSoundSource> {

    public OpenALSoundPool() {
    }

    public OpenALSoundPool(int capacity) {
        super(capacity);
    }

    @Override
    protected OpenALSoundSource createSoundSource() {
        return new OpenALSoundSource(this);
    }

}

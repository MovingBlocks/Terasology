package org.terasology.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.terasology.asset.AssetUri;
import org.terasology.utilities.OggReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.logging.Logger;


public class OggSound extends AbstractSound {
    public OggSound(AssetUri uri, int bufferId) {
        super(uri, bufferId);
    }
}

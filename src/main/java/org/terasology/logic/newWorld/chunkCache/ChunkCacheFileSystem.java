package org.terasology.logic.newWorld.chunkCache;

import org.terasology.logic.newWorld.NewChunk;
import org.terasology.logic.newWorld.NewChunkCache;
import org.terasology.math.Vector3i;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChunkCacheFileSystem implements NewChunkCache {
    private Logger logger = Logger.getLogger(getClass().getName());
    private File worldPath;

    public ChunkCacheFileSystem(File worldPath) {
        this.worldPath = worldPath;
    }

    private String getFileNameFor(Vector3i pos) {
        return pos.x + "." + pos.y + "." + pos.z + ".chunk";
    }

    public NewChunk get(Vector3i id) {
        File f = new File(worldPath, getFileNameFor(id));

        if (!f.exists())
            return null;

        try {
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            NewChunk result = (NewChunk) in.readObject();

            in.close();
            fileIn.close();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void put(NewChunk c) {
        if (!worldPath.exists()) {
            if (!worldPath.mkdirs()) {
                logger.log(Level.SEVERE, "Could not create save directory.");
                return;
            }
        }

        File f = new File(worldPath, getFileNameFor(c.getPos()));

        try {
            FileOutputStream fileOut = new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(c);
            out.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float size() {
        return 0;
    }

    public void dispose() {
    }
}

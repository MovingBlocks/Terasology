package org.terasology.logic.world.chunkStore;

import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.ChunkStore;
import org.terasology.math.Vector3i;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChunkStoreFileSystem implements ChunkStore {
    private Logger logger = Logger.getLogger(getClass().getName());
    private File worldPath;

    public ChunkStoreFileSystem(File worldPath) {
        this.worldPath = worldPath;
    }

    private String getFileNameFor(Vector3i pos) {
        return pos.x + "." + pos.y + "." + pos.z + ".chunk";
    }

    public Chunk get(Vector3i id) {
        File f = new File(worldPath, getFileNameFor(id));

        if (!f.exists())
            return null;

        try {
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            Chunk result = (Chunk) in.readObject();

            in.close();
            fileIn.close();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void put(Chunk c) {
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

    @Override
    public boolean contains(Vector3i position) {
        File f = new File(worldPath, getFileNameFor(position));
        return f.exists();

    }

    @Override
    public float size() {
        return 0;
    }

    @Override
    public void dispose() {
    }
}

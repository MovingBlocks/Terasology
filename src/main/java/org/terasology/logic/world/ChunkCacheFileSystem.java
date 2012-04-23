package org.terasology.logic.world;

import org.terasology.game.Terasology;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChunkCacheFileSystem implements IChunkCache {
    private final LocalWorldProvider _parent;
    private Logger logger = Logger.getLogger(getClass().getName());

    public ChunkCacheFileSystem(LocalWorldProvider parent) {
        _parent = parent;
    }
    public Chunk get(int id) {
        File f = new File(_parent.getObjectSavePath() + "/" + Chunk.getChunkFileNameFromId(id));

        if (!f.exists())
            return null;

        try {
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            Chunk result = (Chunk) in.readObject();
            result.setParent(_parent);

            in.close();
            fileIn.close();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void put(Chunk c) {
        File dirPath = _parent.getObjectSavePath();
        if (!dirPath.exists()) {
            if (!dirPath.mkdirs()) {
                logger.log(Level.SEVERE, "Could not create save directory.");
                return;
            }
        }

        File f = new File(_parent.getObjectSavePath(), c.getChunkFileName());

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
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

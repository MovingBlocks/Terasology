package org.terasology.logic.world;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ChunkCacheGZip implements IChunkCache, Serializable {
    ConcurrentHashMap<Integer, byte[]> _map = new ConcurrentHashMap<Integer, byte[]>();
    int _sizeInByte = 0;

    public ChunkCacheGZip(){

    }

    public Chunk get(int id) {
        Chunk c = null;
        try {
            byte[] b = _map.get(id);
            if(b == null)
                return null;
            ByteArrayInputStream bais = new ByteArrayInputStream(b);
            GZIPInputStream gzipIn = new GZIPInputStream(bais);
            ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
            c = (Chunk) objectIn.readObject();
            objectIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return c;
    }

    public void put(Chunk c) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
            ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
            objectOut.writeObject(c);
            objectOut.close();
            byte[] b = baos.toByteArray();
            _sizeInByte += b.length;
            _map.put(c.getId(), b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float size() {
        return (float)_sizeInByte / (1<<20);
    }

    public void dispose() {

    }
}

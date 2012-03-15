package org.terasology.logic.world;


import org.terasology.math.Vector3i;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.*;

public class ChunkCacheDeflate implements IChunkCache, Serializable {
    ConcurrentMap<Integer, byte[]> _map = new ConcurrentHashMap<Integer, byte[]>();
    int _sizeInByte = 0;

    public ChunkCacheDeflate(){

    }

    public Chunk get(int id) {
        Chunk c = null;
        try {
            byte[] b = _map.get(id);
            if(b == null)
                return null;
            ByteArrayInputStream bais = new ByteArrayInputStream(b);
            InflaterInputStream gzipIn = new InflaterInputStream(bais);
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
            DeflaterOutputStream gzipOut = new DeflaterOutputStream(baos);
            ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
            objectOut.writeObject(c);
            objectOut.close();
            byte[] b = baos.toByteArray();
            _sizeInByte += b.length;
            _map.put(c.getId(), b);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public float size() {
        return (float)_sizeInByte /(1<<20);
    }

    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

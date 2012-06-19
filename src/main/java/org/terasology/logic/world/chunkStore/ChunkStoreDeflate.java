package org.terasology.logic.world.chunkStore;


import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.ChunkStore;
import org.terasology.math.Vector3i;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class ChunkStoreDeflate implements ChunkStore, Serializable {
    ConcurrentMap<Vector3i, byte[]> map = new ConcurrentHashMap<Vector3i, byte[]>();
    int _sizeInByte = 0;

    public ChunkStoreDeflate() {

    }

    public Chunk get(Vector3i id) {
        Chunk c = null;
        try {
            byte[] b = map.get(id);
            if (b == null)
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
            map.put(c.getPos(), b);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public boolean contains(Vector3i position) {
        return map.containsKey(position);
    }

    public float size() {
        return (float) _sizeInByte / (1 << 20);
    }

    public void dispose() {
    }
}

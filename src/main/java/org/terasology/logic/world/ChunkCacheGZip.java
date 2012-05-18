package org.terasology.logic.world;

import org.terasology.math.Vector3i;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ChunkCacheGZip implements IChunkCache, Serializable {
    ConcurrentHashMap<Vector3i, byte[]> _map = new ConcurrentHashMap<Vector3i, byte[]>();
    int _sizeInByte = 0;

    public static ChunkCacheGZip load(File file) throws IOException {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        try {
            fileIn = new FileInputStream(file);
            in = new ObjectInputStream(fileIn);
            return (ChunkCacheGZip) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to load chunk cache", e);
        }
        finally {
            // JAVA7 : cleanup
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Logger.getLogger(ChunkCacheGZip.class.getName()).log(Level.SEVERE, "Failed to close input stream", e);
                }
            }
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (IOException e) {
                    Logger.getLogger(ChunkCacheGZip.class.getName()).log(Level.SEVERE, "Failed to close input stream", e);
                }
            }
        }
    }

    public ChunkCacheGZip(){

    }

    public Chunk get(Vector3i id) {
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
            _map.put(c.getPos(), b);
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

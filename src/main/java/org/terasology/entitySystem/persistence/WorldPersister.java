package org.terasology.entitySystem.persistence;

import com.google.protobuf.TextFormat;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.protobuf.EntityData;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class WorldPersister {

    public enum SaveFormat {
        Binary {
            @Override
            void save(OutputStream out, EntityData.World world) throws IOException {
                world.writeTo(out);
                out.flush();
            }

            @Override
            EntityData.World load(InputStream in) throws IOException {
                return EntityData.World.parseFrom(in);
            }
        },
        Text {
            @Override
            void save(OutputStream out, EntityData.World world) throws IOException {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out));
                TextFormat.print(world, bufferedWriter);
                bufferedWriter.flush();
            }

            @Override
            EntityData.World load(InputStream in) throws IOException {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                EntityData.World.Builder builder = EntityData.World.newBuilder();
                TextFormat.merge(bufferedReader, builder);
                return builder.build();
            }
        },
        JSON {
            @Override
            void save(OutputStream out, EntityData.World world) throws IOException {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out));
                EntityDataJSONFormat.write(world, bufferedWriter);
                bufferedWriter.flush();
            }

            @Override
            EntityData.World load(InputStream in) throws IOException {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                return EntityDataJSONFormat.readWorld(bufferedReader);
            }
        };

        abstract void save(OutputStream out, EntityData.World world) throws IOException;

        abstract EntityData.World load(InputStream in) throws IOException;
    }

    private Logger logger = Logger.getLogger(getClass().getName());
    private EntityManager entityManager;
    private EntityPersisterHelper persisterHelper;

    public WorldPersister( EntityManager entityManager) {
        this.entityManager = entityManager;
        this.persisterHelper = new EntityPersisterHelperImpl((PersistableEntityManager) entityManager);
    }

    public void save(File file, SaveFormat format) throws IOException {
        final EntityData.World world = persisterHelper.serializeWorld();

        File parentFile = file.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(file);

        try {
            format.save(out, world);
        } finally {
            // JAVA7 : Replace with improved resource handling
            try {
                out.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to close file", e);
            }
        }
    }

    public void load(File file, SaveFormat format) throws IOException {
        entityManager.clear();

        FileInputStream in = new FileInputStream(file);
        EntityData.World world = null;
        try {
            world = format.load(in);
        } finally {
            // JAVA7: Replace with improved resource handling
            try {
                in.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to close file", e);
            }
        }

        if (world != null) {
            persisterHelper.deserializeWorld(world);
        }
    }
}
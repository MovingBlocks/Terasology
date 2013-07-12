package org.terasology.persistence.internal;

import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.persistence.GlobalStore;
import org.terasology.protobuf.EntityData;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * @author Immortius
 */
class GlobalStoreInternal implements GlobalStore {

    private GlobalStoreSaver globalStoreSaver;
    private StorageManagerInternal storageManager;

    public GlobalStoreInternal(GlobalStoreSaver globalStoreSave, StorageManagerInternal storageManager) {
        this.globalStoreSaver = globalStoreSave;
        this.storageManager = storageManager;
    }

    @Override
    public void store(EntityRef entity) {
        globalStoreSaver.store(entity);
    }

    @Override
    public void save() {
        EntityData.GlobalStore globalStoreData = globalStoreSaver.save();
        storageManager.store(globalStoreData);
    }
}

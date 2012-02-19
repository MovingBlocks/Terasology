package org.terasology.persistence;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.terasology.persistence.typeSupport.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PersistenceManager {
    
    public static final short END_OF_SECTION = 0;
    short nextId = 1;
    
    Map<String, TypeInfo> typeNameLookup = new HashMap<String, TypeInfo>();
    Map<Class, TypeInfo> typeLookup = new HashMap<Class, TypeInfo>();
    Map<String,Class<? extends Persistable>> persistableClasses = new HashMap<String,Class<? extends Persistable>>();

    public PersistenceManager() {
        registerType(new BooleanInfo());
        registerType(new DoubleInfo());
        registerType(new FloatInfo());
        registerType(new IntegerInfo());
        registerType(new ListInfo());
        registerType(new StringInfo());
        registerType(new Vector3dInfo());
    }
    
    public void registerType(TypeInfo<?> info) {
        info.setId(nextId++);
        typeNameLookup.put(info.getTypeName(), info);
        typeLookup.put(info.getType(), info);
    }
    
    public void registerPersistableClasses(Class<? extends Persistable> persistableClass) {
        persistableClasses.put(persistableClass.getName(), persistableClass);
    }
    
    public LevelReader newReader(InputStream in) throws IOException {
        return newReader(new DataInputStream(in));
    }

    public LevelReader newReader(DataInputStream in) throws IOException {
        return new BinaryLevelReader(in, typeNameLookup, persistableClasses);
    }
    
    public LevelWriter newWriter(OutputStream out) throws IOException {
        return newWriter(new DataOutputStream(out));
    }
    
    public LevelWriter newWriter(DataOutputStream out) throws IOException {
        return new BinaryLevelWriter(out, typeLookup, persistableClasses);
    }
    
    
}

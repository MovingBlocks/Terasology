/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence;

import org.terasology.persistence.interfaces.*;
import org.terasology.persistence.typeSupport.*;

import java.io.*;
import java.util.HashMap;
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

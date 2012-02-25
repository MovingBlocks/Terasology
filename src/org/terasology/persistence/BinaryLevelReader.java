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

import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import org.terasology.persistence.interfaces.*;
import org.terasology.persistence.typeSupport.UnknownTypeInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Immortius <immortius@gmail.com>
 */
public class BinaryLevelReader implements LevelReader, StorageReader {
    private Logger logger = Logger.getLogger(getClass().getName());
    private DataInputStream input;
    private TShortObjectMap<TypeInfo> typeLookup = new TShortObjectHashMap<TypeInfo>();
    private TShortObjectMap<Class<? extends Persistable>> persistableTypeLookup = new TShortObjectHashMap<Class<? extends Persistable>>();
    private Persistable nextPersistable;
    
    private List<Map<String,Object>> propStack = new ArrayList<Map<String, Object>>();
    
    public BinaryLevelReader(DataInputStream in, Map<String,TypeInfo> typeNameMap, Map<String, Class<? extends Persistable>> persistableTypeMap) throws IOException {
        this.input = in;
        readHeader(typeNameMap, persistableTypeMap);
    }
    
    private void readHeader(Map<String,TypeInfo> typeNameMap, Map<String, Class<? extends Persistable>> persistableTypeMap) throws IOException {
        if (input.readChar() != 'T' ||
            input.readChar() != 'E' ||
            input.readChar() != 'R' ||
            input.readChar() != 'A')
            throw new IOException("Not a valid binary file");
        
        int fileVersion = input.readInt();
        short numTypes = input.readShort();
        for (int i = 0; i < numTypes; ++i) {
            short typeId = input.readShort();
            byte size = input.readByte();
            String name = input.readUTF();

            TypeInfo info = typeNameMap.get(name);
            if (info != null && info.size() == size) {
                typeLookup.put(typeId, info);
            } else {
                if (info != null) {
                    logger.log(Level.SEVERE, String.format("Size mismatch for type %s, expected %d but found %d", name, info.size(), size));
                } else {
                    logger.log(Level.SEVERE, String.format("Unknown type %s", name));
                }
                if (size == TypeInfo.VARIABLE_LENGTH) {
                    // TODO: If possible support variable length types better
                    throw new IOException("Unknown variable length type, cannot continue data read");
                }
                typeLookup.put(typeId, new UnknownTypeInfo(size));
            }
        }
        short numPersistableTypes = input.readShort();
        for (int i = 0; i < numPersistableTypes; ++i) {
            short id =  input.readShort();
            String name = input.readUTF();
            Class<? extends Persistable> clazz = persistableTypeMap.get(name);
            if (clazz != null) {
                persistableTypeLookup.put(id, clazz);
            } else {
                logger.warning(String.format("Unknown persistable class \"%s\" will be skipped", name));
            }
        }

        loadNextPersistable();
    }
    
    public Persistable next() throws IOException {
        if (nextPersistable == null) return null;
        Persistable result = nextPersistable;
        loadNextPersistable();
        return result;
    }

    public boolean hasNext() {
        return nextPersistable != null;
    }

    public TypeInfo<?> getType(short id) {
        return typeLookup.get(id);
    }
    
    private Map<String, Object> properties() {
        return propStack.get(propStack.size() - 1);
    }
    
    public Object read(String name) {
        return properties().get(name);
    }
    
    public <T> T read(String name, Class<T> clazz) {
        Object item = properties().get(name);
        if (clazz.isInstance(item))
        {
            return clazz.cast(item);
        }
        return null;
    }
        
    public String readString(String name) {
        return read(name, String.class);
    }

    public Integer readInt(String name) {
        return read(name, Integer.class);
    }

    public int readInt(String name, int defaultVal) {
        Object item = properties().get(name);
        if (item instanceof Integer)
        {
            return (Integer) item;
        }
        return defaultVal;
    }

    public Float readFloat(String name) {
        return read(name, Float.class);
    }
    
    public float readFloat(String name, float defaultVal) {
        Object item = properties().get(name);
        if (item instanceof Float)
        {
            return (Float) item;
        }
        return defaultVal;
    }

    public Double readDouble(String name) {
        return read(name, Double.class);
    }

    public double readDouble(String name, double defaultVal) {
        Object item = properties().get(name);
        if (item instanceof Double)
        {
            return (Double) item;
        }
        return defaultVal;
    }

    public Boolean readBoolean(String name) {
        return read(name, Boolean.class);
    }
    
    public boolean readBoolean(String name, boolean defaultVal) {
        Object item = properties().get(name);
        if (item instanceof Boolean)
        {
            return (Boolean) item;
        }
        return defaultVal;
    }

    public <T> List<T> readList(String name, Class<T> type) {
        Object item = properties().get(name);
        if (item instanceof List)
        {
            Object sample = ((List)item).get(0);
            if (type.isInstance(sample))
            {
                return (List) item;
            }
        }
        return Arrays.asList();
    }

    private void loadNextPersistable() throws IOException {
        nextPersistable = null;
        while (nextPersistable == null) {
            short persistableId = input.readShort();
            if (persistableId == PersistenceManager.END_OF_SECTION) {
                return;
            }

            Class<? extends Persistable> persistableClass = persistableTypeLookup.get(persistableId);
            if (persistableClass == null) {
                // Consume unusable data
                while (true) {
                    short typeId = input.readShort();
                    if (typeId == PersistenceManager.END_OF_SECTION)
                        break;
                    TypeInfo info = typeLookup.get(typeId);
                    if (info == null) {
                        throw new IOException("Unexpected type: " + typeId);
                    }

                    input.readUTF();
                    try {
                        info.read(input, this);
                    } catch (Exception e) {
                        throw new IOException("Error reading data", e);
                    }
                }
            } else {
                propStack.add(new HashMap<String, Object>());
                // Load
                while (true) {
                    short typeId = input.readShort();
                    if (typeId == PersistenceManager.END_OF_SECTION)
                        break;
                    TypeInfo info = typeLookup.get(typeId);
                    if (info == null) {
                        throw new IOException("Unexpected type: " + typeId);
                    }
                    
                    String propName = input.readUTF();
                    try {
                        properties().put(propName, info.read(input, this));
                    } catch (Exception e) {
                        throw new IOException("Error reading data", e);
                    }
                }
                try {
                    nextPersistable = persistableClass.newInstance();
                    nextPersistable.retrieve(this);
                } catch (InstantiationException e) {
                    throw new IOException("Could not read persistable " + persistableClass.getName(), e);
                } catch (IllegalAccessException e) {
                    throw new IOException("Could not read persistable " + persistableClass.getName(), e);
                }

                propStack.remove(propStack.size() - 1);
            }

        }
    }

}

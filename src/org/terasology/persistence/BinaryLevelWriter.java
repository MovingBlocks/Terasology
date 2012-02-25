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

import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Immortius <immortius@gmail.com>
 */
public class BinaryLevelWriter implements LevelWriter, StorageWriter
{
    Logger logger = Logger.getLogger(getClass().getName());
    boolean errored = false;
    DataOutputStream output;
    Map<Class, TypeInfo> typeMap;
    TypeInfo<List> listInfo;
    Map<Class<? extends Persistable>, Short> persistableIdMap = new HashMap<Class<? extends Persistable>, Short>();
       
    public BinaryLevelWriter(DataOutputStream out, Map<Class, TypeInfo> typeMap, Map<String, Class<? extends Persistable>> persistableClasses) throws IOException {
        this.output = out;
        this.typeMap = typeMap;
        listInfo = typeMap.get(List.class);
        if (listInfo == null) throw new IOException("Required type for List is missing");
        short id = 1;
        for (Class<? extends Persistable> clazz : persistableClasses.values()) {
            persistableIdMap.put(clazz, id++);
        }
        
        writeHeader();
    }

    private void writeHeader() throws IOException {
        output.writeChar('T');
        output.writeChar('E');
        output.writeChar('R');
        output.writeChar('A');
        output.writeInt(0x1);
        // Write type info
        output.writeShort(typeMap.size());
        for (TypeInfo info : typeMap.values()) {
            output.writeShort(info.getId());
            output.writeByte(info.size());
            output.writeUTF(info.getTypeName());
        }
        // Write persistable info
        output.writeShort(persistableIdMap.size());
        for (Map.Entry<Class<? extends Persistable>, Short> item : persistableIdMap.entrySet()) {
            output.writeShort(item.getValue());
            output.writeUTF(item.getKey().getName());
        }
    }

    public boolean isInErrorState() {
        return errored;
    }

    public <T> void write(String name, T value)  {
        if (value != null)
        {
            try {
                TypeInfo info = typeMap.get(value.getClass());
                if (info == null) {
                    throw new IOException("Unsupported type : " + value.getClass());
                } else {
                    output.writeShort(info.getId());
                    output.writeUTF(name);
                    info.write(output, value, this);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error writing data", ex);
                errored = true;
            }
        }
    }

    public <T> void write(String name, List<T> value) {
        if (value != null && value.size() > 0)
        {
            try
            {
                // It is assumed that all values in the list are of the same type
                T sample = value.get(0);
                TypeInfo info = typeMap.get(sample.getClass());
                if (info == null)
                {
                    throw new IOException("Unsupported type : " + sample.getClass());
                }
                output.writeShort(listInfo.getId());
                output.writeUTF(name);
                output.writeShort(info.getId());
                output.writeInt(value.size());
                for (T val : value)
                {
                    info.write(output, val, this);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error writing data", ex);
                errored = true;
            }
        }
    }

    public void write(Persistable value) throws IOException {
        Short id = persistableIdMap.get(value.getClass());
        if (id != null) {
            output.writeShort(id);
            value.store(this);
            output.writeShort(PersistenceManager.END_OF_SECTION);
        } else {
            logger.log(Level.SEVERE, "Error writing data, unknown persistable type: " + value.getClass().getName());
            throw new IOException("Error writing data, unknown persistable type: " + value.getClass().getName());
        }
    }

    public void close() throws IOException {
        output.writeShort(PersistenceManager.END_OF_SECTION);
        try {
            output.flush();
            output.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error writing data", ex);
            errored = true;
        }
        
    }
        
}

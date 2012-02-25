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
package org.terasology.persistence.typeSupport;

import org.terasology.persistence.interfaces.LevelReader;
import org.terasology.persistence.interfaces.LevelWriter;
import org.terasology.persistence.interfaces.TypeInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Dummy Unknown type used by the reader to eat unknown info.
 * @author Immortius <immortius@gmail.com>
 */
public class UnknownTypeInfo implements TypeInfo {
    private byte size;
    
    public UnknownTypeInfo(byte size) {
        this.size = size;
    }
    
    public void write(DataOutputStream out, Object value, LevelWriter writer) throws Exception {
        
    }

    public Object read(DataInputStream in, LevelReader reader) throws Exception {
        byte[] result = new byte[size];
        in.readFully(result);
        return result;
    }

    public short getId() {
        return 0;
    }

    public void setId(short id) {
    }

    public Class getType() {
        return byte[].class;
    }

    public String getTypeName() {
        return "Unknown";
    }

    public byte size() {
        return size;
    }
}

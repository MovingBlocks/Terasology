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
package org.terasology.persistence.interfaces;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface TypeInfo<T> {
    public static final byte VARIABLE_LENGTH = 0;

    void write(DataOutputStream out, T value, LevelWriter writer) throws Exception;
    T read(DataInputStream in, LevelReader reader) throws Exception;
    
    short getId();
    void setId(short id);
    Class<T> getType();
    String getTypeName();

    /**
     * @return The size of this type in bytes
     */
    byte size();
}

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

import org.terasology.persistence.interfaces.TypeInfo;

/**
 * @author Immortius <immortius@gmail.com>
 */
public abstract class AbstractTypeInfo<T> implements TypeInfo<T> {
    private byte size;
    private short id;
    private Class<T> type;

    public AbstractTypeInfo(Class<T> type, byte size) {
        this.size = size;
        this.type = type;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public byte size() {
        return size;
    }

    public Class<T> getType() {
        return type;
    }

    public String getTypeName() {
        return type.getSimpleName();
    }
}

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ListInfo extends AbstractTypeInfo<List> {

    public ListInfo() {
        super(List.class, TypeInfo.VARIABLE_LENGTH);
    }

    public void write(DataOutputStream out, List value, LevelWriter writer) throws Exception {
        throw new UnsupportedOperationException("This should be implemented by LevelWriter");
    }

    public List read(DataInputStream in, LevelReader reader) throws Exception {
        List list = new ArrayList();
        TypeInfo contentsType = reader.getType(in.readShort());
        if (contentsType == null)
        {
            throw new IOException("Unknown object type: " + contentsType);
        }
        int size = in.readInt();
        for (int i = 0; i < size; i++)
        {
            list.add(contentsType.read(in, reader));
        }
        return list;
    }
}

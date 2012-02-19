package org.terasology.persistence.typeSupport;

import org.terasology.persistence.LevelReader;
import org.terasology.persistence.LevelWriter;
import org.terasology.persistence.TypeInfo;

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

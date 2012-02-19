/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terasology.persistence;

import java.io.IOException;

/**
 *
 * @author Immortius
 */
public interface LevelReader extends StorageReader {
    
    TypeInfo<?> getType(short id);
    Persistable next() throws IOException;
    boolean hasNext();

}

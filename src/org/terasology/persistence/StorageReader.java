/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terasology.persistence;

import java.util.List;

/**
 * 
 * @author Immortius
 */
public interface StorageReader {
    Object read(String name);
    <T> T read(String name, Class<T> clazz);
    <T> List<T> readList(String name, Class<T> type);

    String readString(String name);
    Integer readInt(String name);
    int readInt(String name, int defaultVal);
    Float readFloat(String name);
    float readFloat(String name, float defaultVal);
    Double readDouble(String name);
    double readDouble(String name, double defaultVal);
    Boolean readBoolean(String name);
    boolean readBoolean(String name, boolean defaultVal);


}

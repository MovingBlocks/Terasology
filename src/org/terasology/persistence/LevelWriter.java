/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terasology.persistence;


import java.io.IOException;

/**
 * Interface for persister
 * @author Immortius
 */
public interface LevelWriter {

    void write(Persistable value) throws IOException;
    boolean isInErrorState();
    void close() throws IOException;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terasology.persistence;

/**
 *
 * @author Immortius
 */
public interface Persistable {
    void store(StorageWriter writer);
    void retrieve(StorageReader reader);
}

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
public interface StorageWriter {
    <T> void write(String name, T value);
    <T> void write(String name, List<T> value);
}

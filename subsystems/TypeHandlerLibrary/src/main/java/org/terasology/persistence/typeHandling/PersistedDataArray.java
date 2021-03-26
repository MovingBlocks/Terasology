// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;

import java.util.List;

/**
 */
public interface PersistedDataArray extends PersistedData, Iterable<PersistedData> {

    int size();

    PersistedData getArrayItem(int index);

    boolean isNumberArray();

    boolean isBooleanArray();

    boolean isStringArray();

    List<String> getAsStringArray();

    TDoubleList getAsDoubleArray();

    TFloatList getAsFloatArray();

    TIntList getAsIntegerArray();

    TLongList getAsLongArray();

    boolean[] getAsBooleanArray();

    List<PersistedData> getAsValueArray();

}

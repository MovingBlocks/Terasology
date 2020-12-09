// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory.arrays;

import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.inMemory.PersistedDouble;
import org.terasology.persistence.typeHandling.inMemory.PersistedFloat;
import org.terasology.persistence.typeHandling.inMemory.PersistedInteger;
import org.terasology.persistence.typeHandling.inMemory.PersistedLong;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class TroveUtils {

    private TroveUtils() {

    }

    public static TFloatList intToFloat(TIntList list) {
        TFloatList result = new TFloatArrayList(list.size());
        TIntIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            int i = iterator.next();
            result.add(i);
        }
        return result;
    }

    public static TFloatList longToFloat(TLongList list) {
        TFloatList result = new TFloatArrayList(list.size());
        TLongIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            long i = iterator.next();
            result.add(i);
        }
        return result;
    }

    public static TFloatList doubleToFloat(TDoubleList list) {
        TFloatList result = new TFloatArrayList(list.size());
        TDoubleIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            double i = iterator.next();
            result.add((float) i);
        }
        return result;
    }

    public static TDoubleList intToDouble(TIntList list) {
        TDoubleList result = new TDoubleArrayList(list.size());
        TIntIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            int i = iterator.next();
            result.add(i);
        }
        return result;
    }

    public static TDoubleList longToDouble(TLongList list) {
        TDoubleList result = new TDoubleArrayList(list.size());
        TLongIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            long i = iterator.next();
            result.add(i);
        }
        return result;
    }

    public static TDoubleList floatToDouble(TFloatList list) {
        TDoubleList result = new TDoubleArrayList(list.size());
        TFloatIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            float i = iterator.next();
            result.add(i);
        }
        return result;
    }

    public static TLongList intToLong(TIntList list) {
        TLongList result = new TLongArrayList(list.size());
        TIntIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            int i = iterator.next();
            result.add(i);
        }
        return result;
    }

    public static TLongList floatToLong(TFloatList list) {
        TLongList result = new TLongArrayList(list.size());
        TFloatIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            float i = iterator.next();
            result.add((long) i);
        }
        return result;
    }

    public static TLongList doubleToLong(TDoubleList list) {
        TLongList result = new TLongArrayList(list.size());
        TDoubleIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            double i = iterator.next();
            result.add((long) i);
        }
        return result;
    }

    public static TIntList floatToInt(TFloatList list) {
        TIntList result = new TIntArrayList(list.size());
        TFloatIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            float i = iterator.next();
            result.add((int) i);
        }
        return result;
    }

    public static TIntList longToInt(TLongList list) {
        TIntList result = new TIntArrayList(list.size());
        TLongIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            long i = iterator.next();
            result.add((int) i);
        }
        return result;
    }

    public static TIntList doubleToInt(TDoubleList list) {
        TIntList result = new TIntArrayList(list.size());
        TDoubleIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            double i = iterator.next();
            result.add((int) i);
        }
        return result;
    }

    public static Iterator<PersistedData> iteratorFrom(TIntList list) {
        return new Iterator<PersistedData>() {
            private final TIntIterator iterator = list.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public PersistedData next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new PersistedInteger(iterator.next());
            }
        };
    }

    public static Iterator<PersistedData> iteratorFrom(TLongList list) {
        return new Iterator<PersistedData>() {
            private final TLongIterator iterator = list.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public PersistedData next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new PersistedLong(iterator.next());
            }
        };
    }

    public static Iterator<PersistedData> iteratorFrom(TFloatList list) {
        return new Iterator<PersistedData>() {
            private final TFloatIterator iterator = list.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public PersistedData next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new PersistedFloat(iterator.next());
            }
        };
    }

    public static Iterator<PersistedData> iteratorFrom(TDoubleList list) {
        return new Iterator<PersistedData>() {
            private final TDoubleIterator iterator = list.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public PersistedData next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new PersistedDouble(iterator.next());
            }
        };
    }
}

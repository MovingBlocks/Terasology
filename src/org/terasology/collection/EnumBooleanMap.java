package org.terasology.collection;

/**
 * EnumMap for storing primitive booleans against each enum value.
 * Values default to false
 * @author Immortius <immortius@gmail.com>
 */
public class EnumBooleanMap<ENUM extends Enum> {
    private boolean[] store;
    
    public EnumBooleanMap(Class<ENUM> enumClass)
    {
        store = new boolean[enumClass.getEnumConstants().length];
    }

    public int size() {
        return store.length;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean containsKey(ENUM key) {
        return true;
    }

    public boolean get(ENUM key) {
        return store[key.ordinal()];
    }

    public Boolean put(ENUM key, boolean value) {
        boolean old = store[key.ordinal()];
        store[key.ordinal()] = value;
        return old;
    }

    /**
     * Sets all values to false
     */
    public void clear() {
        for (int i = 0; i < store.length; ++i)
        {
            store[i] = false;
        }
    }
}

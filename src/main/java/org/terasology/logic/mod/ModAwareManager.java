package org.terasology.logic.mod;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.reflections.Reflections;
import org.terasology.game.CoreRegistry;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Helper class to create "manager" classes, that maps an URI to an concrete item.
 * Items are found by scanning through all mods.
 * Which item belongs to which mod is stored for.
 *
 * @author synopia
 */
public abstract class ModAwareManager<T, U> {
    private Map<U,T> uriToItem = Maps.newHashMap();
    private List<T> itemList = Lists.newArrayList();
    private Map<U, String> uriToMod = Maps.newHashMap();

    protected ModAwareManager() {
        refresh();
    }

    /**
     * @return all found items
     */
    public List<T> listItems() {
        return Lists.newArrayList(itemList);
    }

    /**
     * @return the item associated with the uri
     */
    public T getItem(U uri) {
        return uriToItem.get(uri);
    }

    /**
     * @return the mod a given item belongs to
     */
    public String getMod(U uri) {
        return uriToMod.get(uri);
    }

    /**
     * Rescan all mods for defined classes (getClasses())
     */
    public void refresh() {
        CoreRegistry.get(ModManager.class).getAllReflections(); // this line is necessary, otherwise no classes are found in mods
        itemList.clear();
        uriToItem.clear();
        uriToMod.clear();

        refreshMod(null);
        ModManager modManager = CoreRegistry.get(ModManager.class);
        for (Mod mod : modManager.getMods()) {
            if( mod.isCodeMod() ) {
                refreshMod(mod);
            }
        }

        Collections.sort(itemList, getItemComparator());
    }

    /**
     * @return the uri that defines the given item
     */
    protected abstract U getUri( T item );

    /**
     * @return a list of classes that subtypes of are to be searched
     */
    protected abstract List<Class> getClasses();

    /**
     * @return a comparator that defines the order in which listItems() should return the items
     */
    protected abstract Comparator<T> getItemComparator();

    private void refreshMod(Mod mod) {
        Set<Class<?>> generatorClasses = Sets.newHashSet();
        Reflections reflections;
        if( mod!=null ) {
            reflections = mod.getReflections();
        } else {
            reflections = CoreRegistry.get(ModManager.class).getEngineReflections();
        }

        for (Class<?> cls : getClasses()) {
            generatorClasses.addAll(reflections.getSubTypesOf(cls));
        }
        for (Class<?> generatorClass : generatorClasses) {
            try {
                if(!generatorClass.isInterface() && !Modifier.isAbstract(generatorClass.getModifiers())) {
                    T item = (T) generatorClass.newInstance();
                    U uri = getUri(item);
                    itemList.add(item);
                    uriToItem.put(uri, item);
                    if( mod!=null ) {
                        uriToMod.put(uri, mod.getModInfo().getId());
                    }
                }
            } catch (InstantiationException e) {
//                logger.warn("Could not get map generator "+generatorClass.getName());
            } catch (IllegalAccessException e) {
//                logger.warn("Could not get map generator " + generatorClass.getName());
            }
        }
    }
}

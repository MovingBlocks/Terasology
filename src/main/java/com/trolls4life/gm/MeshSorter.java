package com.trolls4life.gm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import javax.vecmath.Vector3f;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.LocalPlayer;

/**
 * This data structure takes Entities with a location in the world and sorts
 * them based on their distance to the player.
 *
 * The sorting is done asynchronous from the usages of the data, meaning there
 * is no performance loss if an additional cpu-core is present.
 * 
 * The container is not thread safe other than the sorting process.
 * Making it thread safe would be easy but can give a minot performance loss.
 * Simply add the synchronized keyword to all public methods that do not have it 
 * already.
 *
 * @author Rednax
 */
public class MeshSorter implements Iterable<EntityRef> {
    private LinkedList<EntityRef> entities = new LinkedList<EntityRef>();
    private LocalPlayer lp;
    private List<Command> commands = new ArrayList();
    private Timer timer = new Timer();
    private SortTask sortingTask;
    private Vector3f playerPos = null;
    private DistanceComparator comparator = new DistanceComparator();

    public MeshSorter(LocalPlayer localPlayer) {
        lp = localPlayer;
        sortingTask = new SortTask();
        timer.schedule(sortingTask, 0);
    }

    /**
     * Returns the amount of elements in this list.
     *
     * @return
     */
    public synchronized int size() {
        return entities.size();
    }

    /**
     *
     * @return true if there are no elements in this container.
     */
    public boolean isEmpty() {
        return entities.isEmpty();
    }

    public boolean contains(EntityRef e) {
        if (!e.hasComponent(LocationComponent.class)) {
            throw new IllegalArgumentException("Trying to find an Entity without"
                    + " a location component in a container that only stores entities with a location component?!");
        }
        return entities.contains(e);
    }

    /**
     * Add an Entity with a LocationComponent to this container. Note that it will be inserted, rather
     * than appended. So untill a new sorting pass has been made, this new entity
     * is returned whenever entities are requested from this container.
     * @param e The entity to add. Must have a LocationComponent or an 
     * IlligalArgumentException is thrown.
     */
    public synchronized void add(EntityRef e) {
        if (!e.hasComponent(LocationComponent.class)) {
            throw new IllegalArgumentException("Trying to add an Entity without"
                    + " a location component to a container that only stores entities with a location component?!");
        }
        //new entities are inserted to make sure that new entities are drawn first.
        //Since it is likely the players wants to see new entities over existing ones
        //And it is likely new entities spawn neat the player.
        entities.add(0, e);
        //TODO add command
        commands.add(new AddCommand(e));
    }

    /**
     * Remove an entity from this container.
     * @param e the entity to remove. Must have a LocationComponent or an 
     * IlligalArgumentException is thrown. If the entity does not have a 
     * LocationComponent it cannot reside in this container.
     */
    public synchronized void remove(EntityRef e) {
        if (!e.hasComponent(LocationComponent.class)) {
            throw new IllegalArgumentException("Trying to remove an Entity without"
                    + " a location component from a container that only stores entities with a location component?!");
        }
        entities.remove(e);
        //TODO add command
        commands.add(new RemoveCommand(e));
    }

    /**
     * Removes all elements from this container.
     */
    public synchronized void clear() {
        entities.clear();
        //TODO add command
        commands.add(new ClearCommand());
    }

    /**
     * Warning: this method is memory intensive, as the list is copied.
     * The copying is required to ensure thread safety.
     * Returns a normal iterator over all Entities in this collection. While
     * this class attempts to keep the elements sorted based on the distance to
     * the player. This is not guaranteed. The sorting tries to put closed
     * objects on a lower index, hence they will returned first by this
     * iterator.
     *
     * @return An Iterator over all Entities in this collection.
     */
    @Override
    public Iterator<EntityRef> iterator() {
        return cloneEntities().iterator();
    }

    /**
     * Warning: this method is memory intensive, as the list is copied.
     * The copying is required to ensure thread safety.
     * Similar to iterator(), but this version returns a ListIterator, which has
     * some additional functionality.
     *
     * @return A ListIterator over all Entities in this collection.
     */
    public ListIterator<EntityRef> listIterator() {
        return cloneEntities().listIterator();
    }
    
    /**
     * Returns a copy of the entities in this container.
     * Although it is not guaranteed the list is sorted, attempts have been made to
     * put entities nearer to the player at a lower index.
     * @return a list with all entities in this container.
     */
    public LinkedList<EntityRef> getEntities() {
        return cloneEntities();
    }
    
    /**
     * Fills the given array with Entities from this container. Attempts
     * are made to put the Entities nearest to the player in this array and 
     * nearer entities are expected, but not guaranteed to be at a lower index.
     * 
     * This is the most memory friendly way to obtain elements from this 
     * container.
     * 
     * @param output The array to fill with entities from this container.
     * @return The amount of entities that were put into the array. If there 
     * are less entities in this container than the size of output, this
     * number will be this.size(). Otherwise it will be output.length
     */
    public synchronized int getNearest(EntityRef[] output) {
        int size = Math.min(size(), output.length);
        Iterator<EntityRef> iter = entities.iterator();
        for(int x = 0; x < size; x++) {
            output[x] = iter.next();
        }
        return size;
    }
    
    /**
     * Returns the entities that are expected to be the nearest to the 
     * player. It is not guaranteed they are the nearest entities though.
     * @param count the number of entities to return.
     * @return An array with Entities. Attempts have been made to put the 
     * Entities that are closer to the player at a lower index. 
     * The size of this array equals min(count, size()).
     */
    public EntityRef[] getNearest(int count) {
        EntityRef[] output = new EntityRef[Math.min(count, size())];
        getNearest(output);
        return output;
    }
    
    /**
     * Checks if this container is still in a proper state. 
     * @return Should always return true, unless there is a bug in this class.
     */
    public boolean invariantHolds() {
        for(EntityRef e : cloneEntities()) {
            if(e.getComponent(LocationComponent.class) == null) {
                return false;
            }
        }
        return true;
    }

    private synchronized LinkedList<EntityRef> cloneEntities() {
        return (LinkedList<EntityRef>) entities.clone();
    }

    /**
     * Sorts the entities of this container. Can be executed concurrently
     * with the other operations on this container.
     */
    private void sort() {
        lp.getPosition(playerPos);
        /**
         * Note that while cloneEntities() and processQueue are synchronized,
         * this method and the sorting are not. This means that the actual
         * sorting can be done concurrently.
         */
        LinkedList<EntityRef> newEnts = cloneEntities();
        Collections.sort(newEnts, comparator);
        processQueue(newEnts);
    }
    
    /**
     * Updates the sorted list with all changes made while sorting
     * before swapping the lists.
     * @param newEntities the newly sorted list with entities.
     */
    private synchronized void processQueue(LinkedList<EntityRef> newEntities) {
        //Note that the commands are executed in the order they are added to the
        //list.
        for(Command c : commands) {
            c.executeOn(newEntities);
        }
        entities = newEntities;
    }

    private interface Command {
        public void executeOn(List<EntityRef> entities);
    }
    
    private class AddCommand implements Command {

        private EntityRef toAdd;
        public AddCommand(EntityRef toAdd) {
            this.toAdd = toAdd;
        }
        
        @Override
        public void executeOn(List<EntityRef> entities) {
            entities.add(0, toAdd);
        }
        
    }
    
    private class RemoveCommand implements Command {

        private EntityRef toRem;
        public RemoveCommand(EntityRef toRemove) {
            toRem = toRemove;
        }
        
        @Override
        public void executeOn(List<EntityRef> entities) {
            entities.remove(toRem);
        }
        
    }
    
    private class ClearCommand implements Command {

        @Override
        public void executeOn(List<EntityRef> entities) {
            entities.clear();
        }
        
    }

    private class DistanceComparator implements Comparator<EntityRef> {

        @Override
        public int compare(EntityRef o1, EntityRef o2) {
            LocationComponent loc1 = o1.getComponent(LocationComponent.class);
            LocationComponent loc2 = o2.getComponent(LocationComponent.class);
            
            Vector3f temp = null;
            
            loc1.getWorldPosition(temp);
            temp.sub(playerPos);
            float dis1 = temp.lengthSquared();
            
            loc2.getWorldPosition(temp);
            temp.sub(playerPos);
            float dis2 = temp.lengthSquared();
            
            if(dis1 < dis2) {
                return -1;
            } else if (dis2 < dis1) {
                return 1;
            } else { //dis1 == dis2
                return 0;
            }
        }
    }

    private class SortTask extends TimerTask {
        private long lastSortMoment = -1;
        private static final long SORT_DELAY_MILLIES = 200;

        @Override
        public void run() {
            sort();
            long finishTime = System.currentTimeMillis();
            long delay = SORT_DELAY_MILLIES - finishTime;
            if (delay < 0) {
                delay = 0;
            }
            timer.schedule(this, delay);
        }
    }
}

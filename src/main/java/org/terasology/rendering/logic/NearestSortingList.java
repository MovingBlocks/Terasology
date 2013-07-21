/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.logic;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;

/**
 * This data structure takes Entities with a location in the world and sorts
 * them based on their distance to the player.
 *
 * The sorting is done asynchronous from the usages of the data, meaning there
 * is no performance loss if an additional cpu-core is present.
 *
 * When retrieving Entities from this container, no guarantees are given on the
 * sorting of the entities. This class only tries to keep the elements sorted,
 * but does not guarantee it.
 *
 * It it therefor use full for keeping track of the entities with a Mesh
 * attached to them to keep track of what Entities to draw. The advantage is
 * that the sorting does not cause a performance issue, since it is done
 * asynchronously from the main thread.
 *
 * @author XanHou
 */
public class NearestSortingList implements Iterable<EntityRef> {
    private static final Logger logger = LoggerFactory.getLogger(NearestSortingList.class);
    private LinkedList<EntityRef> entities = new LinkedList<EntityRef>();
    private LocalPlayer lp;
    private final List<Command> commands = new ArrayList();
    //This timer is a simple java timer since the scheduling functionality is used
    //Not the timing functionality, which should be done through the LWJGL/TS timer.
    private Timer timer;
    private SortTask sortingTask;
    private Thread sortingThread;
    private Vector3f playerPos = new Vector3f();
    private DistanceComparator comparator = new DistanceComparator();
    /**
     * True while the background sorting process is active, different from the
     * active boolean because active is also true when a sorting run is
     * scheduled.
     */
    private boolean sorting = false;
    /**
     * When the background process that sorts the elements is running, this
     * boolean is true.
     */
    private boolean active = false;
    /**
     * The delay in ms to wait between each sorting run.
     */
    private long sortDelayMS = 50;
    /**
     * Used for temp storage to reduce memory load.
     */
    private static final Vector3f temp = new Vector3f();

    /**
     * Returns the amount of elements in this list.
     *
     * @return
     */
    public int size() {
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
        return entities.contains(e);
    }

    /**
     * Add an Entity with a LocationComponent to this container. Note that it
     * will be inserted, rather than appended. So untill a new sorting pass has
     * been made, this new entity is returned whenever entities are requested
     * from this container.
     *
     * @param e The entity to add. Must have a LocationComponent or an
     *          IlligalArgumentException is thrown.
     */
    public synchronized void add(EntityRef e) {
        if(e.getComponent(LocationComponent.class) == null) {
            logger.warn("Ädding entity without LocationComponent to Container that sorts on Location");
        }
        //new entities are inserted to make sure that new entities are drawn first.
        //Since it is likely the players wants to see new entities over existing ones
        //And it is likely new entities spawn near the player.
        entities.add(0, e);
        if (sorting) {
            commands.add(new AddCommand(e));
        }
    }

    /**
     * Remove an entity from this container.
     *
     * @param e the entity to remove. Must have a LocationComponent or an
     *          IlligalArgumentException is thrown. If the entity does not have a
     *          LocationComponent it cannot reside in this container.
     */
    public synchronized void remove(EntityRef e) {
        entities.remove(e);
        if (sorting) {
            commands.add(new RemoveCommand(e));
        }
    }

    /**
     * Removes all elements from this container.
     */
    public synchronized void clear() {
        entities.clear();
        if (sorting) {
            //There is no need to execute all additions and removals if the list
            //will be cleared, so we can safely clearn the pending commands.
            commands.clear();
            commands.add(new ClearCommand());
        }
    }

    /**
     * Warning: this method is memory intensive, as the list is copied. The
     * copying is required to ensure thread safety. Returns a normal iterator
     * over all Entities in this collection. While this class attempts to keep
     * the elements sorted based on the distance to the player. This is not
     * guaranteed. The sorting tries to put closed objects on a lower index,
     * hence they will returned first by this iterator.
     *
     * @return An Iterator over all Entities in this collection.
     */
    @Override
    public Iterator<EntityRef> iterator() {
        return cloneEntities().iterator();
    }

    /**
     * Warning: this method is memory intensive, as the list is copied. The
     * copying is required to ensure thread safety. Similar to iterator(), but
     * this version returns a ListIterator, which has some additional
     * functionality.
     *
     * @return A ListIterator over all Entities in this collection.
     */
    public ListIterator<EntityRef> listIterator() {
        return cloneEntities().listIterator();
    }

    /**
     * Returns a copy of the entities in this container. Although it is not
     * guaranteed the list is sorted, attempts have been made to put entities
     * nearer to the player at a lower index.
     *
     * @return a list with all entities in this container.
     */
    public LinkedList<EntityRef> getEntities() {
        return cloneEntities();
    }

    /**
     * Fills the given array with Entities from this container. Attempts are
     * made to put the Entities nearest to the player in this array and nearer
     * entities are expected, but not guaranteed to be at a lower index.
     *
     * This is the most memory friendly way to obtain elements from this
     * container.
     *
     * @param output The array to fill with entities from this container.
     *
     * @return The amount of entities that were put into the array. If there are
     *         less entities in this container than the size of output, this
     *         number will be this.size(). Otherwise it will be output.length
     */
    public synchronized int getNearest(EntityRef[] output) {
        int size = Math.min(size(), output.length);
        Iterator<EntityRef> iter = entities.iterator();
        for (int x = 0; x < size; x++) {
            output[x] = iter.next();
        }
        return size;
    }

    /**
     * Returns the entities that are expected to be the nearest to the player.
     * It is not guaranteed they are the nearest entities though.
     *
     * @param count the number of entities to return.
     *
     * @return An array with Entities. Attempts have been made to put the
     *         Entities that are closer to the player at a lower index. The size
     *         of this array equals min(count, size()).
     */
    public EntityRef[] getNearest(int count) {
        EntityRef[] output = new EntityRef[Math.min(count, size())];
        getNearest(output);
        return output;
    }

    /**
     * Calling this method starts the background sorting. If never called, the
     * elements in this container are never sorted!
     */
    public synchronized void initialize() {
        timer = new Timer();
        sortingTask = new SortTask();
        timer.scheduleAtFixedRate(sortingTask, sortDelayMS, sortDelayMS);
        active = true;
    }

    /**
     * Stops the background sorting without deleting clearing this container.
     * This is required for proper clean-up.
     *
     * Note that if a sorting process is running while this method is called,
     * the sorting process finishes sorting this method will wait for it to
     * finish. Afterwards the sorting is not scheduled again until the
     * initialize method is called again.
     *
     * Note that calling stop() and clear() can be done in any order and the
     * specified behaviour will be exactly the same. If there is a difference it
     * is an insignificant performance loss or win if.
     */
    public synchronized void stop() {
        timer.cancel();
        timer.purge();
        if(sortingThread == null) {
            timer = null;
        } else {
            try {
                sortingThread.join();
            } catch (InterruptedException ex) {
                logger.error("Joining of sorting thread was interrupted!");
            }
            timer = null;
        }
    }

    /**
     * Although it has the exact same function as getEntries(), it reads easier
     * inside this class when the word 'clone' is used, rather than 'get'.
     *
     * @return A copy of the entities in this container.
     */
    private synchronized LinkedList<EntityRef> cloneEntities() {
        return (LinkedList<EntityRef>) entities.clone();
    }

    /**
     * These two actions needed to happen atomically and the easier method was
     * to put them in a synchronized method.
     *
     * @return cloneEntities()
     */
    private synchronized LinkedList<EntityRef> cloneAndSetSorting() {
        sorting = true;
        return cloneEntities();
    }

    /**
     * Sorts the entities of this container. Can be executed concurrently with
     * the other operations on this container.
     */
    private void sort() {
        lp.getPosition(playerPos);
        if(!commands.isEmpty()) {
            logger.warn("The commands list was not emptied properly!");
            commands.clear();
        }
        /**
         * Note that while cloneAndSetSorting() and processQueue() are
         * synchronized, this method itself and the sorting are not. This means
         * that the actual sorting can be done concurrently with any other
         * operations.
         */
        LinkedList<EntityRef> newEnts = cloneAndSetSorting();
        
        //System.err.println("sorting " + newEnts.size() + " etities based on playerloc: " + playerPos.x + ", "+playerPos.y+ ", "+playerPos.z);
        
        try {
            Collections.sort(newEnts, comparator);
        } catch (IllegalArgumentException ex) {
            logger.warn("Entities destroyed during sorting process. Sorting is skipped this round.");
            clearQueue();
            return;
        }
        processQueue(newEnts);
    }

    /**
     * Updates the sorted list with all changes made while sorting before
     * swapping the lists.
     *
     * @param newEntities the newly sorted list with entities.
     */
    private synchronized void processQueue(LinkedList<EntityRef> newEntities) {
        //Note that the commands are executed in the order they are added to the
        //list.
        for (Command c : commands) {
            //System.err.println("Executing " + c.getClass().getSimpleName());
            c.executeOn(newEntities);
        }
        commands.clear();
        entities = newEntities;
        sorting = false;
    }
    
    /**
     * Clear the command queue in a synchronized way. Used when the sorting
     * fails.
     */
    private synchronized void clearQueue() {
        commands.clear();
    }

    /**
     * The commands are used to store addition, removal and clear operations
     * when the background process is sorting the entities.
     */
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

    /**
     * Comparator that compares the distances to the player of two Entities.
     * Closer is smaller, hence return -1, which results in lower index for
     * closer the object when sorting.
     */
    private class DistanceComparator implements Comparator<EntityRef> {
        
        @Override
        public int compare(EntityRef o1, EntityRef o2) {
            LocationComponent loc1 = o1.getComponent(LocationComponent.class);
            LocationComponent loc2 = o2.getComponent(LocationComponent.class);

            if(loc1 == null || loc2 == null) {
                return 0;
            } else if(loc1 == null) {
                return 1;
            } else if (loc2 == null) {
                return -1;
            }
            loc1.getWorldPosition(temp);
            temp.sub(playerPos);
            float dis1 = temp.lengthSquared();

            loc2.getWorldPosition(temp);
            temp.sub(playerPos);
            float dis2 = temp.lengthSquared();

            if (dis1 < dis2) {
                return -1;
            } else if (dis2 < dis1) {
                return 1;
            } else { //dis1 == dis2
                return 0;
            }
        }
    }

    /**
     * The TimerTask that does the sorting work. TODO stop the scheduling when
     * the container is supposed to be destroyed.
     */
    private class SortTask extends TimerTask {
        private long lastSortMoment = -1;

        @Override
        public void run() {
            try {
                LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
                lp = localPlayer;
                if(localPlayer != null) {
                    sort();
                }
            } catch (Exception ex) {
                /**
                 * We don't want the failure of the sorter to cause the entire
                 * game to crash. Instead we shall output an error to the
                 * logger and continue.
                 */
                logger.error("Uncaught exception in sorting thread: " + ex.toString());
            }
        }
    }
}

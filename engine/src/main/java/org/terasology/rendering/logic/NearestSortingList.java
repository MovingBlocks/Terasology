/*
 * Copyright 2013 MovingBlocks
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

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.DistanceComparator;
import org.terasology.logic.location.LocationComponent;
import org.terasology.rendering.cameras.Camera;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This data structure takes Entities with a location in the world and sorts
 * them based on their distance to an other entity.
 * <br><br>
 * The sorting is done in a background thread.
 * <br><br>
 * When retrieving Entities from this container, no guarantees are given on the
 * sorting of the entities. This class only tries to keep the elements sorted,
 * but does not guarantee it.
 * <br><br>
 * It it therefor use full for graphics purposes, to keep track of the nearest
 * entities to draw.
 *
 */
public class NearestSortingList implements Iterable<EntityRef> {
    private static final Logger logger = LoggerFactory.getLogger(NearestSortingList.class);

    private List<EntityRef> entities = Lists.newLinkedList();
    private final List<Command> commands = Lists.newArrayList();

    private Timer timer;
    private SortTask sortingTask;
    private Thread sortingThread;

    /**
     * True while the background sorting process is active.
     */
    private boolean sorting;

    /**
     * The delay in ms to wait between each sorting run.
     */
    private long sortPeriod = 50;

    /**
     * Default value is 50 milliseconds.
     *
     * @return the amount of milliseconds between the start of two consecutive
     * sorting runs. (Unless sorting takes longer than this value in ms.)
     */
    public long getSortPeriod() {
        return sortPeriod;
    }

    /**
     * @return the amount of elements in this list.
     */
    public int size() {
        return entities.size();
    }

    /**
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
     * will be inserted, rather than appended. So until a new sorting pass has
     * been made, this new entity is returned whenever entities are requested
     * from this container.
     *
     * @param e The entity to add. Must have a LocationComponent or an
     *          IlligalArgumentException is thrown.
     */
    public synchronized void add(EntityRef e) {
        if (e.getComponent(LocationComponent.class) == null) {
            logger.warn("Adding entity without LocationComponent to container that sorts on location. Entity: {}", e);
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
     *          IllegalArgumentException is thrown. If the entity does not have a
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
     * guaranteed. The sorting tries to put closer objects on a lower index,
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
    public List<EntityRef> getEntities() {
        return cloneEntities();
    }

    /**
     * Fills the given array with Entities from this container. Attempts are
     * made to put the Entities nearest to the player in this array and nearer
     * entities are expected, but not guaranteed to be at a lower index.
     * <br><br>
     * This is the most memory friendly way to obtain elements from this
     * container.
     *
     * @param output The array to fill with entities from this container.
     * @return The amount of entities that were put into the array. If there are
     * less entities in this container than the size of output, this
     * number will be this.size(). Otherwise it will be output.length
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
     * @return An array with Entities. Attempts have been made to put the
     * Entities that are closer to the player at a lower index. The size
     * of this array equals min(count, size()).
     */
    public EntityRef[] getNearest(int count) {
        EntityRef[] output = new EntityRef[Math.min(count, size())];
        getNearest(output);
        return output;
    }

    /**
     * Calling this method starts the background sorting. If never called, the
     * elements in this container are never sorted!
     *
     * @param origin The entity to sort around. When using the getNearest
     *               methods, this container has tried to put entities nearer to the entity
     *               given here at a lower index.
     */
    public synchronized void initialise(Camera origin) {
        initialise(origin, 50, 0);
    }

    /**
     * Initializes the background sorting without starting the sorting
     * yet. This can later be done with the continueSorting() method.
     *
     * @param origin The entity to sort around. When using the getNearest
     *               methods, this container has tried to put entities nearer to the entity
     *               given here at a lower index.
     */
    public synchronized void initialiseAndPause(Camera origin) {
        if (sortingTask != null || timer != null) {
            logger.error("Mis-usages of initialise detected! Initialising again"
                    + " before stopping the sorting process. Sorting is "
                    + "stopped now, but it should be done by the user of "
                    + "this class.");
            stop();
        }
        sortingTask = new SortTask(origin);
    }

    /**
     * Same as initialise(), but allows the user to specify an amount
     * of milliseconds to wait before the first sorting run.
     *
     * @param origin
     * @param period       The minimum time between sorts.
     * @param initialDelay delay before the first sorting run. The frequency of
     *                     sorting runs can be set with the setSortDelayMS(long) method.
     */
    public synchronized void initialise(Camera origin, long period, long initialDelay) {
        if (sortingTask != null || timer != null) {
            logger.error("Mis-usages of initialise detected! Initialising again"
                    + " before stopping the sorting process. Sorting is "
                    + "stopped now, but it should be done by the user of "
                    + "this class.");
            stop();
        }
        sortPeriod = period;
        timer = new Timer();
        sortingTask = new SortTask(origin);
        timer.scheduleAtFixedRate(sortingTask, initialDelay, sortPeriod);
    }

    /**
     * @return true if this container has been initialised, false otherwise. Initialised containers have started sorting.
     */
    public boolean isInitialised() {
        return sortingTask != null;
    }

    /**
     * Stops the background sorting without deleting clearing this container.
     * This is required for proper clean-up.
     * <br><br>
     * Note that if a sorting process is running while this method is called,
     * the sorting process finishes sorting this method will wait for it to
     * finish. Afterwards the sorting is not scheduled again until the
     * initialize method is called again.
     * <br><br>
     * Note that calling stop() and clear() can be done in any order and the
     * specified behaviour will be exactly the same. If there is a difference it
     * is an insignificant performance loss or win if.
     */
    public synchronized void stop() {
        if (timer != null) {
            //stopping a paused instance
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if (sortingThread != null) {
            try {
                sortingThread.join();
            } catch (InterruptedException ex) {
                logger.error("Joining of sorting thread was interrupted!");
            }
        }
        sortingThread = null;
        sortingTask = null;
    }

    /**
     * Although it has the exact same function as getEntries(), it reads easier
     * inside this class when the word 'clone' is used, rather than 'get'.
     *
     * @return A copy of the entities in this container.
     */
    private synchronized List<EntityRef> cloneEntities() {
        return Lists.newLinkedList(entities);
    }

    /**
     * These two actions needed to happen atomically and the easier method was
     * to put them in a synchronized method.
     *
     * @return cloneEntities()
     */
    private synchronized List<EntityRef> cloneAndSetSorting() {
        sorting = true;
        return cloneEntities();
    }

    /**
     * Updates the sorted list with all changes made while sorting before
     * swapping the lists.
     *
     * @param newEntities the newly sorted list with entities. All changes will
     *                    be applied to this list before the entities of this
     *                    collection are set to this list.
     */
    private synchronized void processQueueAndSetEntities(List<EntityRef> newEntities) {
        //Note that the commands are executed in the order they are added to the list.
        for (Command c : commands) {
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
        void executeOn(List<EntityRef> entities);
    }

    private static class AddCommand implements Command {
        private EntityRef toAdd;

        public AddCommand(EntityRef toAdd) {
            this.toAdd = toAdd;
        }

        @Override
        public void executeOn(List<EntityRef> entities) {
            entities.add(0, toAdd);
        }
    }

    private static class RemoveCommand implements Command {
        private EntityRef toRem;

        public RemoveCommand(EntityRef toRemove) {
            toRem = toRemove;
        }

        @Override
        public void executeOn(List<EntityRef> entities) {
            entities.remove(toRem);
        }
    }

    private static class ClearCommand implements Command {
        @Override
        public void executeOn(List<EntityRef> entities) {
            entities.clear();
        }
    }

    /**
     * The TimerTask that does the sorting work.
     */
    private class SortTask extends TimerTask {
        private DistanceComparator comparator = new DistanceComparator();
        private Camera originCamera;

        /**
         * @param origin The entities of a NearestSortingCollection will be
         *               sorted based on their distance to this entity.
         */
        public SortTask(Camera origin) {
            originCamera = origin;
        }

        @Override
        public void run() {
            sortingThread = Thread.currentThread();
            try {
                sort();
            } catch (Exception ex) {
                /**
                 * We don't want the failure of the sorter to cause the entire
                 * game to crash. Instead we shall output an error to the logger
                 * and continue.
                 */
                logger.error("Uncaught exception in sorting thread: " + ex.toString());
            }
        }

        /**
         * Sorts the entities of this container. Can be executed concurrently
         * with the other operations on this container.
         */
        private void sort() {
            comparator.setOrigin(originCamera.getPosition());
            if (!commands.isEmpty()) {
                logger.warn("The commands list was not emptied properly!");
                commands.clear();
            }

            /**
             * Note that while cloneAndSetSorting() and
             * processQueueAndSetEntities() are synchronized, this method itself
             * and the sorting are not. This means that the actual sorting can
             * be done concurrently with any other operations.
             */
            List<EntityRef> newEnts = cloneAndSetSorting();

            try {
                Collections.sort(newEnts, comparator);
            } catch (IllegalArgumentException ex) {
                logger.warn("Entities destroyed during sorting process. Sorting is skipped this round.");
                clearQueue();
                return;
            }
            processQueueAndSetEntities(newEnts);
        }
    }
}

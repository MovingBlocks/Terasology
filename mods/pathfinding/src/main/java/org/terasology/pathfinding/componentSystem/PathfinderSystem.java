package org.terasology.pathfinding.componentSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.components.world.WorldComponent;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.impl.SingleThreadMonitor;
import org.terasology.pathfinding.components.StartBlockComponent;
import org.terasology.pathfinding.components.TargetBlockComponent;
import org.terasology.pathfinding.model.HeightMap;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.pathfinding.rendering.InfoGrid;
import org.terasology.world.BlockChangedEvent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.chunks.ChunkReadyEvent;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author synopia
 */
@RegisterComponentSystem
public class PathfinderSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(PathfinderSystem.class);

    public interface Callback {
        void onPathReady( Path path );
        void onPathInvalided( Vector3i start, Vector3i end );
    }

    public abstract class Task implements Comparable<Task> {
        public abstract void process();
        public abstract int monitorId();
        public void kill(){}
        @Override
        public int compareTo(Task o) {
            return this.monitorId()<o.monitorId() ? -1 : this.monitorId()>o.monitorId() ? 1 : 0;
        }
    }

    public class UpdateChunkTask extends Task {
        public Vector3i chunkPos;

        private UpdateChunkTask(Vector3i chunkPos) {
            this.chunkPos = chunkPos;
        }

        @Override
        public void process() {
            maps.remove(chunkPos);
            HeightMap map = updateMap(chunkPos);
            maps.put(chunkPos, map);
        }

        @Override
        public int monitorId() {
            return 0;
        }

    }

    public class FindPathTask extends Task {
        public Vector3i start;
        public Vector3i end;
        public Path path;
        public Callback callback;
        public boolean observed;

        private FindPathTask(Vector3i start, Vector3i end, boolean observed, Callback callback ) {
            this.start = start;
            this.end = end;
            this.callback = callback;
            this.observed = observed;
        }

        @Override
        public void process() {
            path = findPath(start, end);

            outputQueue.offer(this);
        }

        public void enqueue() {
            inputQueue.offer(this);
            callback.onPathInvalided(start, end);
        }

        @Override
        public void kill() {
            observed = false;
            inputQueue.remove(this);
            observedPaths.remove(this);
        }

        @Override
        public int monitorId() {
            return 1;
        }
    }

    private BlockingQueue<Task> inputQueue = new PriorityBlockingQueue<Task>(100);
    private BlockingQueue<FindPathTask> outputQueue = new ArrayBlockingQueue<FindPathTask>(100);
    private Set<FindPathTask> observedPaths = new HashSet<FindPathTask>();

    private ExecutorService inputThreads;

    private Map<Vector3i, HeightMap> maps = new HashMap<Vector3i, HeightMap>();
    private Pathfinder pathfinder;
    @In
    private WorldProvider world;

    public PathfinderSystem() {
        CoreRegistry.put(PathfinderSystem.class, this);
    }

    public void requestPath( Vector3i start, Vector3i end, Callback callback ) {
        inputQueue.offer(new FindPathTask(start, end, false, callback));
    }

    public FindPathTask observePath( Vector3i start, Vector3i end, Callback callback ) {
        FindPathTask task = new FindPathTask(start, end, true, callback);
        inputQueue.offer(task);
        return task;
    }

    public HeightMap getHeightMap( Vector3i chunkPos ) {
        return maps.get(chunkPos);
    }

    @Override
    public void initialise() {
        pathfinder = new Pathfinder(world);
        logger.info("Pathfinder started");

        inputThreads = Executors.newFixedThreadPool(1);
        inputThreads.execute(new Runnable() {
            @Override
            public void run() {
                final SingleThreadMonitor monitor = ThreadMonitor.create("Pathfinder.Requests", "Update", "FindPath");
                try {
                    boolean running = true;
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    while (running) {
                        try {
                            Task task = inputQueue.take();
                            task.process();
                            monitor.increment(task.monitorId());
                            logger.info("-- Observed Paths: "+observedPaths.size()+" --");
                        } catch (InterruptedException e) {
                            monitor.addError(e);
                            logger.error("Thread interrupted", e);
                        } catch (Exception e) {
                            monitor.addError(e);
                            logger.error("Error in thread", e);
                        }
                    }
                    logger.debug("Thread shutdown safely");
                } finally {
                    monitor.setActive(false);
                }
            }
        });
    }

    @Override
    public void update(float delta) {
        while (!outputQueue.isEmpty()) {
            FindPathTask task = outputQueue.poll();
            task.callback.onPathReady(task.path);
            if( task.observed ) {
                observedPaths.add(task);
            }
        }
    }

    @Override
    public void shutdown() {

    }

    private Path findPath(Vector3i startPos, Vector3i targetPos) {
        WalkableBlock start = pathfinder.getBlock(startPos);
        WalkableBlock target = pathfinder.getBlock(targetPos);
        Path path = null;
        if( start!=null && target!=null ) {
            path = pathfinder.findPath(target, start);
        }
        return path;
    }

    private HeightMap updateMap(Vector3i targetPos) {
        HeightMap map = pathfinder.update(targetPos);
        for (FindPathTask path : observedPaths) {
            path.enqueue();
        }
        return map;
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void blockChanged(BlockChangedEvent event, EntityRef entity) {
        inputQueue.offer(new UpdateChunkTask(TeraMath.calcChunkPos(event.getBlockPosition())));
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void chunkReady(ChunkReadyEvent event, EntityRef worldEntity) {
        Vector3i chunkPos = new Vector3i(event.getChunkPos());
        inputQueue.offer(new UpdateChunkTask(chunkPos));
    }
}

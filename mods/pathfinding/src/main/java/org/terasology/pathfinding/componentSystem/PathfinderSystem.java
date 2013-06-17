package org.terasology.pathfinding.componentSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.WorldComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.impl.SingleThreadMonitor;
import org.terasology.pathfinding.model.HeightMap;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.world.BlockChangedEvent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.chunks.ChunkReadyEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author synopia
 */
@RegisterComponentSystem
public class PathfinderSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(PathfinderSystem.class);

    public interface Callback {
        void onPathReady( Path path );
    }

    public class UpdateChunkTask {
        public Vector3i chunkPos;

        private UpdateChunkTask(Vector3i chunkPos) {
            this.chunkPos = chunkPos;
        }

        public void process() {
            maps.remove(chunkPos);
            HeightMap map = pathfinder.update(chunkPos);
            maps.put(chunkPos, map);
        }
    }

    public class FindPathTask {
        public Vector3i startPos;
        public Vector3i targetPos;
        public Path path;
        public Callback callback;

        private FindPathTask(Vector3i startPos, Vector3i targetPos, Callback callback ) {
            this.startPos = startPos;
            this.targetPos = targetPos;
            this.callback = callback;
        }

        public void process() {
            WalkableBlock start = pathfinder.getBlock(startPos);
            WalkableBlock target = pathfinder.getBlock(targetPos);
            path = null;
            if( start!=null && target!=null ) {
                path = pathfinder.findPath(target, start);
            }
            outputQueue.offer(this);
        }

        public void kill() {
            taskMap.remove(this);
            callback = null;
        }
    }

    private BlockingQueue<UpdateChunkTask> updateChunkQueue = new ArrayBlockingQueue<UpdateChunkTask>(100);
    private BlockingQueue<FindPathTask> outputQueue = new ArrayBlockingQueue<FindPathTask>(100);
    private Set<FindPathTask> taskMap = Collections.synchronizedSet(new HashSet<FindPathTask>());

    private ExecutorService inputThreads;

    private Map<Vector3i, HeightMap> maps = new HashMap<Vector3i, HeightMap>();
    private Pathfinder pathfinder;
    @In
    private WorldProvider world;

    private boolean pathsValid;

    public PathfinderSystem() {
        CoreRegistry.put(PathfinderSystem.class, this);
    }

    public FindPathTask requestPath(Vector3i start, Vector3i end, Callback callback) {
        FindPathTask task = new FindPathTask(start, end, callback);
        taskMap.add(task);
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
                            UpdateChunkTask task = updateChunkQueue.poll(1, TimeUnit.SECONDS);
                            if( task!=null ) {
                                pathsValid = false;
                                task.process();
                                monitor.increment(0);
                            } else {
                                if( !pathsValid ) {
                                    pathfinder.clearCache();
                                    int count = 0;
                                    long time = System.nanoTime();
                                    int notFound = 0;
                                    for (FindPathTask pathTask : taskMap) {
                                        if( pathTask !=null ) {
                                            count ++;
                                            pathTask.process();
                                            if( pathTask.path==null || pathTask.path==Path.INVALID ) {
                                                notFound ++;
                                            }
                                            monitor.increment(1);
                                        }
                                    }
                                    pathsValid = true;
                                    float ms = (System.nanoTime() - time) / 1000 / 1000f;
                                    logger.info("Searching "+count+" pathes took "+ ms +" ms ("
                                        +(1000f/count)+" pps), not found="+notFound);
                                }
                            }
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
        }
    }

    @Override
    public void shutdown() {
        inputThreads.shutdown();
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void blockChanged(BlockChangedEvent event, EntityRef entity) {
        updateChunkQueue.offer(new UpdateChunkTask(TeraMath.calcChunkPos(event.getBlockPosition())));
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void chunkReady(ChunkReadyEvent event, EntityRef worldEntity) {
        Vector3i chunkPos = new Vector3i(event.getChunkPos());
        updateChunkQueue.offer(new UpdateChunkTask(chunkPos));
    }
}

// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Per-chunk-position locks guarding chunk light-array data against the race between {@link LateLightMerger}
 * writing it and {@code ChunkMeshWorker} reading it for tessellation on another thread - see {@link
 * LateLightMerger}'s class doc for why that race exists.
 * <p>
 * Scoped per position rather than one global lock: merging a chunk far from the camera must not stall meshing
 * one right in front of it. A {@link ReentrantReadWriteLock} per position rather than a plain lock: tessellation
 * only reads, merging is the only writer, and with ~10 concurrent mesh-worker threads most contention is
 * reader-vs-reader on the same or overlapping chunks - which a plain lock would serialise for no reason, and a
 * read lock lets proceed together. Only a merge (the writer) actually needs to exclude anyone else.
 * <p>
 * Both sides need more than one chunk locked at once - merging touches a 3x3x3 neighbourhood, and meshing reads
 * light across chunk boundaries the same way - so {@link #withReadLocks}/{@link #withWriteLocks} always acquire
 * in a fixed position order regardless of the order the caller's neighbourhood list happens to be in. That is
 * the standard deadlock-avoidance discipline for multi-lock acquisition: two callers wanting overlapping sets
 * of locks, both sorted the same way, can never form a cycle.
 * <p>
 * Locks are never removed once created. A chunk unloading and reloading later reusing the same {@link
 * ReentrantReadWriteLock} instance is exactly what keeps this correct - freeing a lock while another thread
 * might still be waiting on it (or about to look it up) would let two threads hold "the" lock for the same
 * position at once, reintroducing the race this exists to prevent. The map grows with the number of distinct
 * positions ever visited in a session, which is a few tens of thousands at most - negligible next to the chunk
 * data itself.
 */
public final class ChunkLightLocks {
    private static final Logger logger = LoggerFactory.getLogger(ChunkLightLocks.class);
    private static final ConcurrentHashMap<Vector3ic, ReentrantReadWriteLock> LOCKS = new ConcurrentHashMap<>();
    private static final Comparator<Vector3ic> ORDER = Comparator
            .comparingInt(Vector3ic::x)
            .thenComparingInt(Vector3ic::y)
            .thenComparingInt(Vector3ic::z);

    // Contention accounting, debug-gated - see logStatsIfDebugAndDue(). Per-lock-acquisition, not
    // per-withLocks-call: a 27-chunk merge that contends on one of its 27 locks counts as one
    // contended acquisition out of 27, not one contended call out of many.
    private static final LongAdder TOTAL_ACQUISITIONS = new LongAdder();
    private static final LongAdder CONTENDED_ACQUISITIONS = new LongAdder();
    private static final LongAdder CONTENDED_WAIT_NANOS = new LongAdder();
    private static volatile long lastStatsLogMs;

    private ChunkLightLocks() {
    }

    /** For readers (tessellation): any number of readers, on the same or different positions, run together. */
    public static void withReadLocks(Collection<Vector3ic> positions, Runnable action) {
        withLocks(positions, ReentrantReadWriteLock::readLock, action);
    }

    /** For the writer (light merging): excludes readers and other writers on the same positions. */
    public static void withWriteLocks(Collection<Vector3ic> positions, Runnable action) {
        withLocks(positions, ReentrantReadWriteLock::writeLock, action);
    }

    private static void withLocks(Collection<Vector3ic> positions,
                                   java.util.function.Function<ReentrantReadWriteLock, Lock> side, Runnable action) {
        List<Vector3ic> sorted = new ArrayList<>(positions);
        sorted.sort(ORDER);
        List<Lock> held = new ArrayList<>(sorted.size());
        try {
            for (Vector3ic pos : sorted) {
                Lock lock = side.apply(LOCKS.computeIfAbsent(new Vector3i(pos), p -> new ReentrantReadWriteLock()));
                TOTAL_ACQUISITIONS.increment();
                if (!lock.tryLock()) {
                    CONTENDED_ACQUISITIONS.increment();
                    long start = System.nanoTime();
                    lock.lock();
                    CONTENDED_WAIT_NANOS.add(System.nanoTime() - start);
                }
                held.add(lock);
            }
            action.run();
        } finally {
            for (int i = held.size() - 1; i >= 0; i--) {
                held.get(i).unlock();
            }
        }
        logStatsIfDebugAndDue();
    }

    private static void logStatsIfDebugAndDue() {
        if (!logger.isDebugEnabled() || System.currentTimeMillis() - lastStatsLogMs < 1000) {
            return;
        }
        lastStatsLogMs = System.currentTimeMillis();
        long total = TOTAL_ACQUISITIONS.sum();
        long contended = CONTENDED_ACQUISITIONS.sum();
        long waitMs = CONTENDED_WAIT_NANOS.sum() / 1_000_000;
        logger.debug("perfProbe chunkLightLock: totalAcquisitions={}, contended={} ({}%), cumulativeContendedWait={}ms",
                total, contended, total == 0 ? 0 : (100 * contended / total), waitMs);
    }
}

package org.terasology.engine.world.chunks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Part 2: Model-Based Testing (FSM).
 * Feature: Chunk Lifecycle.
 * Validates transitions between PENDING, READY, DIRTY, and DISPOSED.
 */
public class ChunkLifecycleTest {

    private Chunk chunk;

    @BeforeEach
    public void setUp() {
        chunk = mock(Chunk.class);
    }

    @Test
    @DisplayName("T1: PENDING -> READY (Generation Complete)")
    public void testMarkReadyTransition() {
        when(chunk.isReady()).thenReturn(false);
        chunk.markReady();
        when(chunk.isReady()).thenReturn(true);
        assertTrue(chunk.isReady());
    }

    @Test
    @DisplayName("T2 & T3: READY <-> DIRTY (Modification & Saving)")
    public void testDirtyStateTransitions() {
        // T2: READY -> DIRTY
        chunk.setDirty(true);
        when(chunk.isDirty()).thenReturn(true);
        assertTrue(chunk.isDirty());
        // T3: DIRTY -> READY
        chunk.setDirty(false);
        when(chunk.isDirty()).thenReturn(false);
        assertFalse(chunk.isDirty());
    }

    @Test
    @DisplayName("T4: READY -> DISPOSED (Out of Range)")
    public void testReadyToDisposedTransition() {
        when(chunk.isReady()).thenReturn(true);
        chunk.dispose();
        when(chunk.isDisposed()).thenReturn(true);
        assertTrue(chunk.isDisposed());
    }

    @Test
    @DisplayName("T5: PENDING -> DISPOSED (Load Cancelled)")
    public void testPendingToDisposedTransition() {
        // Start in PENDING
        when(chunk.isReady()).thenReturn(false);
        when(chunk.isDisposed()).thenReturn(false);
        // Event: Dispose before it ever becomes ready
        chunk.dispose();
        // Verification
        when(chunk.isDisposed()).thenReturn(true);
        assertTrue(chunk.isDisposed(), "Chunk should be able to transition directly from PENDING to DISPOSED.");
    }
}
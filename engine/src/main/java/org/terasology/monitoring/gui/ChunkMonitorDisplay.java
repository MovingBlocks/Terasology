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
package org.terasology.monitoring.gui;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.chunk.ChunkMonitor;
import org.terasology.monitoring.chunk.ChunkMonitorEntry;
import org.terasology.monitoring.chunk.ChunkMonitorEvent;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.chunks.Chunk;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("serial")
public class ChunkMonitorDisplay extends JPanel {

    public static final Color COLOR_COMPLETE = new Color(0, 38, 28);
    public static final Color COLOR_INTERNAL_LIGHT_GENERATION_PENDING = new Color(4, 76, 41);
    public static final Color COLOR_ADJACENCY_GENERATION_PENDING = new Color(150, 237, 137);

    public static final Color COLOR_HIGHLIGHT_TESSELLATION = Color.blue.brighter().brighter();

    public static final Color COLOR_SELECTED_CHUNK = new Color(255, 102, 0);

    public static final Color COLOR_DEAD = Color.lightGray;
    public static final Color COLOR_INVALID = Color.red;

    private static final Logger logger = LoggerFactory.getLogger(ChunkMonitorDisplay.class);

    private final EventBus eventbus = new EventBus("ChunkMonitorDisplay");
    private final List<ChunkMonitorEntry> chunks = Lists.newArrayList();
    private final Map<Vector3i, ChunkMonitorEntry> map = Maps.newHashMap();
    private final ImageBuffer image = new ImageBuffer();

    private int refreshInterval;
    private int centerOffsetX;
    private int centerOffsetY;
    private int offsetX;
    private int offsetY;
    private int chunkSize;
    private int renderY;
    private int minRenderY;
    private int maxRenderY;
    private boolean followPlayer = true;

    private Vector3i selectedChunk;

    private final BlockingQueue<Request> queue = new LinkedBlockingQueue<>();
    private final transient ExecutorService executor;
    private final transient Runnable renderTask;

    public ChunkMonitorDisplay(int refreshInterval, int chunkSize) {
        Preconditions.checkArgument(refreshInterval >= 500, "Parameter 'refreshInterval' has to be greater or equal 500 (" + refreshInterval + ")");
        Preconditions.checkArgument(chunkSize >= 6, "Parameter 'chunkSize' has to be greater or equal 6 (" + chunkSize + ")");
        addComponentListener(new ResizeListener());
        final MouseInputListener ml = new MouseInputListener();
        addMouseListener(ml);
        addMouseMotionListener(ml);
        addMouseWheelListener(ml);
        this.refreshInterval = refreshInterval;
        this.chunkSize = chunkSize;
        this.executor = Executors.newSingleThreadExecutor();
        this.renderTask = new RenderTask();
        ChunkMonitor.registerForEvents(this);
        queue.offer(new InitialRequest());
        executor.execute(renderTask);
    }

    private void fireChunkSelectedEvent(Vector3i pos) {
        eventbus.post(new ChunkMonitorDisplayEvent.Selected(this, pos, pos == null ? null : map.get(pos)));
    }

    private Vector3i mouseToChunkPos(Point p) {
        Preconditions.checkNotNull(p, "The parameter 'p' must not be null");
        int x = (p.x - centerOffsetX - offsetX) / chunkSize;
        int z = (p.y - centerOffsetY - offsetY) / chunkSize;
        return new Vector3i(x - 1, renderY, z);
    }

    private void updateDisplay() {
        queue.offer(new RenderRequest());
    }

    private void updateDisplay(boolean fastResume) {
        queue.offer(new RenderRequest(fastResume));
    }

    private void recomputeRenderY() {
        int min = 0;
        int max = 0;
        int y = renderY;
        for (ChunkMonitorEntry chunk : chunks) {
            final Vector3i pos = chunk.getPosition();
            if (pos.y < min) {
                min = pos.y;
            }
            if (pos.y > max) {
                max = pos.y;
            }
        }
        if (y < min) {
            y = min;
        }
        if (y > max) {
            y = max;
        }
        minRenderY = min;
        maxRenderY = max;
        renderY = y;
    }

    private Vector3i calcPlayerChunkPos() {
        final LocalPlayer p = CoreRegistry.get(LocalPlayer.class);
        if (p != null) {
            return ChunkMath.calcChunkPos(new Vector3i(p.getPosition()));
        }
        return null;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public ChunkMonitorDisplay setChunkSize(int value) {
        if (value != chunkSize) {
            Preconditions.checkArgument(value >= 6, "Parameter 'value' has to be greater or equal 6 (" + value + ")");
            chunkSize = value;
            updateDisplay(true);
        }
        return this;
    }

    public Vector3i getSelectedChunk() {
        if (selectedChunk == null) {
            return null;
        }
        return new Vector3i(selectedChunk);
    }

    public ChunkMonitorDisplay setSelectedChunk(Vector3i chunk) {
        if (selectedChunk == null) {
            if (chunk != null) {
                selectedChunk = chunk;
                updateDisplay(true);
                fireChunkSelectedEvent(chunk);
            }
        } else {
            if (chunk == null || !selectedChunk.equals(chunk)) {
                selectedChunk = chunk;
                updateDisplay(true);
                fireChunkSelectedEvent(chunk);
            }
        }
        return this;
    }

    public int getRenderY() {
        return renderY;
    }

    public int getMinRenderY() {
        return minRenderY;
    }

    public int getMaxRenderY() {
        return maxRenderY;
    }

    public ChunkMonitorDisplay setRenderY(int value) {
        int clampedValue = value;
        if (value < minRenderY) {
            clampedValue = minRenderY;
        }
        if (value > maxRenderY) {
            clampedValue = maxRenderY;
        }
        if (renderY != clampedValue) {
            renderY = clampedValue;
            updateDisplay(true);
        }
        return this;
    }

    public ChunkMonitorDisplay setRenderYDelta(int delta) {
        return setRenderY(renderY + delta);
    }

    public boolean getFollowPlayer() {
        return followPlayer;
    }

    public ChunkMonitorDisplay setFollowPlayer(boolean value) {
        if (followPlayer != value) {
            followPlayer = value;
            updateDisplay();
        }
        return this;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public ChunkMonitorDisplay setOffset(int x, int y) {
        if (offsetX != x || offsetY != y) {
            this.offsetX = x;
            this.offsetY = y;
            updateDisplay(true);
        }
        return this;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public ChunkMonitorDisplay setRefreshInterval(int value) {
        Preconditions.checkArgument(value >= 500, "Parameter 'value' has to be greater or equal 500 (" + value + ")");
        this.refreshInterval = value;
        return this;
    }

    public void registerForEvents(Object object) {
        Preconditions.checkNotNull(object, "The parameter 'object' must not be null");
        eventbus.register(object);
    }

    @Subscribe
    public void receiveChunkEvent(ChunkMonitorEvent event) {
        if (event != null) {
            queue.offer(new ChunkRequest(event));
        }
    }

    @Override
    public void paint(Graphics g) {
        if (!image.render(g, 0, 0)) {
            super.paint(g);
        }
    }

    private static class ImageBuffer {

        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private int width;
        private int height;
        private BufferedImage imageA;
        private BufferedImage imageB;

        public ImageBuffer(int width, int height) {
            resize(width, height);
        }

        public ImageBuffer() {
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public Graphics2D getGraphics() {
            lock.readLock().lock();
            try {
                if (imageB != null) {
                    return (Graphics2D) imageB.getGraphics();
                }
            } finally {
                lock.readLock().unlock();
            }
            return null;
        }

        public void resize(int newWidth, int hewHeight) {
            lock.writeLock().lock();
            try {
                this.width = newWidth;
                this.height = hewHeight;
                if (newWidth < 1 || hewHeight < 1) {
                    imageB = null;
                } else if (imageB == null || newWidth != imageB.getWidth() || hewHeight != imageB.getHeight()) {
                    imageB = new BufferedImage(newWidth, hewHeight, BufferedImage.TYPE_INT_ARGB);
                }
            } catch (Exception e) {
                imageB = null;
                logger.error("Error allocating background buffer for chunk monitor display", e);
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void swap() {
            lock.writeLock().lock();
            try {
                final BufferedImage tmp = imageA;
                imageA = imageB;
                imageB = tmp;
                resize(width, height);
            } finally {
                lock.writeLock().unlock();
            }
        }

        public boolean render(Graphics g, int x, int y) {
            lock.readLock().lock();
            try {
                if (imageA != null) {
                    g.drawImage(imageA, x, y, null);
                    return true;
                }
            } finally {
                lock.readLock().unlock();
            }
            return false;
        }
    }

    private interface Request {

        String getName();

        boolean isChunkEvent();

        boolean needsRendering();

        boolean fastResume();

        void execute();
    }

    private abstract class UpdateRequest implements Request {

        @Override
        public boolean isChunkEvent() {
            return false;
        }
    }

    private class RenderRequest extends UpdateRequest {

        private final boolean fastResume;

        public RenderRequest(boolean fastResume) {
            this.fastResume = fastResume;
        }

        public RenderRequest() {
            this.fastResume = false;
        }

        @Override
        public void execute() {
        }

        @Override
        public String getName() {
            return "Render Request";
        }

        @Override
        public boolean needsRendering() {
            return true;
        }

        @Override
        public boolean fastResume() {
            return fastResume;
        }
    }

    private class InitialRequest extends UpdateRequest {

        @Override
        public void execute() {
            ChunkMonitor.getChunks(chunks);
            recomputeRenderY();
        }

        @Override
        public String getName() {
            return "Initial Request";
        }

        @Override
        public boolean needsRendering() {
            return true;
        }

        @Override
        public boolean fastResume() {
            return false;
        }
    }

    private class ResizeRequest extends UpdateRequest {

        public final int width;
        public final int height;

        public ResizeRequest(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void execute() {
            image.resize(width, height);
            centerOffsetX = width / 2 - chunkSize / 2;
            centerOffsetY = height / 2 - chunkSize / 2;
        }

        @Override
        public String getName() {
            return "Resize Request";
        }

        @Override
        public boolean needsRendering() {
            return true;
        }

        @Override
        public boolean fastResume() {
            return true;
        }
    }

    private class ChunkRequest implements Request {

        public final ChunkMonitorEvent event;

        public ChunkRequest(ChunkMonitorEvent event) {
            Preconditions.checkNotNull(event, "The parameter 'event' must not be null");
            this.event = event;
        }

        @Override
        public String getName() {
            return "Chunk Request";
        }

        @Override
        public boolean isChunkEvent() {
            return true;
        }

        @Override
        public void execute() {
            if (event instanceof ChunkMonitorEvent.ChunkProviderInitialized) {
                chunks.clear();
                map.clear();
                ChunkMonitor.getChunks(chunks);
                for (ChunkMonitorEntry e : chunks) {
                    map.put(e.getPosition(), e);
                }
                recomputeRenderY();
            } else if (event instanceof ChunkMonitorEvent.ChunkProviderDisposed) {
                chunks.clear();
                map.clear();
                recomputeRenderY();
            } else if (event instanceof ChunkMonitorEvent.BasicChunkEvent) {
                final ChunkMonitorEvent.BasicChunkEvent bEvent = (ChunkMonitorEvent.BasicChunkEvent) event;
                final Vector3i pos = bEvent.getPosition();
                final ChunkMonitorEntry entry;
                if (event instanceof ChunkMonitorEvent.Created) {
                    final ChunkMonitorEvent.Created cEvent = (ChunkMonitorEvent.Created) event;
                    entry = cEvent.getEntry();
                    if (pos.y < minRenderY) {
                        minRenderY = pos.y;
                    }
                    if (pos.y > maxRenderY) {
                        maxRenderY = pos.y;
                    }
                    chunks.add(entry);
                    map.put(pos, entry);
                } else {
                    entry = map.get(pos);
                }
                if (entry != null) {
                    entry.addEvent(bEvent);
                } else {
                    logger.error("No chunk monitor entry found for position {}", pos);
                }
            }
        }

        @Override
        public boolean needsRendering() {
            return true;
        }

        @Override
        public boolean fastResume() {
            return false;
        }
    }

    private class ResizeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
            queue.offer(new ResizeRequest(getWidth(), getHeight()));
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }

    private class MouseInputListener implements MouseWheelListener, MouseMotionListener, MouseListener {

        private Point leftPressed;

        @Override
        public void mouseDragged(MouseEvent e) {
            if (leftPressed != null) {
                final int dx = e.getPoint().x - leftPressed.x;
                final int dy = e.getPoint().y - leftPressed.y;
                setOffset(offsetX + dx, offsetY + dy);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            setRenderYDelta(e.getWheelRotation());
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == 2) {
                final Vector3i pos = calcPlayerChunkPos();
                if (pos != null) {
                    setRenderY(pos.y);
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                leftPressed = e.getPoint();
                offsetX = getOffsetX();
                offsetY = getOffsetY();
            }
            if (e.getButton() == MouseEvent.BUTTON2) {
                setSelectedChunk(mouseToChunkPos(e.getPoint()));
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                leftPressed = null;
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    private final class RenderTask implements Runnable {

        private RenderTask() {
        }

        private Rectangle calcBox(List<ChunkMonitorEntry> chunkEntries) {
            if (chunkEntries.isEmpty()) {
                return new Rectangle(0, 0, 0, 0);
            }
            int xmin = Integer.MAX_VALUE;
            int xmax = Integer.MIN_VALUE;
            int ymin = Integer.MAX_VALUE;
            int ymax = Integer.MIN_VALUE;
            for (ChunkMonitorEntry entry : chunkEntries) {
                final Vector3i pos = entry.getPosition();
                if (pos.y != renderY) {
                    continue;
                }
                if (pos.x < xmin) {
                    xmin = pos.x;
                }
                if (pos.x > xmax) {
                    xmax = pos.x;
                }
                if (pos.z < ymin) {
                    ymin = pos.z;
                }
                if (pos.z > ymax) {
                    ymax = pos.z;
                }
            }
            return new Rectangle(xmin, ymin, xmax - xmin + 1, ymax - ymin + 1);
        }

        private Color calcChunkColor(ChunkMonitorEntry entry) {
            final Chunk chunk = entry.getLatestChunk();

            if (chunk == null) {
                return COLOR_DEAD;
            }

            if (chunk.getMesh() != null) {
                return COLOR_HIGHLIGHT_TESSELLATION;
            }

            if (chunk.isReady()) {
                return COLOR_COMPLETE;
            } else {
                return COLOR_INTERNAL_LIGHT_GENERATION_PENDING;
            }
        }

        private void renderSelectedChunk(Graphics2D g, int offsetx, int offsety, Vector3i pos) {
            if (pos != null) {
                g.setColor(COLOR_SELECTED_CHUNK);
                g.drawRect(pos.x * chunkSize + offsetx, pos.z * chunkSize + offsety, chunkSize - 1, chunkSize - 1);
                g.drawRect(pos.x * chunkSize + offsetx - 1, pos.z * chunkSize + offsety - 1, chunkSize + 1, chunkSize + 1);
            }
        }

        private void renderBox(Graphics2D g, int offsetx, int offsety, Rectangle box) {
            g.setColor(Color.white);
            g.drawRect(box.x * chunkSize + offsetx, box.y * chunkSize + offsety, box.width * chunkSize - 1, box.height * chunkSize - 1);
        }

        private void renderBackground(Graphics2D g, int width, int height) {
            g.setColor(Color.black);
            g.fillRect(0, 0, width, height);
        }

        private void renderChunks(Graphics2D g, int offsetx, int offsety, List<ChunkMonitorEntry> chunkEntries) {
            chunkEntries.stream().filter(entry -> entry.getPosition().y == renderY).forEach(entry ->
                    renderChunk(g, offsetx, offsety, entry.getPosition(), entry));
        }

        private void renderChunk(Graphics2D g, int offsetx, int offsety, Vector3i pos, ChunkMonitorEntry entry) {
            g.setColor(calcChunkColor(entry));
            g.fillRect(pos.x * chunkSize + offsetx + 1, pos.z * chunkSize + offsety + 1, chunkSize - 2, chunkSize - 2);
        }

        private void render(Graphics2D g, int offsetx, int offsety, int width, int height, List<ChunkMonitorEntry> chunkEntries) {
            final Rectangle box = calcBox(chunkEntries);
            renderBackground(g, width, height);
            renderChunks(g, offsetx, offsety, chunkEntries);
            renderBox(g, offsetx, offsety, box);
            renderSelectedChunk(g, offsetx, offsety, selectedChunk);
        }

        private void render() {
            final Graphics2D g = image.getGraphics();
            if (g != null) {
                final int iw = image.getWidth();
                final int ih = image.getHeight();
                render(g, centerOffsetX + offsetX, centerOffsetY + offsetY, iw, ih, chunks);
                image.swap();
                repaint();
            }
        }

        private void repaint() {
            SwingUtilities.invokeLater(ChunkMonitorDisplay.this::repaint);
        }

        private long poll(List<Request> output) throws InterruptedException {
            long time = System.currentTimeMillis();
            final Request r = queue.poll(500, TimeUnit.MILLISECONDS);
            if (r != null) {
                output.add(r);
                queue.drainTo(output);
            }
            return (System.currentTimeMillis() - time);
        }

        private void doFollowPlayer() {
            final Vector3i pos = calcPlayerChunkPos();
            if (pos != null) {
                setRenderY(pos.y);
            }
        }

        @Override
        public void run() {

            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            final List<Request> requests = new LinkedList<>();

            try {
                while (true) {

                    final long slept = poll(requests);
                    boolean needsRendering = false;
                    boolean fastResume = false;

                    for (Request r : requests) {
                        try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(r.getName())) {
                            r.execute();
                        } catch (Exception e) {
                            ThreadMonitor.addError(e);
                            logger.error("Thread error", e);
                        } finally {
                            needsRendering |= r.needsRendering();
                            fastResume |= r.fastResume();
                        }
                    }

                    requests.clear();

                    if (followPlayer) {
                        doFollowPlayer();
                    }

                    if (needsRendering) {
                        render();
                    }

                    if (!fastResume && (slept <= 400)) {
                        Thread.sleep(500 - slept);
                    }
                }
            } catch (Exception e) {
                ThreadMonitor.addError(e);
                logger.error("Thread error", e);
            }
        }
    }
}

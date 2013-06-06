package org.terasology.monitoring.gui;

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
import java.util.HashMap;
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

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.ChunkMonitor;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.impl.ChunkMonitorEvent;
import org.terasology.monitoring.impl.ChunkMonitorEntry;
import org.terasology.monitoring.impl.SingleThreadMonitor;
import org.terasology.world.chunks.Chunk;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class ChunkMonitorDisplay extends JPanel {

    public static final Color ColorComplete = new Color(0, 38, 28);
    public static final Color ColorFullLightConnectivityPending = new Color(69, 191, 85);
    public static final Color ColorLightPropagationPending = new Color(22, 127, 57);
    public static final Color ColorInternalLighGenerationPending = new Color(4, 76, 41);
    public static final Color ColorAdjacencyGenerationPending = new Color(150, 237, 137);
    
    public static final Color ColorHighlightTessellation = Color.blue.brighter().brighter();
    
    public static final Color ColorSelectedChunk = new Color(255, 102, 0);
    
    public static final Color ColorDead = Color.lightGray;
    public static final Color ColorInvalid = Color.red;
    
    protected static final Logger logger = LoggerFactory.getLogger(ChunkMonitorDisplay.class);

    protected final EventBus eventbus = new EventBus("ChunkMonitorDisplay");
    protected final List<ChunkMonitorEntry> chunks = new LinkedList<ChunkMonitorEntry>();
    protected final Map<Vector3i, ChunkMonitorEntry> map = new HashMap<Vector3i, ChunkMonitorEntry>();
    protected final ImageBuffer image = new ImageBuffer();
    
    protected int refreshInterval, centerOffsetX = 0, centerOffsetY = 0, offsetX, offsetY, chunkSize;
    protected int renderY = 0, minRenderY = 0, maxRenderY = 0;
    protected boolean followPlayer = true;
    
    protected Vector3i selectedChunk = null;
    
    protected final BlockingQueue<Request> queue = new LinkedBlockingQueue<Request>();
    protected final ExecutorService executor;
    protected final Runnable renderTask;
    
    protected void fireChunkSelectedEvent(Vector3i pos) {
        eventbus.post(new ChunkMonitorDisplayEvent.Selected(this, pos, pos == null ? null : map.get(pos)));
    }
    
    protected Vector3i mouseToChunkPos(Point p) {
        Preconditions.checkNotNull(p, "The parameter 'p' must not be null");
        int x = (p.x - centerOffsetX - offsetX) / chunkSize, z = (p.y - centerOffsetY - offsetY) / chunkSize;
        return new Vector3i(x-1, renderY, z);
    }
    
    protected void updateDisplay() {
        queue.offer(new RenderRequest());
    }
    
    protected void updateDisplay(boolean fastResume) {
        queue.offer(new RenderRequest(fastResume));
    }
    
    protected void recomputeRenderY() {
        int min = 0, max = 0, y = renderY;
        for (ChunkMonitorEntry chunk : chunks) {
            final Vector3i pos = chunk.getPosition();
            if (pos.y < min) min = pos.y;
            if (pos.y > max) max = pos.y;
        }
        if (y < min) y = min;
        if (y > max) y = max;
        minRenderY = min;
        maxRenderY = max;
        renderY = y;
    }
    
    protected static class ImageBuffer {
        
        protected final ReadWriteLock lock = new ReentrantReadWriteLock();
        protected int width, height;
        protected BufferedImage imageA, imageB;
        
        public ImageBuffer(int width, int height) {
            resize(width, height);
        }
        
        public ImageBuffer() {}
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public Graphics2D getGraphics() {
            lock.readLock().lock();
            try {
                if (imageB != null)
                    return (Graphics2D) imageB.getGraphics();
            } finally {
                lock.readLock().unlock();
            }
            return null;
        }
        
        public void resize(int width, int height) {
            lock.writeLock().lock();
            try {
                this.width = width;
                this.height = height;
                if (width < 1 || height < 1) {
                    imageB = null;
                } else if (imageB == null || width != imageB.getWidth() || height != imageB.getHeight()) {
                    imageB = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
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
    
    protected interface Request {
        
        public boolean isChunkEvent();
        
        public boolean needsRendering();
        
        public boolean fastResume();
        
        public void execute();
    }
    
    protected abstract class UpdateRequest implements Request {

        @Override
        public boolean isChunkEvent() {return false;}
    }
    
    protected class RenderRequest extends UpdateRequest {

        protected final boolean fastResume;
        
        public RenderRequest(boolean fastResume) {
            this.fastResume = fastResume;
        }
        
        public RenderRequest() {
            this.fastResume = false;
        }
        
        @Override
        public void execute() {}

        @Override
        public boolean needsRendering() {
            return true;
        }

        @Override
        public boolean fastResume() {
            return fastResume;
        }
    }
    
    protected class InitialRequest extends UpdateRequest {

        @Override
        public void execute() {
            ChunkMonitor.getChunks(chunks);
            recomputeRenderY();
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
    
    protected class ResizeRequest extends UpdateRequest {
        
        public final int width, height;
        
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
        public boolean needsRendering() {
            return true;
        }

        @Override
        public boolean fastResume() {
            return true;
        }
    }
    
    protected class ChunkRequest implements Request {
        
        public final ChunkMonitorEvent event;
        
        public ChunkRequest(ChunkMonitorEvent event) {
            Preconditions.checkNotNull(event, "The parameter 'event' must not be null");
            this.event = event;
        }
        
        @Override
        public boolean isChunkEvent() {return true;}
        
        @Override
        public void execute() {
            if (event instanceof ChunkMonitorEvent.ChunkProviderInitialized) {
                chunks.clear();
                map.clear();
                ChunkMonitor.getChunks(chunks);
                for (ChunkMonitorEntry e : chunks)
                    map.put(e.getPosition(), e);
                recomputeRenderY();
            }
            else if (event instanceof ChunkMonitorEvent.ChunkProviderDisposed) {
                chunks.clear();
                map.clear();
                recomputeRenderY();
            }
            else if (event instanceof ChunkMonitorEvent.BasicChunkEvent) {
                final ChunkMonitorEvent.BasicChunkEvent bEvent = (ChunkMonitorEvent.BasicChunkEvent) event;
                final Vector3i pos = bEvent.getPosition();
                final ChunkMonitorEntry entry;
                if (event instanceof ChunkMonitorEvent.Created) {
                    final ChunkMonitorEvent.Created cEvent = (ChunkMonitorEvent.Created) event;
                    entry = cEvent.getEntry();
                    if (pos.y < minRenderY)
                        minRenderY = pos.y;
                    if (pos.y > maxRenderY)
                        maxRenderY = pos.y;
                    chunks.add(entry);
                    map.put(pos, entry);
                } else 
                    entry = map.get(pos);
                if (entry != null)
                    entry.addEvent(bEvent);
                else
                    logger.error("No chunk monitor entry found for position {}", pos);
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
    
    protected class ResizeListener implements ComponentListener {
        
        @Override
        public void componentResized(ComponentEvent e) {
            queue.offer(new ResizeRequest(getWidth(), getHeight()));
        }

        @Override
        public void componentMoved(ComponentEvent e) {}
        @Override
        public void componentShown(ComponentEvent e) {}
        @Override
        public void componentHidden(ComponentEvent e) {}
    }
    
    protected class MouseInputListener implements MouseWheelListener, MouseMotionListener, MouseListener {

        private Point leftPressed = null;
        private int offsetX, offsetY;
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (leftPressed != null) {
                final int dx = e.getPoint().x - leftPressed.x, dy = e.getPoint().y - leftPressed.y;
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
                if (pos != null) 
                    setRenderY(pos.y);
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
            if (e.getButton() == MouseEvent.BUTTON1)
                leftPressed = null;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            
        }

        @Override
        public void mouseExited(MouseEvent e) {
            
        }
    }
    
    protected class RenderTask implements Runnable {

        protected RenderTask() {}
        
        protected Rectangle calcBox(List<ChunkMonitorEntry> chunks) {
            if (chunks.isEmpty()) 
                return new Rectangle(0, 0, 0, 0);
            int xmin = Integer.MAX_VALUE, xmax = Integer.MIN_VALUE;
            int ymin = Integer.MAX_VALUE, ymax = Integer.MIN_VALUE;
            for (ChunkMonitorEntry entry : chunks) {
                final Vector3i pos = entry.getPosition();
                if (pos.y != renderY) continue;
                if (pos.x < xmin) xmin = pos.x;
                if (pos.x > xmax) xmax = pos.x;
                if (pos.z < ymin) ymin = pos.z;
                if (pos.z > ymax) ymax = pos.z;
            }
            return new Rectangle(xmin, ymin, xmax - xmin + 1, ymax - ymin + 1);
        }

        protected Color calcChunkColor(ChunkMonitorEntry entry) {
            final Chunk chunk = entry.getLatestChunk();
            
            if (chunk == null)
                return ColorDead;
            
            if (chunk.getMesh() != null)
                return ColorHighlightTessellation;
            
            switch(chunk.getChunkState()) {
            case ADJACENCY_GENERATION_PENDING:
                return ColorAdjacencyGenerationPending;
            case FULL_LIGHT_CONNECTIVITY_PENDING:
                return ColorFullLightConnectivityPending;
            case LIGHT_PROPAGATION_PENDING:
                return ColorLightPropagationPending;
            case INTERNAL_LIGHT_GENERATION_PENDING:
                return ColorInternalLighGenerationPending;
            case COMPLETE:
                return ColorComplete;
            }
            
            return ColorInvalid;
        }
        
        protected void renderSelectedChunk(Graphics2D g, int offsetx, int offsety, Vector3i pos) {
            if (pos != null) {
                g.setColor(ColorSelectedChunk);
                g.drawRect(pos.x * chunkSize + offsetx, pos.z * chunkSize + offsety, chunkSize - 1, chunkSize - 1);
                g.drawRect(pos.x * chunkSize + offsetx - 1, pos.z * chunkSize + offsety - 1, chunkSize + 1, chunkSize + 1);
            }
        }

        protected void renderBox(Graphics2D g, int offsetx, int offsety, Rectangle box) {
            g.setColor(Color.white);
            g.drawRect(box.x * chunkSize + offsetx, box.y * chunkSize + offsety, box.width * chunkSize - 1, box.height * chunkSize - 1);
        }

        protected void renderBackground(Graphics2D g, int width, int height) {
            g.setColor(Color.black);
            g.fillRect(0, 0, width, height);
        }

        protected void renderChunks(Graphics2D g, int offsetx, int offsety, List<ChunkMonitorEntry> chunks) {
            for (ChunkMonitorEntry entry : chunks) {
                if (entry.getPosition().y == renderY) 
                    renderChunk(g, offsetx, offsety, entry.getPosition(), entry);
            }
        }

        protected void renderChunk(Graphics2D g, int offsetx, int offsety, Vector3i pos, ChunkMonitorEntry entry) {
            g.setColor(calcChunkColor(entry));
            g.fillRect(pos.x * chunkSize + offsetx + 1, pos.z * chunkSize + offsety + 1, chunkSize - 2, chunkSize - 2);
        }
        
        protected void render(Graphics2D g, int offsetx, int offsety, int width, int height, List<ChunkMonitorEntry> chunks) {
            final Rectangle box = calcBox(chunks);
            renderBackground(g, width, height);
            renderChunks(g, offsetx, offsety, chunks);
            renderBox(g, offsetx, offsety, box);
            renderSelectedChunk(g, offsetx, offsety, selectedChunk);
        }
        
        protected void render() {
            final Graphics2D g = image.getGraphics();
            if (g != null) {
                final int iw = image.getWidth(), ih = image.getHeight();
                render(g, centerOffsetX + offsetX, centerOffsetY + offsetY, iw, ih, chunks);
                image.swap();
                repaint();
            }
        }
        
        protected void repaint() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ChunkMonitorDisplay.this.repaint();
                }
            });
        }
        
        protected long poll(List<Request> output) throws InterruptedException {
            long time = System.currentTimeMillis();
            final Request r = queue.poll(500, TimeUnit.MILLISECONDS);
            if (r != null) {
                output.add(r);
                queue.drainTo(output);
            }
            return (System.currentTimeMillis() - time);
        }
        
        protected void doFollowPlayer() {
            final Vector3i pos = calcPlayerChunkPos();
            if (pos != null) {
                setRenderY(pos.y);
            }
        }
        
        @Override
        public void run() {
            
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            
            final LinkedList<Request> requests = new LinkedList<Request>();
            final SingleThreadMonitor monitor = ThreadMonitor.create("Monitoring.Chunks", "Requests", "Events", "Polls");
            
            try {
                while (true) {
                    
                    final long slept = poll(requests);
                    boolean needsRendering = false, fastResume = false;
                    
                    monitor.increment(2);
                    
                    for (Request r : requests) 
                        try {
                            r.execute();
                        } catch (Exception e) {
                            monitor.addError(e);
                            logger.error("Thread error", e);
                        } finally {
                            needsRendering |= r.needsRendering();
                            fastResume |= r.fastResume();
                            if (r.isChunkEvent())
                                monitor.increment(1);
                            else
                                monitor.increment(0);
                        }
                    
                    requests.clear();
                    
                    if (followPlayer)
                        doFollowPlayer();
                    
                    if (needsRendering)
                        render();
                    
                    if (!fastResume && (slept <= 400))
                        Thread.sleep(500 - slept);
                }
            } catch (Exception e) {
                monitor.addError(e);
                logger.error("Thread error", e);
            } finally {
                monitor.setActive(false);
            }
        }
    };

    protected Vector3i calcPlayerChunkPos() {
        final LocalPlayer p = CoreRegistry.get(LocalPlayer.class);
        if (p != null) {
            return TeraMath.calcChunkPos(new Vector3i(p.getPosition()));
        }
        return null;
    }

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
        if (selectedChunk == null)
            return null;
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
        if (value < minRenderY) value = minRenderY;
        if (value > maxRenderY) value = maxRenderY;
        if (renderY != value) {
            renderY = value;
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
    public void recieveChunkEvent(ChunkMonitorEvent event) {
        if (event != null)
            queue.offer(new ChunkRequest(event));
    }

    @Override
    public void paint(Graphics g) {
        if (!image.render(g, 0, 0))
            super.paint(g);
    }
}
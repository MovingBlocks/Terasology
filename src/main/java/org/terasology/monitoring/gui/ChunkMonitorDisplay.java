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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class ChunkMonitorDisplay extends JPanel {

    public static final int PURGE_DEAD_CHUNKS_INTERVAL = 10000;
    
    public static final Color ColorComplete = new Color(0, 38, 28);
    public static final Color ColorFullLightConnectivityPending = new Color(69, 191, 85);
    public static final Color ColorLightPropagationPending = new Color(22, 127, 57);
    public static final Color ColorInternalLighGenerationPending = new Color(4, 76, 41);
    public static final Color ColorAdjacencyGenerationPending = new Color(150, 237, 137);
    public static final Color ColorDead = Color.lightGray;
    public static final Color ColorInvalid = Color.red;
    
    protected static final Logger logger = LoggerFactory.getLogger(ChunkMonitorDisplay.class);

    protected final List<ChunkMonitorEntry> chunks = new LinkedList<ChunkMonitorEntry>();
    
    protected AtomicReference<BufferedImage> imageRef = new AtomicReference<BufferedImage>();
    protected int refreshInterval, centerOffsetX = 0, centerOffsetY = 0, offsetX, offsetY, chunkSize;
    protected int renderY = 0, minRenderY = 0, maxRenderY = 0;
    protected boolean followPlayer = true;
    
    protected final BlockingQueue<Request> queue = new LinkedBlockingQueue<Request>();
    protected final ExecutorService executor;
    protected final Runnable renderTask;
    
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
        
    protected interface Request {
        
        public boolean isChunkEvent();
        
        public void execute();
    }
    
    protected abstract class UpdateRequest implements Request {

        @Override
        public boolean isChunkEvent() {return false;}
    }
    
    protected class RenderRequest extends UpdateRequest {

        @Override
        public void execute() {}
    }
    
    protected class InitialRequest extends UpdateRequest {

        @Override
        public void execute() {
            ChunkMonitor.getChunks(chunks);
            recomputeRenderY();
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
            if (width < 10 || height < 10) {
                imageRef.set(null);
            } else {
                BufferedImage img = imageRef.get();
                if (img == null || img.getWidth() != width || img.getHeight() != height) {
                    imageRef.set(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
                    centerOffsetX = width / 2 - chunkSize / 2;
                    centerOffsetY = height / 2 - chunkSize / 2;
                }
            }
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
                ChunkMonitor.getChunks(chunks);
                recomputeRenderY();
            }
            else if (event instanceof ChunkMonitorEvent.ChunkProviderDisposed) {
                chunks.clear();
                recomputeRenderY();
            }
            else if (event instanceof ChunkMonitorEvent.Created) {
                final ChunkMonitorEvent.Created e = (ChunkMonitorEvent.Created) event;
                final ChunkMonitorEntry chunk = e.getEntry();
                final Vector3i pos = e.getPosition();
                if (pos.y < minRenderY)
                    minRenderY = pos.y;
                if (pos.y > maxRenderY)
                    maxRenderY = pos.y;
                chunks.add(chunk);
            }
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
            final Chunk chunk = entry.getChunk();
            if (chunk == null)
                return ColorDead;
            
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
        }
        
        protected void render() {
            final BufferedImage image = imageRef.get();
            if (image != null) {
                Graphics2D g = (Graphics2D) image.getGraphics();
                final int iw = image.getWidth(), ih = image.getHeight();
                render(g, centerOffsetX + offsetX, centerOffsetY + offsetY, iw, ih, chunks);
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
            final SingleThreadMonitor monitor = ThreadMonitor.create("Monitoring.Chunks", "Requests", "Events");
            
            try {
                while (true) {
                    final long slept = poll(requests);
                    for (Request r : requests) 
                        try {
                            r.execute();
                        } catch (Exception e) {
                            monitor.addError(e);
                            logger.error("Thread error", e);
                        } finally {
                            if (r.isChunkEvent())
                                monitor.increment(1);
                            else
                                monitor.increment(0);
                        }
                    requests.clear();
                    if (followPlayer)
                        doFollowPlayer();
                    render();
                    if (slept <= 400)
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
            queue.offer(new RenderRequest());
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
            queue.offer(new RenderRequest());
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
            queue.offer(new RenderRequest());
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
            queue.offer(new RenderRequest());
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
    
    @Subscribe
    public void recieveChunkEvent(ChunkMonitorEvent event) {
        if (event != null) {
            queue.offer(new ChunkRequest(event));
        }
    }

    @Override
    public void paint(Graphics g) {
        final BufferedImage img = imageRef.get();
        if (img != null) {
            g.drawImage(img, 0, 0, null);
        } else {
            super.paint(g);
        }
    }
}
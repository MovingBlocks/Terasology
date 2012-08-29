package org.terasology.rendering.gui.framework.style;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.widgets.UIImage;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIStyleBorderImage extends UIDisplayContainer implements UIStyle {

    private Texture texture;
    private Vector4f width = new Vector4f(0f, 0f, 0f, 0f);
    private Vector2f sourceOrigin;
    private Vector2f sourceSize;
    
    private Vector2f targetOrigin = new Vector2f(0f, 0f);
    private Vector2f targetSize = new Vector2f(0f, 0f);
    
    private final Map<String, UIImage> frames = new HashMap<String, UIImage>();
    private final Map<String, UIImage> corners = new HashMap<String, UIImage>();

    public UIStyleBorderImage(Texture texture) {        
        this.texture = texture;
        setCrop(false);
    }

    /**
     * Create the frame from the texture.
     * @param side The side.
     */
    private void sourceFrame(String side) {
        if (!side.equals("top") && !side.equals("bottom") && !side.equals("right") && !side.equals("left")) {
            return;
        }
        
        UIImage frame;
        
        if (frames.containsKey(side)) {
            frame = frames.get(side);
        } else {
            frame = new UIImage(texture);
        }

        if (side.equals("top")) {
            
            frame.setTextureOrigin(new Vector2f(sourceOrigin.x + width.w, sourceOrigin.y));
            frame.setTextureSize(new Vector2f(sourceSize.x - width.w - width.y, width.x));
            
        } else if (side.equals("bottom")) {
            
            frame.setTextureOrigin(new Vector2f(sourceOrigin.x + width.w, sourceOrigin.y + sourceSize.y - width.z));
            frame.setTextureSize(new Vector2f(sourceSize.x - width.w - width.y, width.z));
            
        } else if (side.equals("right")) {
            
            frame.setTextureOrigin(new Vector2f(sourceOrigin.x + sourceSize.x - width.y, sourceOrigin.y + width.x));
            frame.setTextureSize(new Vector2f(width.y, sourceSize.y - width.x - width.z));
            
        } else if (side.equals("left")) {
            
            frame.setTextureOrigin(new Vector2f(sourceOrigin.x, sourceOrigin.y + width.x));
            frame.setTextureSize(new Vector2f(width.w, sourceSize.y - width.x - width.z));
            
        } 
        
        addDisplayElement(frame);
        frames.put(side, frame);
    }
    
    /**
     * Position the frame on the target.
     * @param side The side.
     */
    private void targetFrame(String side) {
        UIImage frame = frames.get(side);
        
        if (frame == null) {
            return;
        }
        
        if (side.equals("top")) {
            
            frame.setPosition(new Vector2f(targetOrigin.x + width.w, targetOrigin.y));
            frame.setSize(new Vector2f(targetSize.x - width.w - width.y, width.x));
            frame.setVisible(true);
            
        } else if (side.equals("bottom")) {
            
            frame.setPosition(new Vector2f(targetOrigin.x + width.w, targetOrigin.y + targetSize.y - width.z));
            frame.setSize(new Vector2f(targetSize.x - width.w - width.y, width.z));
            frame.setVisible(true);
            
        } else if (side.equals("right")) {
            
            frame.setPosition(new Vector2f(targetOrigin.x + targetSize.x - width.y, targetOrigin.y + width.x));
            frame.setSize(new Vector2f(width.y, targetSize.y - width.x - width.z));
            frame.setVisible(true);
            
        } else if (side.equals("left")) {
            
            frame.setPosition(new Vector2f(targetOrigin.x, targetOrigin.y + width.x));
            frame.setSize(new Vector2f(width.w, targetSize.y - width.x - width.z));
            frame.setVisible(true);
            
        }
    }
    
    /**
     * Create the corner from the texture.
     * @param side The side.
     */
    private void sourceCorner(String side) {
        if (!side.equals("top-left") && !side.equals("top-right") && !side.equals("bottom-left") && !side.equals("bottom-right")) {
            return;
        }
        
        UIImage corner;
        
        if (corners.containsKey(side)) {
            corner = corners.get(side);
        } else {
            corner = new UIImage(texture);
        }
        
        if (side.equals("top-left")) {
            
            corner.setTextureOrigin(new Vector2f(sourceOrigin.x, sourceOrigin.y));
            corner.setTextureSize(new Vector2f(width.w, width.x));
            
        } else if (side.equals("top-right")) {
            
            corner.setTextureOrigin(new Vector2f(sourceOrigin.x + sourceSize.x - width.y, sourceOrigin.y));
            corner.setTextureSize(new Vector2f(width.y, width.x));
            
        } else if (side.equals("bottom-left")) {
            
            corner.setTextureOrigin(new Vector2f(sourceOrigin.x, sourceOrigin.y + sourceSize.y - width.z));
            corner.setTextureSize(new Vector2f(width.w, width.z));
            
        } else if (side.equals("bottom-right")) {
            
            corner.setTextureOrigin(new Vector2f(sourceOrigin.x + sourceSize.x - width.y, sourceOrigin.y + sourceSize.y - width.z));
            corner.setTextureSize(new Vector2f(width.y, width.z));
            
        }
        
        addDisplayElement(corner);
        corners.put(side, corner);        
    }
    
    /**
     * Position the corner on the target.
     * @param side The side.
     */
    private void targetCorner(String side) {
        UIImage corner = corners.get(side);
        
        if (corner == null) {
            return;
        }
        
        if (side.equals("top-left")) {
            
            corner.setPosition(new Vector2f(targetOrigin.x, targetOrigin.y));
            corner.setSize(new Vector2f(width.w, width.x));
            corner.setVisible(true);
            
        } else if (side.equals("top-right")) {
            
            corner.setPosition(new Vector2f(targetOrigin.x + targetSize.x - width.y, targetOrigin.y));
            corner.setSize(new Vector2f(width.y, width.x));
            corner.setVisible(true);
            
        } else if (side.equals("bottom-left")) {
            
            corner.setPosition(new Vector2f(targetOrigin.x, targetOrigin.y + targetSize.y - width.z));
            corner.setSize(new Vector2f(width.w, width.z));
            corner.setVisible(true);
            
        } else if (side.equals("bottom-right")) {
            
            corner.setPosition(new Vector2f(targetOrigin.x + targetSize.x - width.y, targetOrigin.y + targetSize.y - width.z));
            corner.setSize(new Vector2f(width.y, width.z));
            corner.setVisible(true);
            
        }
        
    }
    
    /**
     * Set the source origin, size and border width of the image.
     * @param origin The origin in the texture.
     * @param size The size of the border container in the texture.
     * @param width The border width. x = top, y = right, z = bottom, w = left
     */
    public void setBorderSource(Vector2f origin, Vector2f size, Vector4f width) {
        this.sourceOrigin = origin;
        this.sourceSize = size;
        this.width = width;
        
        sourceFrame("top");
        sourceFrame("bottom");
        sourceFrame("right");
        sourceFrame("left");
        
        sourceCorner("top-left");
        sourceCorner("top-right");
        sourceCorner("bottom-left");
        sourceCorner("bottom-right");
    }
    
    @Override
    public void layout() {
        super.layout();
        
        if (getParent() != null && frames.size() > 0) {
            this.targetOrigin.x = -width.w;
            this.targetOrigin.y = -width.x;
            this.targetSize.x = getParent().getSize().x + width.y + width.w;
            this.targetSize.y = getParent().getSize().y + width.x + width.z;
            
            targetFrame("top");
            targetFrame("bottom");
            targetFrame("right");
            targetFrame("left");
            
            targetCorner("top-left");
            targetCorner("top-right");
            targetCorner("bottom-left");
            targetCorner("bottom-right");
        }
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }
    
    public Texture getTexture() {
        return texture;
    }
}

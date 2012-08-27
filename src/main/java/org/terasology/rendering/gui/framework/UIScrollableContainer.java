package org.terasology.rendering.gui.framework;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.framework.events.ScrollListener;
import org.terasology.rendering.gui.framework.style.UIStyle;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIImage;

/**
 * A container which will display a scrollbar if the content is to big to be displayed.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public abstract class UIScrollableContainer extends UIDisplayContainer {
    
    private final List<ScrollListener> scrollListener = new ArrayList<ScrollListener>();
    private UIComposite container;
    
    //scrollbar
    private UIImage scrollbar;
    private boolean scrolling = false;            //true if the scrollbar was pressed, this will enable scrolling
    private float scrollbarPressedOffset;         //the position on which the scrollbar was grabbed with the mouse
    
    //content
    private float max;                            //the max position value of all child elements
    private float contentHeight;                  //the contents hight of all chicld elements
    private float multiplier = 1f;                //the multiplier of how the movement in the scrollbar will move the actual child elements
    
    //settings
    private boolean enableScrolling = false;
    private boolean enableScrollbar = false;
    
    //other
    private boolean isScrollable = false;
    
    public UIScrollableContainer() {
        setup();
    }
    
    public UIScrollableContainer(Vector2f size) {
        setSize(size);
        setEnableScrolling(true);
        setEnableScrollbar(true);
        
        setup();
    }

    private void setup() {
        container = new UIComposite();
        container.setSize("100%", "100%");
        container.setVisible(true);
        
        scrollbar = new UIImage(AssetManager.loadTexture("engine:gui_menu"));
        scrollbar.setCrop(false);
        scrollbar.setTextureOrigin(new Vector2f(0f, 0f));
        scrollbar.setTextureSize(new Vector2f(60f, 15f));
        scrollbar.setSize(new Vector2f(15f, 60f));
        scrollbar.setVisible(false);
        scrollbar.addMouseButtonListener(new MouseButtonListener() {    
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }
            
            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
                scrolling = false;
                scrollbarPressedOffset = 0f;
            }
            
            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                if (intersect && enableScrolling) {
                    scrolling = true;
                    
                    //calculate the press point on the scrollbar
                    scrollbarPressedOffset = Display.getHeight() - Mouse.getY() - scrollbar.getPosition().y - getPosition().y;
                }
            }
        });
        scrollbar.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void move(UIDisplayElement element) {
                if (scrolling && enableScrolling) {
                    moveScrollbar(Display.getHeight() - Mouse.getY());
                }
            }

            @Override
            public void leave(UIDisplayElement element) {
                
            }
            
            @Override
            public void hover(UIDisplayElement element) {
                
            }
            
            @Override
            public void enter(UIDisplayElement element) {
                
            }
        });
        
        addMouseButtonListener(new MouseButtonListener() {
            
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
                if (wheel != 0 && enableScrolling && isScrollable && intersect) {
                    moveScrollbar(scrollbar.getAbsolutePosition().y - (wheel / 10));
                }
            }
            
            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {

            }
            
            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {

            }
        });
        
        super.addDisplayElement(container);
        super.addDisplayElement(scrollbar);
    }

    /**
     * Move the scrollbar.
     * @param pos The position (y direction) where to move the scrollbar.
     */
    private void moveScrollbar(float pos) {
        float scrollbarPos = pos - getPosition().y - scrollbarPressedOffset;
        
        if (scrollbarPos < 0) {
            scrollbarPos = 0;
        }
        
        if (scrollbarPos > (getSize().y - scrollbar.getSize().y)) {
            scrollbarPos = getSize().y - scrollbar.getSize().y;
        }

        //calculate the different in the position
        scrollbar.setPosition(new Vector2f(getSize().x - scrollbar.getSize().x, scrollbarPos));
        
        //move the content
        container.setPosition(new Vector2f(container.getPosition().x, -multiplier * scrollbar.getPosition().y));
        
        notifyScrollListeners();
    }

    /**
     * Calculate the high of the content based on the child elements within this container.
     */
    private void calcContentHeight() {
        if (scrollbar != null) {
            if (container.getDisplayElements().size() > 0) {
                
                //Lets calculate the max value recursively
                max = 0;
                calcMax(container.getDisplayElements());
                contentHeight = max - getAbsolutePosition().y;
                
                //container.setSize(new Vector2f(getSize().x, contentHeight));
            }
            
            //check if the content is bigger than the container
            if (contentHeight > getSize().y) {
                isScrollable = true;
                
                //calculate how much space needs to be scrolled by the scrollbar
                float diff = contentHeight - getSize().y;
    
                //calculate the new multiplier based on the size which needs to be scrolled by the scrollbar
                //TODO calculate multiplier none linear..
                multiplier = (diff / getSize().y) + 1;
                
                if (enableScrollbar) {
                    //set the new size of the scrollbar based on the multiplier
                    scrollbar.setSize(new Vector2f(scrollbar.getSize().x, getSize().y / multiplier));
                    
                    //enable the scrollbar
                    scrollbar.setVisible(true);
                }
                
                moveScrollbar(scrollbar.getAbsolutePosition().y);
            } else {
                isScrollable = false;
                
                //disable the scrollbar
                scrollbar.setVisible(false);
                
                moveScrollbar(0f);
            }
        }
    }
    
    /**
     * Calculate the max value of all display elements within a display container recursively.
     * @param displayElements The display elements of a display container.
     */
    private void calcMax(List<UIDisplayElement> displayElements) {
        float elementMax;
        
        //loop through all child elements
        for (UIDisplayElement element : displayElements) {
            if (element.isVisible() && !(element instanceof UIStyle) && element != scrollbar) {
                //recursive action if the element also contains child elements
                if (element instanceof UIDisplayContainer) {
                    calcMax(((UIDisplayContainer)element).getDisplayElements());
                }

                elementMax = element.getAbsolutePosition().y + element.getSize().y;
                
                if (elementMax > max) {
                    max = elementMax;
                }
            }
        }
    }

    @Override
    public void addDisplayElement(UIDisplayElement element) {
        if (element instanceof UIStyle) {
            super.addDisplayElement(element);
        } else {
            container.addDisplayElement(element);
        }
    }
    
    @Override
    public void removeDisplayElement(UIDisplayElement element) {
        container.getDisplayElements().remove(element);
        element.setParent(null);
        
        calcContentHeight();
    }
    
    @Override
    public void removeAllDisplayElements() {
        for (UIDisplayElement element : container.getDisplayElements()) {
            element.setParent(null);
        }
        container.getDisplayElements().clear();
        
        calcContentHeight();
    }
    
    /**
     * Check if scrolling is enabled.
     * @return Returns true if scrolling is enabled.
     */
    public boolean isEnableScrolling() {
        return enableScrolling;
    }

    /**
     * Set if the container allows scrolling.
     * @param enable True to enable scrolling.
     */
    public void setEnableScrolling(boolean enable) {
        this.enableScrolling = enable;

        setCropContainer(enable);
    }
    
    /**
     * Check if the scrollbar will be displayed if the content is scrollable. If the scrollbar is disabled, scrolling can be achieved by using the mouse wheel.
     * @return Returns true if the scrollbar is enabled.
     */
    public boolean isEnableScrollbar() {
        return enableScrollbar;
    }

    /**
     * Set if the scrollbar will be displayed if the content is scrollable. If the scrollbar is disabled, scrolling can be achieved by using the mouse wheel.
     * @param enable True to enable scrollbar.
     */
    public void setEnableScrollbar(boolean enable) {
        this.enableScrollbar = enable;
        
        calcContentHeight();
    }
    
    private void notifyScrollListeners() {
        for (ScrollListener listener : scrollListener) {
            listener.scrolled(this);
        }
    }
    
    public void addScrollListener(ScrollListener listener) {
        scrollListener.add(listener);
    }

    public void removeScrollListener(ScrollListener listener) {
        scrollListener.remove(listener);
    }
    
    @Override
    public void layout() {
        super.layout();
        
        calcContentHeight();
    }
}

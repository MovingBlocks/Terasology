/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.framework.events.IMouseButtonListener;
import org.terasology.rendering.gui.framework.events.IMouseMoveListener;
import org.terasology.rendering.gui.framework.events.IScrollListener;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

/*
 * A simple graphical ScrollBar
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */

public class UIScrollBar extends UIDisplayContainer {

    //ScrollBarElements
    private UIScrollBarThumb _scrolBarThumb;

    //Scroll Listeners
    private final ArrayList<IScrollListener> _scrollListeners = new ArrayList<IScrollListener>();

    //Options
    private float _max;
    private float _min;

    private float _step;

    private float _prevMousePos = -1;

    private ScrollType _scrollType = ScrollType.vertical;

    private boolean _scrolled = false;
    private boolean _wheelled = false;

    private float _containerLength = 0.0f;
    private float _contentLength = 0.0f;
    private float _value = 0.0f;
    private float _oldValue = 0.0f;

    public static enum ScrollType {
        vertical, horizontal
    }
    
    public class UIScrollBarThumb extends UIDisplayContainer {
        //Graphics
        private UIGraphicsElement _header;
        private UIGraphicsElement _body;
        private UIGraphicsElement _footer;

        private UIScrollBar.ScrollType _scrollType = UIScrollBar.ScrollType.vertical;

        public UIScrollBarThumb(Vector2f size, UIScrollBar.ScrollType scrollType) {
            setSize(size);
            _scrollType = scrollType;

            _header = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));
            _body = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));
            _footer = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));

            _header.setVisible(true);
            _body.setVisible(true);
            _footer.setVisible(true);

            addDisplayElement(_header);
            addDisplayElement(_body);
            addDisplayElement(_footer);

            switch (scrollType) {
                case vertical:
                    setVerticalOptions();
                    break;
                case horizontal:
                    setHorizontalPositions();
                    break;
            }

            _header.getTextureOrigin().set(0f, 155f / 512f);
            _body.getTextureOrigin().set(7f / 512f, 155f / 512f);
            _footer.getTextureOrigin().set(18f / 512f, 155f / 512f);
        }

        private void setVerticalOptions() {
            /*SET POS FOR HEADER*/
            _header.setRotateAngle(90);
            _header.setPosition(getPosition());
            _header.getPosition().x += 15f;
            _header.setSize(new Vector2f(8f, 15f));
            _header.getTextureSize().set(new Vector2f(7f / 512f, 15f / 512f));

            /*SET POS FOR BODY*/
            _body.setRotateAngle(90);
            _body.setPosition(new Vector2f(getPosition().x, getPosition().y + _header.getSize().x));
            _body.getPosition().x += 15f;
            _body.getTextureSize().set(new Vector2f(10f / 512f, 15f / 512f));

            /*SET POS FOR FOOTER*/
            _footer.setRotateAngle(90);
            _footer.setPosition(new Vector2f(getPosition().x, getPosition().y + 2 * _header.getTextureSize().y + _body.getSize().y));
            _footer.getPosition().x += 15f;
            _footer.setSize(new Vector2f(8f, 15f));
            _footer.getTextureSize().set(new Vector2f(8f / 512f, 15f / 512f));
        }

        private void setHorizontalPositions() {
            /*SET POS FOR HEADER*/
            _header.setPosition(getPosition());
            _header.setSize(new Vector2f(7f, 15f));
            _header.getTextureSize().set(new Vector2f(7f / 512f, 15f / 512f));

            /*SET POS FOR BODY*/
            _body.setPosition(new Vector2f(getPosition().x + _header.getSize().x, getPosition().y));
            _body.getTextureSize().set(new Vector2f(10f / 512f, 15f / 512f));

            /*SET POS FOR FOOTER*/
            //_footer.setRotateAngle(180);
            _footer.setPosition(new Vector2f((getPosition().x + 2 * _header.getTextureSize().x + _body.getSize().x), getPosition().y));
            _footer.setSize(new Vector2f(8f, 15f));
            //_footer.getPosition().y += 15f;
            _footer.getTextureSize().set(new Vector2f(8f / 512f, 15f / 512f));
        }

        public void resize(float newScrollSize) {
            float newBodyScrollSize = newScrollSize - _header.getSize().x * 2;

            if (_scrollType == UIScrollBar.ScrollType.vertical) {
                setSize(new Vector2f(15f, newScrollSize));

                _body.setSize(new Vector2f(newBodyScrollSize, 15f));
                _footer.getPosition().y = _body.getPosition().y +
                        _body.getSize().x;
            } else {
                setSize(new Vector2f(newScrollSize, 15f));

                _body.setSize(new Vector2f(newBodyScrollSize, 15f));
                _footer.getPosition().x = _body.getPosition().x +
                        _body.getSize().x;
            }
        }

        public float getThumbPosition() {
            if (_scrollType == UIScrollBar.ScrollType.vertical) {
                return getPosition().y;
            }
            return getPosition().x;
        }

        public float getThumbSize() {
            if (_scrollType == UIScrollBar.ScrollType.vertical) {
                return getSize().y;
            }
            return getSize().x;
        }

        public void setThumbPosition(float newPosition) {
            if (_scrollType == UIScrollBar.ScrollType.vertical) {
                getPosition().y = newPosition;
            } else {
                getPosition().x = newPosition;
            }
        }

    }

    public UIScrollBar(Vector2f size, ScrollType scrollType) {
        setScrollType(scrollType);
        switch (_scrollType) {
            case vertical:
                setSize(new Vector2f(15f, size.y));
                _scrolBarThumb = new UIScrollBarThumb(getSize(), ScrollType.vertical);
                break;
            case horizontal:
                setSize(new Vector2f(size.x, 15f));
                _scrolBarThumb = new UIScrollBarThumb(getSize(), ScrollType.horizontal);
                break;
        }
        
        _scrolBarThumb.setVisible(true);
        _scrolBarThumb.addMouseButtonListener(new IMouseButtonListener() {
			@Override
			public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
				scrolled(calculateScrollFromWheel(((-1) * wheel / 30) / _step));
			}
			
			@Override
			public void up(UIDisplayElement element, int button, boolean intersect) {
				_scrolled = false;
			}
			
			@Override
			public void down(UIDisplayElement element, int button, boolean intersect) {
				if (intersect) {
					_scrolled = true;
	                if (_prevMousePos == -1) {
	                	Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
	                    if (_scrollType == ScrollType.vertical) {
	                        _prevMousePos = mousePos.y;
	                    } else {
	                        _prevMousePos = mousePos.x;
	                    }
	                }
				}
			}
		});
        addMouseListener(new IMouseMoveListener() {
			@Override
			public void move(UIDisplayElement element) {
				if (_scrolled) {
		        	Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
		            scrolled(calculateScrollFromMouse(_scrollType == ScrollType.vertical ? mousePos.y : mousePos.x));
		            updateThumb();
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
        
        addDisplayElement(_scrolBarThumb);
    }

    public void scrolled(float scrollTo) {
        _scrolBarThumb.setThumbPosition((_scrolBarThumb.getThumbPosition() + scrollTo));
        calculateValue();
        notifyScrolledListeners();
    }

	private float calculateScrollFromMouse(float mousePos) {
        float scrollValue = 0;

        if (_max < (_scrolBarThumb.getThumbPosition() + mousePos - _prevMousePos + _scrolBarThumb.getThumbSize())) {
            mousePos = _max - _scrolBarThumb.getThumbSize() + _prevMousePos - _scrolBarThumb.getThumbPosition();
        } else if (_min > (_scrolBarThumb.getThumbPosition() + mousePos - _prevMousePos)) {
            mousePos = _min + _prevMousePos - _scrolBarThumb.getThumbPosition();
        }

        scrollValue = mousePos - _prevMousePos;

        _prevMousePos = mousePos;

        return scrollValue;
    }

    public float calculateScrollFromWheel(float wheelValue) {
        float scrollValue = wheelValue;

        if (_max < (_scrolBarThumb.getThumbPosition() + wheelValue + _scrolBarThumb.getThumbSize())) {
            scrollValue = _max - _scrolBarThumb.getThumbSize() - _scrolBarThumb.getThumbPosition();
        } else if (_min > (_scrolBarThumb.getThumbPosition() + wheelValue)) {
            scrollValue = _min - _scrolBarThumb.getThumbPosition();
        }

        return scrollValue;
    }
    
    private void notifyScrolledListeners() {
        for (int i = 0; i < _scrollListeners.size(); i++) {
            _scrollListeners.get(i).scrolled(this);
        }
	}

    public void addScrollListener(IScrollListener listener) {
        _scrollListeners.add(listener);
    }

    public void removeScrollListener(IScrollListener listener) {
        _scrollListeners.remove(listener);
    }

    private void updateThumb() {
        float newScrollSize = _containerLength * _step;

        if (newScrollSize != _scrolBarThumb.getThumbSize()) {
            _scrolBarThumb.resize(newScrollSize);
        }

        if ((_scrolBarThumb.getThumbPosition() + _scrolBarThumb.getThumbSize() > _max)) {
            _prevMousePos = _scrolBarThumb.getThumbPosition();
            scrolled((_scrolBarThumb.getThumbPosition() + _scrolBarThumb.getThumbSize()) - _max);
        }
        calculateValue();
    }

    public void setScrollType(ScrollType scrollType) {
        _scrollType = scrollType;
    }


    private void calculateValue() {
        _value = _scrolBarThumb.getThumbPosition() / _step;
    }


    public void setMaxMin(float min, float max) {
        _min = min;
        _max = max;

        switch (_scrollType) {
            case vertical:
                setSize(new Vector2f(15f, max));
                break;
            case horizontal:
                setSize(new Vector2f(max, 15f));
                break;
        }
    }

    public boolean isScrolled() {
        return _scrolled;
    }

    public void setStep(float contentLength, float containerLength) {
        try {
            _step = containerLength / contentLength;

            if (_step > 1.0f) {
                _step = 1.0f;
            }

        } catch (ArithmeticException e) {
            _step = 1.0f;
        }

        _containerLength = containerLength;
        _contentLength = contentLength;
        
        updateThumb();
    }

    public float getValue() {
        return _value;
    }

    public float getOldValue() {
        return _oldValue;
    }

    public float getStep() {
        return _step;
    }

    public void setWheelled(boolean wheelled) {
        _wheelled = wheelled;
    }

    public void resetScrollPosition(){
        if( !isScrolled( ) ){
            scrolled((-1) * _scrolBarThumb.getThumbPosition());
        }
    }
}
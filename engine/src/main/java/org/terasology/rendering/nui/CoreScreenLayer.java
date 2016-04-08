/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui;

import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.animation.Animation;
import org.terasology.rendering.animation.AnimationListener;
import org.terasology.rendering.animation.Frame;
import org.terasology.rendering.animation.FloatInterpolator;
import org.terasology.rendering.animation.AccelerateInterpolator;
import org.terasology.rendering.animation.DecelerateInterpolator;
import org.terasology.rendering.animation.RepeatMode;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 */
public abstract class CoreScreenLayer extends AbstractWidget implements UIScreenLayer {

    private static final InteractionListener DEFAULT_SCREEN_LISTENER = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return true;
        }

        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            return true;
        }
    };

    @LayoutConfig
    private UIWidget contents;

    private NUIManager manager;
    private boolean initialised;

    private Rect2i animRegion;
    private Rect2i animCanvasSize;
    private Animation sizeAnim;

    public CoreScreenLayer() {
        animRegion = Rect2i.createFromMinAndMax(-9000, 0, 1000, 1000);
        animCanvasSize = animRegion;
    }

    public CoreScreenLayer(String id) {
        super(id);
        animRegion = Rect2i.createFromMinAndMax(-9000, 0, 1000, 1000);
        animCanvasSize = animRegion;
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    protected InteractionListener getScreenListener() {
        return DEFAULT_SCREEN_LISTENER;
    }

    public void setContents(UIWidget contents) {
        this.contents = contents;
    }

    @Override
    public void onOpened() {
        if (!initialised) {
            initialise();
            initialised = true;
        }
    }

    protected abstract void initialise();

    @Override
    public boolean isLowerLayerVisible() {
        return true;
    }

    @Override
    public boolean isReleasingMouse() {
        return true;
    }

    @Override
    public boolean isEscapeToCloseAllowed() {
        return true;
    }

    public UIWidget getContents() {
        return contents;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (sizeAnim != null && !sizeAnim.isFinished()) {
            animCanvasSize = canvas.getRegion();
        } else if (animCanvasSize == null) {
            animRegion = canvas.getRegion();
        }

        if (isModal()) {
            canvas.addInteractionRegion(getScreenListener());
        }
        if (contents != null) {
            canvas.drawWidget(contents, animRegion);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (contents != null) {
            contents.update(delta);
        }
    }

    @Override
    public NUIManager getManager() {
        return manager;
    }

    @Override
    public void setManager(NUIManager manager) {
        this.manager = manager;
    }

    @Override
    public void onClosed() {
    }

    @Override
    public void onGainFocus() {
    }

    @Override
    public void onLoseFocus() {
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent event) {
    }

    @Override
    public void onMouseWheelEvent(MouseWheelEvent event) {
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        return false;
    }

    @Override
    public void onBindEvent(BindButtonEvent event) {
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        return areaHint;
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean isSkinAppliedByCanvas() {
        return true;
    }

    @Override
    public String getMode() {
        return DEFAULT_MODE;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        if (contents == null) {
            return Collections.emptyIterator();
        }
        return Arrays.asList(contents).iterator();
    }

    @Override
    public Animation getOpenAnimation() {
        sizeAnim = new Animation();
        Frame frm = new Frame();
        // Move into view from left side (l to r)
        frm.addComponent(new Float(0),
                         new Float(1),
                         new FloatInterpolator() {
                public void setValue(Object value) {
                    float v = ((Float) value).floatValue();
                    animRegion = Rect2i.createFromMinAndMax(
                        (int) (-animCanvasSize.maxX() * (1 - v)),
                        animCanvasSize.minY(),
                        (int) (animCanvasSize.maxX() * v),
                        animCanvasSize.maxY());
                }
            }, new DecelerateInterpolator());
        frm.setDuration(.75f);
        frm.setRepeatCount(1);
        sizeAnim.addFrame(frm);
        sizeAnim.setRepeatCount(1);
        sizeAnim.setRepeatMode(RepeatMode.RUN_ONCE);
        sizeAnim.addListener(new AnimationListener() {
                @Override public void onStart() {
                    animRegion = Rect2i.createFromMinAndMax(-9000, 0, 1000, 1000);
                    animCanvasSize = animRegion;
                }
                @Override public void onEnd() {
                    sizeAnim = null;
                    animRegion = null;
                    animCanvasSize = null;
                }
            });
        return sizeAnim;
    }

    @Override
    public Animation getCloseAnimation() {
        sizeAnim = new Animation();
        Frame frm = new Frame();
        // Move into view out of right side (r to inf)
        frm.addComponent(new Float(0),
                         new Float(1),
                         new FloatInterpolator() {
                public void setValue(Object value) {
                    float v = ((Float) value).floatValue();
                    animRegion = Rect2i.createFromMinAndMax(
                        (int) (animCanvasSize.maxX() * v),
                        animCanvasSize.minY(),
                        (int) (animCanvasSize.maxX() * (v + 1)),
                        animCanvasSize.maxY());
                }
            }, new AccelerateInterpolator());
        frm.setDuration(.75f);
        frm.setRepeatCount(1);
        sizeAnim.addFrame(frm);
        sizeAnim.setRepeatCount(1);
        sizeAnim.setRepeatMode(RepeatMode.RUN_ONCE);
        sizeAnim.addListener(new AnimationListener() {
                @Override public void onStart() {
                    animCanvasSize = animRegion;
                }
                @Override public void onEnd() {
                    sizeAnim = null;
                    animRegion = null;
                    animCanvasSize = null;
                }
            });
        return sizeAnim;
    }
}

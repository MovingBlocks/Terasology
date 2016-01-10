/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layouts.miglayout;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.ComponentWrapper;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.ContainerWrapper;
import net.miginfocom.layout.Grid;
import net.miginfocom.layout.LC;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.UIWidget;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MigLayout Binding
 * <br><br>
 * see: http://www.miglayout.com/
 * <br><br>
 */
public class MigLayout extends CoreLayout<MigLayout.CCHint> implements ContainerWrapper {
    private Map<ComponentWrapper, CC> ccMap = Maps.newHashMap();
    private Map<UIWidget, ComponentWrapper> wrappers = Maps.newHashMap();
    private List<ComponentWrapper> children = Lists.newArrayList();

    @LayoutConfig
    private String layoutConstraints;
    @LayoutConfig
    private String rowConstraints;
    @LayoutConfig
    private String colConstraints;

    private LC lc;
    private AC rc;
    private AC cc;

    private Grid grid;
    private boolean dirty;
    private MigComponent delegate = new MigComponent(null, null);
    private List<Rect2i> debugRects = Lists.newArrayList();
    @LayoutConfig
    private boolean debug;

    public MigLayout() {
        setLayoutConstraints("");
        setRowConstraints("");
        setColConstraints("");
    }

    public MigLayout(String id) {
        super(id);
        setLayoutConstraints("");
        setRowConstraints("");
        setColConstraints("");
    }

    public void setLc(LC lc) {
        this.lc = lc;
        dirty = true;
    }

    public void setLayoutConstraints(String constraint) {
        layoutConstraints = constraint;
        setLc(ConstraintParser.parseLayoutConstraint(ConstraintParser.prepare(constraint)));
    }

    public void setCC(AC columnConstraint) {
        this.cc = columnConstraint;
        dirty = true;
    }

    public void setColConstraints(String constraint) {
        colConstraints = constraint;
        setCC(ConstraintParser.parseColumnConstraints(ConstraintParser.prepare(constraint)));
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setRc(AC rc) {
        this.rc = rc;
        dirty = true;
    }

    public void setRowConstraints(String constraint) {
        rowConstraints = constraint;
        setRc(ConstraintParser.parseColumnConstraints(ConstraintParser.prepare(constraint)));
    }

    @Override
    public void onDraw(Canvas canvas) {
        int[] bounds = {0, 0, canvas.size().x, canvas.size().y};

        layoutContainer(canvas, bounds);

        for (ComponentWrapper wrapper : wrappers.values()) {
            UIWidget component = (UIWidget) wrapper.getComponent();
            Rect2i region = Rect2i.createFromMinAndSize(wrapper.getX(), wrapper.getY(), wrapper.getWidth(), wrapper.getHeight());
            canvas.drawWidget(component, region);
        }

        if (debug) {
            grid.paintDebug();
        }
        for (Rect2i region : debugRects) {
            canvas.drawLine(region.minX(), region.minY(), region.maxX(), region.minY(), Color.WHITE);
            canvas.drawLine(region.maxX(), region.minY(), region.maxX(), region.maxY(), Color.WHITE);
            canvas.drawLine(region.maxX(), region.maxY(), region.minX(), region.maxY(), Color.WHITE);
            canvas.drawLine(region.minX(), region.maxY(), region.minX(), region.minY(), Color.WHITE);
        }
    }

    @Override
    public void addWidget(UIWidget element, CCHint hint) {
        final ComponentWrapper cw = getWrapper(element);

        final String cStr = ConstraintParser.prepare(hint != null ? hint.cc : "");
        CC constraint = AccessController.doPrivileged((PrivilegedAction<CC>) () -> ConstraintParser.parseComponentConstraint(cStr));

        ccMap.put(cw, constraint);
        wrappers.put(element, cw);
        children.add(cw);
        dirty = true;
    }

    @Override
    public void removeWidget(UIWidget element) {
        ComponentWrapper cw = wrappers.remove(element);
        ccMap.remove(cw);
        children.remove(cw);
        invalidate();
    }

    public void clear() {
        wrappers.clear();
        ccMap.clear();
        children.clear();
        invalidate();
    }

    public void invalidate() {
        dirty = true;
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        int[] bounds = {0, 0, canvas.size().x, canvas.size().y};
        layoutContainer(canvas, bounds);
        return new Vector2i(grid.getWidth()[1], grid.getHeight()[1]);
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return getPreferredContentSize(canvas, Vector2i.zero());
    }

    private ComponentWrapper getWrapper(UIWidget comp) {
        ComponentWrapper cw;
        if (comp instanceof MigLayout) {
            MigLayout migLayout = (MigLayout) comp;
            migLayout.setParent(this);
            cw = migLayout;
        } else {
            cw = new MigComponent(this, comp);
        }
        return cw;
    }

    private void checkCache() {
        if (dirty) {
            grid = null;
        }

        if (grid == null) {
            grid = new Grid(this, lc, rc, cc, ccMap, new ArrayList<>());
        }
        debugRects.clear();

        dirty = false;
    }

    public void layoutContainer(Canvas canvas, int[] bounds) {
        for (ComponentWrapper wrapper : children) {
            if (wrapper instanceof MigLayout) {
                MigLayout layout = (MigLayout) wrapper;
                layout.layoutContainer(canvas, bounds);
            } else if (wrapper instanceof MigComponent) {
                MigComponent migComponent = (MigComponent) wrapper;
                migComponent.calculatePreferredSize(canvas, canvas.size());
            }
        }
        checkCache();
        if (grid.layout(bounds, lc.getAlignX(), lc.getAlignY(), debug, true)) {
            grid = null;
            checkCache();
            grid.layout(bounds, lc.getAlignX(), lc.getAlignY(), debug, false);
        }
    }

    @Override
    public ComponentWrapper[] getComponents() {
        return children.toArray(new ComponentWrapper[children.size()]);
    }

    @Override
    public int getComponentCount() {
        return children.size();
    }

    @Override
    public Object getLayout() {
        return this;
    }

    @Override
    public boolean isLeftToRight() {
        return true;
    }

    @Override
    public void paintDebugCell(int x, int y, int width, int height) {
        debugRects.add(Rect2i.createFromMinAndSize(x, y, width, height));
    }

    @Override
    public Object getComponent() {
        return this;
    }

    @Override
    public int getX() {
        return delegate.getX();
    }

    @Override
    public int getY() {
        return delegate.getY();
    }

    @Override
    public int getWidth() {
        return delegate.getWidth();
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public int getScreenLocationX() {
        return delegate.getScreenLocationX();
    }

    @Override
    public int getScreenLocationY() {
        return delegate.getScreenLocationY();
    }

    @Override
    public int getMinimumWidth(int hHint) {
        return grid.getWidth()[0];
    }

    @Override
    public int getMinimumHeight(int wHint) {
        return grid.getHeight()[0];
    }

    @Override
    public int getPreferredWidth(int hHint) {
        return grid.getWidth()[1];
    }

    @Override
    public int getPreferredHeight(int wHint) {
        return grid.getHeight()[1];
    }

    @Override
    public int getMaximumWidth(int hHint) {
        return grid.getWidth()[2];
    }

    @Override
    public int getMaximumHeight(int wHint) {
        return grid.getHeight()[2];
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        delegate.setBounds(x, y, width, height);
    }

    @Override
    public boolean isVisible() {
        return delegate.isVisible();
    }

    @Override
    public int getBaseline(int width, int height) {
        return delegate.getBaseline(width, height);
    }

    @Override
    public boolean hasBaseline() {
        return delegate.hasBaseline();
    }

    @Override
    public ContainerWrapper getParent() {
        return delegate.getParent();
    }

    public void setParent(MigLayout parent) {
        delegate.setParent(parent);
    }

    @Override
    public float getPixelUnitFactor(boolean isHor) {
        return delegate.getPixelUnitFactor(isHor);
    }

    @Override
    public int getHorizontalScreenDPI() {
        return delegate.getHorizontalScreenDPI();
    }

    @Override
    public int getVerticalScreenDPI() {
        return delegate.getVerticalScreenDPI();
    }

    @Override
    public int getScreenWidth() {
        throw new IllegalAccessError("Not supported!");
    }

    @Override
    public int getScreenHeight() {
        throw new IllegalAccessError("Not supported!");
    }

    @Override
    public String getLinkId() {
        return null;
    }

    @Override
    public int getLayoutHashCode() {
        return delegate.getLayoutHashCode();
    }

    @Override
    public int[] getVisualPadding() {
        return delegate.getVisualPadding();
    }

    @Override
    public void paintDebugOutline(boolean showVisualPadding) {
    }

    @Override
    public int getComponentType(boolean disregardScrollPane) {
        return 0;
    }

    @Override
    public int getContentBias() {
        return -1;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return new Iterator<UIWidget>() {
            private Iterator<ComponentWrapper> it = children.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public UIWidget next() {
                return (UIWidget) it.next().getComponent();
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }

    public static class CCHint implements LayoutHint {
        private String cc = "";

        public CCHint() {
        }

        public CCHint(String cc) {
            this.cc = cc;
        }
    }
}

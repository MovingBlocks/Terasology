/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui.layout;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.ComponentWrapper;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.ContainerWrapper;
import net.miginfocom.layout.Grid;
import net.miginfocom.layout.LC;
import net.miginfocom.layout.LayoutCallback;
import org.terasology.math.Rect2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.UIWidget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MigLayout Binding
 * <p/>
 * see: http://www.miglayout.com/
 *
 * @author synopia
 */
public class MigLayout extends CoreLayout implements ContainerWrapper {
    private final Map<UIWidget, Object> scrConstrMap = Maps.newIdentityHashMap();

    private Map<ComponentWrapper, CC> ccMap = Maps.newHashMap();
    private List<ComponentWrapper> children = Lists.newArrayList();
    private LC lc;
    private AC colSpecs;
    private AC rowSpecs;
    private Grid grid;
    private boolean dirty;
    private MigComponent delegate = new MigComponent(null, null);

    public MigLayout() {
        this("", "", "");
    }

    public MigLayout(String layoutConstraints) {
        this(layoutConstraints, "", "");
    }

    public MigLayout(String layoutConstraints, String colConstraints) {
        this(layoutConstraints, colConstraints, "");
    }

    public MigLayout(String layoutConstraints, String colConstraints, String rowConstraints) {
        setLayoutConstraints(layoutConstraints);
        setColumnConstraints(colConstraints);
        setRowConstraints(rowConstraints);
    }

    public MigLayout(LC layoutConstraints) {
        this(layoutConstraints, null, null);
    }

    public MigLayout(LC layoutConstraints, AC colConstraints) {
        this(layoutConstraints, colConstraints, null);
    }

    public MigLayout(LC layoutConstraints, AC colConstraints, AC rowConstraints) {
        setLayoutConstraints(layoutConstraints);
        setColumnConstraints(colConstraints);
        setRowConstraints(rowConstraints);
    }

    public void setLayoutConstraints(Object constr) {
        if (constr == null || constr instanceof String) {
            constr = ConstraintParser.prepare((String) constr);
            lc = ConstraintParser.parseLayoutConstraint((String) constr);
        } else if (constr instanceof LC) {
            lc = (LC) constr;
        }
        dirty = true;
    }

    public void setColumnConstraints(Object constr) {
        if (constr == null || constr instanceof String) {
            constr = ConstraintParser.prepare((String) constr);
            colSpecs = ConstraintParser.parseColumnConstraints((String) constr);
        } else if (constr instanceof AC) {
            colSpecs = (AC) constr;
        }
        dirty = true;
    }

    public void setRowConstraints(Object constr) {
        if (constr == null || constr instanceof String) {
            constr = ConstraintParser.prepare((String) constr);
            rowSpecs = ConstraintParser.parseColumnConstraints((String) constr);
        } else if (constr instanceof AC) {
            rowSpecs = (AC) constr;
        }
        dirty = true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        layoutContainer();
        for (ComponentWrapper wrapper : ccMap.keySet()) {
            UIWidget component = (UIWidget) wrapper.getComponent();
            Rect2i region = Rect2i.createFromMinAndSize(wrapper.getX(), wrapper.getY(), wrapper.getWidth(), wrapper.getHeight());
            canvas.drawElement(component, region);
        }
    }

    public void addElement(UIWidget comp, Object constr) {
        setComponentConstraintsImpl(comp, constr, true);
    }

    public void setComponentConstraintsImpl(UIWidget comp, Object constr, boolean noCheck) {
        if (!noCheck && !scrConstrMap.containsKey(comp)) {
            throw new IllegalArgumentException("Component must already be added to parent!");
        }
        ComponentWrapper cw;
        if (comp instanceof MigLayout) {
            MigLayout migLayout = (MigLayout) comp;
            migLayout.setParent(this);
            cw = migLayout;
        } else {
            cw = new MigComponent(this, comp);
        }
        if (constr == null || constr instanceof String) {
            String cStr = ConstraintParser.prepare((String) constr);
            scrConstrMap.put(comp, constr);
            ccMap.put(cw, ConstraintParser.parseComponentConstraint(cStr));
            children.add(cw);
        } else if (constr instanceof CC) {
            scrConstrMap.put(comp, constr);
            ccMap.put(cw, (CC) constr);
            children.add(cw);
        }
        dirty = true;
    }

    private void checkCache() {
        if (dirty) {
            grid = null;
        }

        if (grid == null) {
            grid = new Grid(this, lc, rowSpecs, colSpecs, ccMap, new ArrayList<LayoutCallback>());
        }
        dirty = false;
    }

    public void layoutContainer() {
        for (ComponentWrapper wrapper : children) {
            if (wrapper instanceof MigLayout) {
                MigLayout layout = (MigLayout) wrapper;
                layout.layoutContainer();
            }
        }

        checkCache();
        int[] b = new int[]{0, 0, 1200, 700};
        if (grid.layout(b, lc.getAlignX(), lc.getAlignY(), false, true)) {
            grid = null;
            checkCache();
            grid.layout(b, lc.getAlignX(), lc.getAlignY(), false, false);
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
        return delegate.getScreenWidth();
    }

    @Override
    public int getScreenHeight() {
        return delegate.getScreenHeight();
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
    public void paintDebugOutline() {

    }

    @Override
    public int getComponetType(boolean disregardScrollPane) {
        return 0;
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
}

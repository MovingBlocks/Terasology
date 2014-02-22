package org.terasology.rendering.nui.layouts;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.UIWidget;

public class ColumnLayoutTest {

    private ColumnLayout columnLayout;

    private Canvas canvas;

    private UIWidget itemAt1x1;
    private UIWidget itemAt2x1;
    private UIWidget itemAt3x1;
    private UIWidget itemAt1x2;
    private UIWidget itemAt2x2;
    private UIWidget itemAt3x2;

    @Before
    public void setup() {
        columnLayout = new ColumnLayout();

        canvas = mock(Canvas.class);

        itemAt1x1 = mock(UIWidget.class);
        itemAt2x1 = mock(UIWidget.class);
        itemAt3x1 = mock(UIWidget.class);
        itemAt1x2 = mock(UIWidget.class);
        itemAt2x2 = mock(UIWidget.class);
        itemAt3x2 = mock(UIWidget.class);

        columnLayout.setColumns(3);
        columnLayout.addWidget(itemAt1x1);
        columnLayout.addWidget(itemAt2x1);
        columnLayout.addWidget(itemAt3x1);
        columnLayout.addWidget(itemAt1x2);
        columnLayout.addWidget(itemAt2x2);
        columnLayout.addWidget(itemAt3x2);
    }

    @Test
    public void testThreeColumnsAutosizedEqually() throws Exception {

        columnLayout.setAutoSizeColumns(true);
        when(canvas.calculateRestrictedSize(eq(itemAt1x1), any(Vector2i.class))).thenReturn(new Vector2i(50, 10));
        when(canvas.calculateRestrictedSize(eq(itemAt2x1), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(itemAt3x1), any(Vector2i.class))).thenReturn(new Vector2i(10, 10));

        when(canvas.calculateRestrictedSize(eq(itemAt1x2), any(Vector2i.class))).thenReturn(new Vector2i(20, 10));
        when(canvas.calculateRestrictedSize(eq(itemAt2x2), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(itemAt3x2), any(Vector2i.class))).thenReturn(new Vector2i(20, 10));

        Vector2i availableSize = new Vector2i(200, 200);

        Vector2i result = columnLayout.getPreferredContentSize(canvas, availableSize);
        assertEquals(75, result.x);
        assertEquals(20, result.y);

        when(canvas.size()).thenReturn(availableSize);

        columnLayout.onDraw(canvas);

        // Centered in available area, drawn consecutively
        verify(canvas).drawWidget(itemAt1x1, Rect2i.createFromMinAndSize(((200 - 75) / 2), 0, 50, 100));
        verify(canvas).drawWidget(itemAt2x1, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50, 0, 5, 100));
        verify(canvas).drawWidget(itemAt3x1, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50 + 5, 0, 20, 100));

        verify(canvas).drawWidget(itemAt1x2, Rect2i.createFromMinAndSize(((200 - 75) / 2), 100, 50, 100));
        verify(canvas).drawWidget(itemAt2x2, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50, 100, 5, 100));
        verify(canvas).drawWidget(itemAt3x2, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50 + 5, 100, 20, 100));
    }

    @Test
    public void testThreeColumnsProportionallySized() throws Exception {

        columnLayout.setAutoSizeColumns(false);
        columnLayout.setColumnWidths(0.5f, 0.2f, 0.3f);

        when(canvas.calculateRestrictedSize(eq(itemAt1x1), any(Vector2i.class))).thenReturn(new Vector2i(50, 10));
        when(canvas.calculateRestrictedSize(eq(itemAt2x1), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(itemAt3x1), any(Vector2i.class))).thenReturn(new Vector2i(10, 10));

        when(canvas.calculateRestrictedSize(eq(itemAt1x2), any(Vector2i.class))).thenReturn(new Vector2i(20, 10));
        when(canvas.calculateRestrictedSize(eq(itemAt2x2), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(itemAt3x2), any(Vector2i.class))).thenReturn(new Vector2i(20, 10));

        Vector2i availableSize = new Vector2i(200, 200);

        Vector2i result = columnLayout.getPreferredContentSize(canvas, availableSize);
        // This is the size of the first column divided by its ratio.
        // In general, the minimum column size / ratio guarantees the ration
        // and insures that every column has at least as much as its preferred size
        assertEquals(100, result.x);
        assertEquals(20, result.y);

        when(canvas.size()).thenReturn(availableSize);

        columnLayout.onDraw(canvas);

        // Gets half of entire area
        verify(canvas).drawWidget(itemAt1x1, Rect2i.createFromMinAndSize(0, 0, 100, 100));
        // Gets one-fifth of entire area
        verify(canvas).drawWidget(itemAt2x1, Rect2i.createFromMinAndSize(100, 0, 40, 100));
        // Gets three-tens of entire area
        verify(canvas).drawWidget(itemAt3x1, Rect2i.createFromMinAndSize(100 + 40, 0, 60, 100));

        // Gets half of entire area
        verify(canvas).drawWidget(itemAt1x2, Rect2i.createFromMinAndSize(0, 100, 100, 100));
        // Gets one-fifth of entire area
        verify(canvas).drawWidget(itemAt2x2, Rect2i.createFromMinAndSize(100, 100, 40, 100));
        // Gets three-tens of entire area
        verify(canvas).drawWidget(itemAt3x2, Rect2i.createFromMinAndSize(100 + 40, 100, 60, 100));
    }

    @Test
    public void testThreeColumnsMinimallySized() throws Exception {

        columnLayout.setAutoSizeColumns(false);
        columnLayout.setMinimizeWidth(true);
        columnLayout.setMinimizeHeight(true);

        when(canvas.calculateRestrictedSize(eq(itemAt1x1), any(Vector2i.class))).thenReturn(new Vector2i(50, 10));
        when(canvas.calculateRestrictedSize(eq(itemAt2x1), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(itemAt3x1), any(Vector2i.class))).thenReturn(new Vector2i(10, 10));

        when(canvas.calculateRestrictedSize(eq(itemAt1x2), any(Vector2i.class))).thenReturn(new Vector2i(20, 10));
        when(canvas.calculateRestrictedSize(eq(itemAt2x2), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(itemAt3x2), any(Vector2i.class))).thenReturn(new Vector2i(20, 10));

        Vector2i availableSize = new Vector2i(200, 200);

        Vector2i result = columnLayout.getPreferredContentSize(canvas, availableSize);
        assertEquals(75, result.x);
        assertEquals(20, result.y);

        when(canvas.size()).thenReturn(availableSize);

        columnLayout.onDraw(canvas);

        verify(canvas).drawWidget(itemAt1x1, Rect2i.createFromMinAndSize(((200 - 75) / 2), 0, 50, 10));
        verify(canvas).drawWidget(itemAt2x1, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50, 0, 5, 10));
        verify(canvas).drawWidget(itemAt3x1, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50 + 5, 0, 20, 10));

        verify(canvas).drawWidget(itemAt1x2, Rect2i.createFromMinAndSize(((200 - 75) / 2), 10, 50, 10));
        verify(canvas).drawWidget(itemAt2x2, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50, 10, 5, 10));
        verify(canvas).drawWidget(itemAt3x2, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50 + 5, 10, 20, 10));
    }

}
